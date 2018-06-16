package com.photon.photonchain.network.core;


import com.alibaba.fastjson.JSON;
import com.photon.photonchain.network.ehcacheManager.*;
import com.photon.photonchain.network.excutor.FoundryMachineExcutor;
import com.photon.photonchain.network.proto.InesvMessage;
import com.photon.photonchain.network.proto.MessageManager;
import com.photon.photonchain.network.utils.DateUtil;
import com.photon.photonchain.network.utils.FoundryUtils;
import com.photon.photonchain.network.utils.ListUtil;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.encryption.HashMerkle;
import com.photon.photonchain.storage.encryption.SHAEncrypt;
import com.photon.photonchain.storage.entity.*;
import com.photon.photonchain.storage.repository.TransactionRepository;
import com.photon.photonchain.storage.repository.UnconfirmedTranRepository;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.*;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.DEL_PARTICIPANT;
import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.NEW_BLOCK;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.RESPONSE;

/**
 * @Author:PTN
 * @Description:
 * @Date:9:48 2017/12/28
 * @Modified by:
 */
@Component
public class FoundryMachine {
    private static Logger logger = LoggerFactory.getLogger(FoundryMachine.class);
    @Autowired
    private FoundryMachineExcutor foundryMachineExcutor;
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;
    @Autowired
    private InitializationManager initializationManager;
    @Autowired
    private SyncUnconfirmedTranManager syncUnconfirmedTranManager;
    @Autowired
    private SyncBlockManager syncBlockManager;
    @Autowired
    private SyncTokenManager syncTokenManager;
    @Autowired
    private FoundryMachineManager foundryMachineManager;
    @Autowired
    private FoundryUtils foundryUtils;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UnconfirmedTranManager unconfirmedTranManager;

    private void blockFoundryMachine(byte[] foundryPublicKey, byte[] foundryPrivateKey) {
        while (foundryMachineManager.foundryMachineIsStart(Hex.toHexString(foundryPublicKey))) {
            logger.info("【syncBlockManager.isSyncBlock()】" + syncBlockManager.isSyncBlock());
            logger.info("【syncUnconfirmedTranManager.isSyncTransaction()】" + syncUnconfirmedTranManager.isSyncTransaction());
            logger.info("【 syncTokenManager.isSyncToken()】" + syncTokenManager.isSyncToken());
            if (nioSocketChannelManager.getActiveNioSocketChannelCount() < Constants.FORGABLE_NODES || syncBlockManager.isSyncBlock() || syncUnconfirmedTranManager.isSyncTransaction() || syncTokenManager.isSyncToken()) {
                InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(RESPONSE, DEL_PARTICIPANT);
                builder.setParticipant(Hex.toHexString(foundryPublicKey));
                List<String> hostList = nioSocketChannelManager.getChannelHostList();
                builder.addAllNodeAddressList(hostList);
                nioSocketChannelManager.write(builder.build());
                initializationManager.setFoundryMachineState(false);
                foundryMachineManager.setFoundryMachine(Hex.toHexString(foundryPublicKey), false);
                return;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String foundryMachiner = foundryUtils.getFoundryMachiner();
            if (foundryMachiner == null) {
                continue;
            }
            if (foundryMachiner.equals(foundryMachineManager.getWaitfoundryMachine())) {
                foundryMachineManager.setWaitFoundryMachineCount(foundryMachineManager.getWaitFoundryMachineCount() + 1);
            } else {
                foundryMachineManager.setWaitfoundryMachine(foundryMachiner);
                foundryMachineManager.setWaitFoundryMachineCount(1);
            }
            if (!Hex.toHexString(foundryPublicKey).equals(foundryMachiner) && foundryMachineManager.getWaitFoundryMachineCount() > 8) {
                foundryMachineManager.setWaitfoundryMachine(null);
                InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(RESPONSE, DEL_PARTICIPANT);
                builder.setParticipant(foundryMachiner);
                List<String> hostList = nioSocketChannelManager.getChannelHostList();
                builder.addAllNodeAddressList(hostList);
                nioSocketChannelManager.write(builder.build());
                continue;
            }
            long interval = DateUtil.getWebTime() - initializationManager.getLastBlock().getBlockHead().getTimeStamp();
            if (interval > Constants.BLOCK_INTERVAL && Hex.toHexString(foundryPublicKey).equals(foundryMachiner)) {
                Block lastBlock = initializationManager.getLastBlock();
                int version = Constants.BLOCK_VERSION;
                long blockHeight = initializationManager.getBlockHeight() + 1;
                long timeStamp = DateUtil.getWebTime();
                List<Transaction> blockTransactions = new ArrayList<>();

                long transValueSum = 0;
                List<UnconfirmedTran> unconfirmedTrans = new ArrayList<>();
                Map<String, UnconfirmedTran> unconfirmedTranMap = unconfirmedTranManager.getUnconfirmedTranMap();
                for (Map.Entry<String, UnconfirmedTran> entry : unconfirmedTranMap.entrySet()) {
                    UnconfirmedTran unconfirmedTran = entry.getValue();
                    if (unconfirmedTran.getTransType() == 4) {
                        transValueSum += unconfirmedTran.getTransValue();
                        unconfirmedTrans.add(unconfirmedTran);
                    }
                }
                if (transValueSum >= Constants.CONTRACT_FUNDS_MIN) {
                    for (UnconfirmedTran unconfirmedTran : unconfirmedTrans) {
                        Transaction contractTransaction = transactionRepository.findByContract(unconfirmedTran.getContractAddress(), 3);
                        if (contractTransaction != null) {
                            TransactionHead transactionHead = new TransactionHead(unconfirmedTran.getTransFrom(), Hex.toHexString(foundryPublicKey), unconfirmedTran.getTransValue(), unconfirmedTran.getFee(), unconfirmedTran.getTimeStamp());
                            Transaction transaction = new Transaction(unconfirmedTran.getTransSignature(), transactionHead, blockHeight, timeStamp - unconfirmedTran.getTimeStamp(), unconfirmedTran.getTransFrom(), Hex.toHexString(foundryPublicKey), String.valueOf(contractTransaction.getBlockHeight()), unconfirmedTran.getTokenName(), unconfirmedTran.getTransType(), unconfirmedTran.getContractAddress(), unconfirmedTran.getContractBin(), unconfirmedTran.getContractType(), unconfirmedTran.getContractState(), unconfirmedTran.getExchengeToken(), unconfirmedTran.getTransValue(), unconfirmedTran.getFee());
                            blockTransactions.add(transaction);
                        }
                    }
                }
                //TODO:unconfirm
                List<UnconfirmedTran> unconfirmedTranList = unconfirmedTranManager.queryUnconfirmedTran(null, -1, null, null, System.currentTimeMillis() - 8000l);
                Collections.sort(unconfirmedTranList);
                Collections.reverse(unconfirmedTranList);
                int count = unconfirmedTranList.size() > Constants.BLOCK_TRANSACTION_SIZE ? Constants.BLOCK_TRANSACTION_SIZE : unconfirmedTranList.size();
                unconfirmedTranList = unconfirmedTranList.subList(0, count);

                long totalAmount = 0;
                long totalFee = 0;

                List<UnconfirmedTran> contraUnconfirmedTranList = new ArrayList<>();
                List<String> contraUnconfirmedTranListTypeSix = new ArrayList<>();

                Map<String, UnconfirmedTran> map = new HashMap<>();

                for (UnconfirmedTran unconfirmedTran : unconfirmedTranList) {
                    if (unconfirmedTran.getTransType() == 5) {
                        contraUnconfirmedTranList.add(unconfirmedTran);
                        continue;
                    }
                    totalFee = totalFee + unconfirmedTran.getFee();
                    TransactionHead transactionHead = new TransactionHead(unconfirmedTran.getTransFrom(), unconfirmedTran.getTransTo(), unconfirmedTran.getTransValue(), unconfirmedTran.getFee(), unconfirmedTran.getTimeStamp());
                    Transaction transaction = new Transaction(unconfirmedTran.getTransSignature(), transactionHead, blockHeight, timeStamp - unconfirmedTran.getTimeStamp(), unconfirmedTran.getTransFrom(), unconfirmedTran.getTransTo(), unconfirmedTran.getRemark(), unconfirmedTran.getTokenName(), unconfirmedTran.getTransType(), unconfirmedTran.getContractAddress(), unconfirmedTran.getContractBin(), unconfirmedTran.getContractType(), unconfirmedTran.getContractState(), unconfirmedTran.getExchengeToken(), unconfirmedTran.getTransValue(), unconfirmedTran.getFee());
                    blockTransactions.add(transaction);

                    if (unconfirmedTran.getTransType() == 6) {
                        contraUnconfirmedTranListTypeSix.add(unconfirmedTran.getContractAddress());
                    }
                }

                List<UnconfirmedTran> contraUnconfirmedTranListTure = new ArrayList<>();
                for (UnconfirmedTran unconfirmedTran : contraUnconfirmedTranList) {
                    String contractAddress = unconfirmedTran.getContractAddress();
                    if (!contraUnconfirmedTranListTypeSix.contains(contractAddress)) {
                        contraUnconfirmedTranListTure.add(unconfirmedTran);
                    }
                }

                for (UnconfirmedTran unconfirmedTran : contraUnconfirmedTranListTure) {
                    //TODO:unconfirm
                    List<UnconfirmedTran> unconfirmedTranList1 = unconfirmedTranManager.queryUnconfirmedTran(unconfirmedTran.getContractAddress(), 6, null, null, -1);
                    if (unconfirmedTranList1.size() == 0) {
                        if (map.get(unconfirmedTran.getContractAddress() + "5") != null) {
                            totalFee = totalFee + unconfirmedTran.getFee();
                            TransactionHead transactionHead = new TransactionHead(unconfirmedTran.getTransFrom(), unconfirmedTran.getTransTo(), unconfirmedTran.getTransValue(), unconfirmedTran.getFee(), unconfirmedTran.getTimeStamp());
                            Transaction transaction = new Transaction(unconfirmedTran.getTransSignature(), transactionHead, blockHeight, timeStamp - unconfirmedTran.getTimeStamp(), unconfirmedTran.getTransFrom(), unconfirmedTran.getTransTo(), unconfirmedTran.getRemark(), unconfirmedTran.getTokenName(), unconfirmedTran.getTransType(), unconfirmedTran.getContractAddress(), unconfirmedTran.getContractBin(), unconfirmedTran.getContractType(), unconfirmedTran.getContractState(), unconfirmedTran.getExchengeToken(), unconfirmedTran.getTransValue(), unconfirmedTran.getFee());
                            blockTransactions.add(transaction);


                            UnconfirmedTran u = map.get(unconfirmedTran.getContractAddress() + "5");
                            totalFee = totalFee + u.getFee();
                            TransactionHead transactionHeadA = new TransactionHead(u.getTransFrom(), u.getTransTo(), u.getTransValue(), u.getFee(), u.getTimeStamp());
                            Transaction transactionA = new Transaction(u.getTransSignature(), transactionHeadA, blockHeight, timeStamp - u.getTimeStamp(), u.getTransFrom(), u.getTransTo(), u.getRemark(), u.getTokenName(), u.getTransType(), u.getContractAddress(), u.getContractBin(), u.getContractType(), u.getContractState(), u.getExchengeToken(), u.getTransValue(), u.getFee());
                            blockTransactions.add(transactionA);
                        } else {
                            map.put(unconfirmedTran.getContractAddress() + "5", unconfirmedTran);
                        }
                    }
                }



                int diffYear = FoundryUtils.getDiffYear(GenesisBlock.GENESIS_TIME, timeStamp);

                long blockReward = FoundryUtils.getBlockReward(foundryPublicKey, diffYear, blockHeight, initializationManager, false);
                ECKey ecKey = new ECKey(new SecureRandom());
                TransactionHead miningTransactionHead = new TransactionHead(Hex.toHexString(ecKey.getPubKey()), Hex.toHexString(foundryPublicKey), totalFee + blockReward, totalFee, timeStamp);
                Transaction miningTransaction = new Transaction(null, miningTransactionHead, blockHeight, 0, Hex.toHexString(ecKey.getPubKey()), Hex.toHexString(foundryPublicKey), "mining", Constants.PTN, 2, blockReward + totalFee, totalFee);
                byte[] transSignature = JSON.toJSONString(ECKey.fromPrivate(ecKey.getPrivKeyBytes()).sign(SHAEncrypt.sha3(SerializationUtils.serialize(miningTransaction.toSignature())))).getBytes();
                miningTransaction.setTransSignature(transSignature);
                blockTransactions.add(miningTransaction);
                byte[] hashPrevBlock = Hex.decode(lastBlock.getBlockHash());
                List<byte[]> transactionSHAList = new ArrayList<>();
                long blockSize = 0;
                for (Transaction transaction : blockTransactions) {
                    totalAmount = totalAmount + transaction.getTransactionHead().getTransValue();
                    TransactionHead transactionHead = transaction.getTransactionHead();
                    transactionSHAList.add(SHAEncrypt.SHA256(transactionHead.toString()));
                    blockSize = blockSize + SerializationUtils.serialize(transactionHead).length;
                }
                byte[] hashMerkleRoot = transactionSHAList.isEmpty() ? new byte[]{} : HashMerkle.getHashMerkleRoot(transactionSHAList);
                BlockHead blockHead = new BlockHead(version, timeStamp, Constants.CUMULATIVE_DIFFICULTY, hashPrevBlock, hashMerkleRoot);
                byte[] blockSignature = JSON.toJSONString(ECKey.fromPrivate(foundryPrivateKey).sign(SHAEncrypt.sha3(SerializationUtils.serialize(blockHead)))).getBytes();
                Block block = new Block(blockHeight, blockSize, totalAmount, totalFee, blockSignature, foundryPublicKey, blockHead, blockTransactions);
                block.setBlockHash(Hex.toHexString(SHAEncrypt.SHA256(block.getBlockHead())));
                InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(RESPONSE, NEW_BLOCK);
                builder.setBlock(MessageManager.createBlockMessage(block));
                List<String> hostList = nioSocketChannelManager.getChannelHostList();
                builder.addAllNodeAddressList(hostList);
                nioSocketChannelManager.write(builder.build());
            }
        }
    }

    public void init(String pubKey, String priKey) {
        foundryMachineExcutor.execute(() -> {
            blockFoundryMachine(
                    Hex.decode(pubKey),
                    Hex.decode(priKey));
        });
    }


}