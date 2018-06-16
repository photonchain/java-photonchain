package com.photon.photonchain.network.core;

import com.alibaba.fastjson.JSON;
import com.photon.photonchain.network.ehcacheManager.*;
import com.photon.photonchain.network.utils.FoundryUtils;
import com.photon.photonchain.network.utils.TokenUtil;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.encryption.HashMerkle;
import com.photon.photonchain.storage.encryption.SHAEncrypt;
import com.photon.photonchain.storage.entity.*;
import com.photon.photonchain.storage.repository.TokenRepository;
import com.photon.photonchain.storage.repository.TransactionRepository;
import com.photon.photonchain.storage.repository.UnconfirmedTranRepository;
import net.sf.ehcache.Cache;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:16 2018/2/6
 * @Modified by:
 */
@Component
public class Verification {
    private static Logger logger = LoggerFactory.getLogger(Verification.class);

    @Autowired
    private InitializationManager initializationManager;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UnconfirmedTranRepository unconfirmedTranRepository;
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;
    @Autowired
    private CheckPoint checkPoint;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AssetsManager assetsManager;
    @Autowired
    private UnconfirmedTranManager unconfirmedTranManager;

    private final static String UNCONFIRMEDTRAN_MAP = "unconfirmedTranMap";

    private Cache unconfirmedTranCache = EhCacheManager.getCache("unconfirmedTranCache");

    public boolean verificationUnconfirmedTran(UnconfirmedTran unconfirmedTran) {
        if (transFromOrToIsNull(unconfirmedTran.getTransFrom(), unconfirmedTran.getTransTo())) {
            return false;
        }
        boolean adopt = false;
        boolean verificationTransSignature = ECKey.fromPublicOnly(Hex.decode(unconfirmedTran.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(unconfirmedTran.toString())), JSON.parseObject(new String(unconfirmedTran.getTransSignature()), ECKey.ECDSASignature.class));
        boolean verifyTrade = false;
        Map<String, Long> fromAssets = assetsManager.getAccountAssets(unconfirmedTran.getTransFrom(), unconfirmedTran.getTokenName());
        Map<String, Long> fromAssetsPtn = assetsManager.getAccountAssets(unconfirmedTran.getTransFrom(), Constants.PTN);
        boolean verifyFee = false;
        boolean verifyBalance = false;
        switch (unconfirmedTran.getTransType()) {
            case 0:
                //verify balance
                verifyTrade = this.verifiUntransBalance(unconfirmedTran, fromAssets, fromAssetsPtn);
                break;
            case 1:
                long fee = Double.valueOf(TokenUtil.TokensRate(unconfirmedTran.getTokenName()) * Constants.MININUMUNIT).longValue();
                if (fee == 0) fee = 1;
                verifyFee = fromAssetsPtn.get(Constants.BALANCE) >= fee ? true : false;
                if (fee == unconfirmedTran.getFee() && verifyFee) {
                    verifyTrade = true;
                }
                break;
            case 3:
                //verify balance
                verifyBalance = this.verifiUntransBalance(unconfirmedTran, fromAssets, fromAssetsPtn);
                boolean verifyToken = false;
                String tokenCoin = unconfirmedTran.getTokenName().equalsIgnoreCase(Constants.PTN) ? unconfirmedTran.getExchengeToken() : unconfirmedTran.getTokenName();
                if (tokenRepository.findByName(tokenCoin) != null) {
                    verifyToken = true;
                }
                if (verifyToken && verifyBalance) {
                    verifyTrade = true;
                }
                break;
            case 4:
                //verify balance
                verifyBalance = this.verifiUntransBalance(unconfirmedTran, fromAssets, fromAssetsPtn);
                String bin = unconfirmedTran.getContractBin();
                verifyFee = this.getExchangePtn(bin) / 1000 == unconfirmedTran.getTransValue() ? true : false;
                if (verifyBalance && verifyFee) {
                    verifyTrade = true;
                }
                break;
            case 5:
                //verify balance
                verifyBalance = this.verifiUntransBalance(unconfirmedTran, fromAssets, fromAssetsPtn);
                if (verificationTransSignature == false) {
                    boolean verifyContract = false;
                    boolean verifyExchange = false;
                    Transaction transContract = transactionRepository.findByContract(unconfirmedTran.getContractAddress(), 3);
                    if (transContract != null && unconfirmedTran.getTransFrom().equals(transContract.getTransTo()) && unconfirmedTran.getTransValue() == transContract.getTransValue() && unconfirmedTran.getTokenName().equalsIgnoreCase(transContract.getTokenName())) {
                        verifyContract = true;
                    }

                    if (transContract != null) {
                        List<UnconfirmedTran> list = unconfirmedTranManager.queryUnconfirmedTran(unconfirmedTran.getContractAddress(),5,null,unconfirmedTran.getTransTo(),0);
                        if(!list.isEmpty()){
                            UnconfirmedTran unconfirmedTranExchange = list.get(0);
                            if (unconfirmedTranExchange != null && unconfirmedTranExchange.getTransTo().equals(transContract.getTransFrom()) && unconfirmedTranExchange.getTransFrom().equals(unconfirmedTran.getTransTo())) {
                                verifyExchange = true;
                            }
                        }
                    }
                    if (verifyBalance && verifyContract && verifyExchange) {
                        verificationTransSignature = true;
                        verifyTrade = true;
                    }
                } else {
                    verifyTrade = true;
                }
                break;
            case 6:
                //verify balance
                verifyBalance = this.verifiUntransBalance(unconfirmedTran, fromAssets, fromAssetsPtn);
                verificationTransSignature = true;
                String from = unconfirmedTran.getTransFrom();
                String to = unconfirmedTran.getTransTo();
                String contractAddress = unconfirmedTran.getContractAddress();
                long transValue = unconfirmedTran.getTransValue();
                Transaction transaction = transactionRepository.findByContract(contractAddress, 3);
                if (verifyBalance && transaction != null && transaction.getTransFrom().equals(to) && transaction.getTransTo().equals(from) && transaction.getTransactionHead().getTransValue() == transValue) {
                    verifyTrade = true;
                }
                break;
            default:
                verifyTrade = false;
                break;
        }
        logger.info("【verificationTransSignature】:" + verificationTransSignature);
        logger.info("【verifyTrade】:" + verifyTrade);
        if (verificationTransSignature && verifyTrade) {
            adopt = true;
        }
        return adopt;
    }

    public boolean verificationToken(Token token) {
        Token existsToken = tokenRepository.findByName(token.getName().toLowerCase());
        if (existsToken != null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean verificationBlock(Block block) {
        boolean verifySignature = ECKey.fromPublicOnly(block.getFoundryPublicKey()).verify(SHAEncrypt.sha3(SerializationUtils.serialize(block.getBlockHead())), JSON.parseObject(new String(block.getBlockSignature()), ECKey.ECDSASignature.class));
        boolean verifyPrevHash = Arrays.equals(block.getBlockHead().getHashPrevBlock(), Hex.decode(initializationManager.getLastBlock().getBlockHash()));
        if (!verifySignature || !verifyPrevHash) {
            return false;
        }
        List<Transaction> transactionList = block.getBlockTransactions();
        List<byte[]> transactionSHAList = new ArrayList<>();
        int mining = 0;
        for (Transaction transaction : transactionList) {
            if (transFromOrToIsNull(transaction.getTransFrom(), transaction.getTransTo()) || !verifyTransHead(transaction)) {
                return false;
            }
            UnconfirmedTran existsUnconfirmedTran = null;
            if (transaction.getTransType() != 2) {
                Map<String, UnconfirmedTran> map = EhCacheManager.getCacheValue(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP, Map.class);
                existsUnconfirmedTran = map.get(Hex.toHexString(transaction.getTransSignature()));
                if (existsUnconfirmedTran == null) {
                    return false;
                }
            } else {
                mining++;
                if (!transaction.getTransTo().equals(Hex.toHexString(block.getFoundryPublicKey()))) {
                    return false;
                }
                int diffYear = FoundryUtils.getDiffYear(GenesisBlock.GENESIS_TIME, block.getBlockHead().getTimeStamp());
                long blockReward = FoundryUtils.getBlockReward(block.getFoundryPublicKey(), diffYear, block.getBlockHeight(), initializationManager, false);
                if (transaction.getTransactionHead().getTransValue() != block.getTotalFee() + blockReward) {
                    return false;
                }
                if (mining != 1) {
                    return false;
                }
            }
            boolean verifyTranSignature = ECKey.fromPublicOnly(Hex.decode(transaction.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(transaction.toSignature())), JSON.parseObject(new String(transaction.getTransSignature()), ECKey.ECDSASignature.class));
            switch (transaction.getTransType()) {
                case 0:
                case 1:
                case 2:
                case 3:
                    if (verifyTranSignature == false) {
                        return false;
                    }
                    break;
                case 4:
                    if (!transaction.getTransTo().equals(Hex.toHexString(block.getFoundryPublicKey())) || !transaction.getTokenName().equals(existsUnconfirmedTran.getTokenName()) || !transaction.getTransFrom().equals(existsUnconfirmedTran.getTransFrom()) || transaction.getTransValue() != existsUnconfirmedTran.getTransValue() || transaction.getFee() != 0) {
                        return false;
                    }
                    break;
                case 5:
                    if (verifyTranSignature == false) {
                        if (!transaction.getTransTo().equals(existsUnconfirmedTran.getTransTo()) || !transaction.getTokenName().equals(existsUnconfirmedTran.getTokenName()) || !transaction.getTransFrom().equals(existsUnconfirmedTran.getTransFrom()) || transaction.getTransValue() != existsUnconfirmedTran.getTransValue() || transaction.getFee() != 0) {
                            return false;
                        }
                    }
                    break;
                case 6:
                    if (!transaction.getTransTo().equals(existsUnconfirmedTran.getTransTo()) || !transaction.getTokenName().equals(existsUnconfirmedTran.getTokenName()) || !transaction.getTransFrom().equals(existsUnconfirmedTran.getTransFrom()) || transaction.getTransValue() != existsUnconfirmedTran.getTransValue() || transaction.getFee() != 0) {
                        return false;
                    }
                    break;
                default:
                    break;
            }

            transactionSHAList.add(SHAEncrypt.SHA256(transaction.getTransactionHead().toString()));
        }
        byte[] hashMerkleRoot = transactionSHAList.isEmpty() ? new byte[]{} : HashMerkle.getHashMerkleRoot(transactionSHAList);
        boolean verifyHashMerkleRoot = Arrays.equals(hashMerkleRoot, block.getBlockHead().getHashMerkleRoot());
        boolean verifyFoundrer = false;
        Map<String, Long> foundrerAssetsPtn = assetsManager.getAccountAssets(Hex.toHexString(block.getFoundryPublicKey()), Constants.PTN);
        long balance = foundrerAssetsPtn.get(Constants.BALANCE);
        if (balance > 0) {
            verifyFoundrer = true;
        }
        if (verifySignature && verifyPrevHash && verifyHashMerkleRoot && verifyFoundrer) {
            return true;
        }
        return false;
    }

    public List<Block> verificationSyncBlockList(List<Block> syncBlockList, String macAddress) {
        List<Block> saveBlockList = new ArrayList<>();
        Block verificationBlock = initializationManager.getLastBlock();
        if (!checkPoint.checkDate(syncBlockList)) {
            nioSocketChannelManager.removeTheMac(macAddress);
            return saveBlockList;
        }
        for (Block block : syncBlockList) {
            if (verificationBlock.getBlockHeight() >= block.getBlockHeight()) {
                continue;
            }
            if (verificationBlock(block, verificationBlock, syncBlockList)) {
                saveBlockList.add(block);
            } else {
                logger.info("verificationBlock fail==" + block.getBlockHeight());
                saveBlockList = new ArrayList<>();
                if (syncBlockList.size() > 0) {
                    nioSocketChannelManager.removeTheMac(macAddress);
                }
                break;
            }
            verificationBlock = block;
        }
        return saveBlockList;
    }

    public List<UnconfirmedTran> verificationSyncUnconfirmedTranList(List<UnconfirmedTran> syncUnconfirmedTranList) {
        List<UnconfirmedTran> saveUnconfirmedTranList = new ArrayList<>();
        Iterable<UnconfirmedTran> existsUnconfirmedTran = unconfirmedTranManager.queryUnconfirmedTran(null,-1,null,null,0);//五分钟之前的未确认流水

        List<UnconfirmedTran> existsTransaction = new ArrayList<>();
        for (UnconfirmedTran unconfirmedTran : existsUnconfirmedTran) {
            syncUnconfirmedTranList.remove(unconfirmedTran);
        }
        for (UnconfirmedTran unconfirmedTran : syncUnconfirmedTranList) {
            Transaction transaction = transactionRepository.findByTransSignature(unconfirmedTran.getTransSignature());
            if (transaction != null) {
                existsTransaction.add(unconfirmedTran);
            }
        }
        syncUnconfirmedTranList.removeAll(existsTransaction);
        for (UnconfirmedTran unconfirmedTran : syncUnconfirmedTranList) {
            if (transFromOrToIsNull(unconfirmedTran.getTransFrom(), unconfirmedTran.getTransTo())) {
                return new ArrayList<>();
            }
            boolean verificationTransSignature = false;
            verificationTransSignature = ECKey.fromPublicOnly(Hex.decode(unconfirmedTran.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(unconfirmedTran.toString())), JSON.parseObject(new String(unconfirmedTran.getTransSignature()), ECKey.ECDSASignature.class));
            switch (unconfirmedTran.getTransType()) {
                case 5:
                    if (verificationTransSignature == false) {
                        boolean verifyContract = false;
                        boolean verifyExchange = false;
                        Transaction transContract = transactionRepository.findByContract(unconfirmedTran.getContractAddress(), 3);
                        if (transContract != null && unconfirmedTran.getTransFrom().equals(transContract.getTransTo()) && unconfirmedTran.getTransValue() == transContract.getTransValue() && unconfirmedTran.getTokenName().equalsIgnoreCase(transContract.getTokenName())) {
                            verifyContract = true;
                        }
                        if (transContract != null) {
                            for (UnconfirmedTran unconfirmedTran1 : syncUnconfirmedTranList) {
                                if (unconfirmedTran1.getTransType() == 5 && unconfirmedTran1.getContractAddress().equals(unconfirmedTran.getContractAddress()) && unconfirmedTran1.getTransTo().equals(transContract.getTransFrom()) && unconfirmedTran1.getTransFrom().equals(unconfirmedTran.getTransTo())) {
                                    verifyExchange = true;
                                }
                            }
                        }
                        if (verifyContract && verifyExchange) {
                            verificationTransSignature = true;
                        }
                    }
                    break;
                case 6:
                    String from = unconfirmedTran.getTransFrom();
                    String to = unconfirmedTran.getTransTo();
                    String contractAddress = unconfirmedTran.getContractAddress();
                    long transValue = unconfirmedTran.getTransValue();
                    Transaction transaction = null;
                    try {
                        transaction = transactionRepository.findByContract(contractAddress, 3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (transaction != null && transaction.getTransFrom().equals(to) && transaction.getTransTo().equals(from) && transaction.getTransactionHead().getTransValue() == transValue && transaction.getFee() == 0) {
                        verificationTransSignature = true;
                    }
                    break;
                default:
                    break;
            }
            if (verificationTransSignature) {
                saveUnconfirmedTranList.add(unconfirmedTran);
            } else {
                saveUnconfirmedTranList = new ArrayList<>();
                break;
            }
        }
        return saveUnconfirmedTranList;
    }

    public List<Token> verificationSyncTokenList(List<Token> syncTokenList) {
        List<Token> saveToken = new ArrayList<>();
        Iterable<Token> tokenIterable = tokenRepository.findAll();
        for (Token token : tokenIterable) {
            syncTokenList.remove(token);
        }
        for (Token token : syncTokenList) {
            saveToken.add(token);
        }
        return saveToken;
    }

    public boolean verifiUntransBalance(UnconfirmedTran unconfirmedTran, Map<String, Long> fromAssets, Map<String, Long> fromAssetsPtn) {
        boolean verifyTrade = false;
        boolean verifyFee = false;
        if (!Constants.PTN.equalsIgnoreCase(unconfirmedTran.getTokenName())) {
            boolean verifyTransfer = fromAssets.get(Constants.BALANCE) >= unconfirmedTran.getTransValue() ? true : false;
            verifyFee = fromAssetsPtn.get(Constants.BALANCE) >= unconfirmedTran.getFee() ? true : false;
            if (verifyTransfer && verifyFee) {
                verifyTrade = true;
            }
        } else {
            boolean verifyTransfer = fromAssets.get(Constants.BALANCE) >= unconfirmedTran.getTransValue() + unconfirmedTran.getFee() ? true : false;
            if (verifyTransfer) {
                verifyTrade = true;
            }
        }
        return verifyTrade;
    }

    private boolean verificationBlock(Block block, Block verificationBlock, List<Block> blockList) {
        boolean verifySignature = ECKey.fromPublicOnly(block.getFoundryPublicKey()).verify(SHAEncrypt.sha3(SerializationUtils.serialize(block.getBlockHead())), JSON.parseObject(new String(block.getBlockSignature()), ECKey.ECDSASignature.class));
        boolean verifyPrevHash = Arrays.equals(block.getBlockHead().getHashPrevBlock(), Hex.decode(verificationBlock.getBlockHash()));
        List<Transaction> transactionList = block.getBlockTransactions();
        List<byte[]> transactionSHAList = new ArrayList<>();
        int mining = 0;
        for (Transaction transaction : transactionList) {
            if (transFromOrToIsNull(transaction.getTransFrom(), transaction.getTransTo()) || !verifyTransHead(transaction)) {
                return false;
            }
            switch (transaction.getTransType()) {
                case 0:
                case 1:
                case 2:
                case 3:
                    boolean verifyTranSignature = ECKey.fromPublicOnly(Hex.decode(transaction.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(transaction.toSignature())), JSON.parseObject(new String(transaction.getTransSignature()), ECKey.ECDSASignature.class));
                    if (!verifyTranSignature) {
                        return false;
                    }
                    break;
                case 4:
                case 5:
                case 6:
                    break;
            }
            switch (transaction.getTransType()) {
                case 2:
                    mining++;
                    if (!transaction.getTransTo().equals(Hex.toHexString(block.getFoundryPublicKey()))) {
                        return false;
                    }
                    int diffYear = FoundryUtils.getDiffYear(GenesisBlock.GENESIS_TIME, block.getBlockHead().getTimeStamp());
                    long blockReward = FoundryUtils.getBlockReward(block.getFoundryPublicKey(), diffYear, block.getBlockHeight(), initializationManager, true);
                    if (transaction.getTransactionHead().getTransValue() != block.getTotalFee() + blockReward) {
                        return false;
                    }
                    if (mining != 1) {
                        return false;
                    }
                    break;
                case 4:
                    boolean verifyTransTo = transaction.getTransTo().equals(Hex.toHexString(block.getFoundryPublicKey()));
                    if (verifyTransTo == false) {
                        return false;
                    }
                    Transaction contractTransaction = transactionRepository.findByContract(transaction.getContractAddress(), 3);
                    if (contractTransaction == null) { //no exist db,select blockList
                        for (Block block1 : blockList) {
                            if (String.valueOf(block1.getBlockHeight()).equals(transaction.getRemark())) {
                                for (Transaction transaction1 : block1.getBlockTransactions()) {
                                    if (transaction1.getContractAddress().equals(transaction.getContractAddress()) || transaction1.getTransType() == 3) {
                                        contractTransaction = transaction1;
                                    }
                                }
                            }
                        }
                    }
                    if (contractTransaction == null) {
                        return false;
                    }
                    boolean verifyFee = this.getExchangePtn(contractTransaction.getContractBin()) / 1000 == transaction.getTransValue() ? true : false;
                    if (!contractTransaction.getTransFrom().equals(transaction.getTransFrom()) || !verifyFee) {
                        return false;
                    }
                    break;
                case 5:
                    boolean verifyTranSignature = ECKey.fromPublicOnly(Hex.decode(transaction.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(transaction.toSignature())), JSON.parseObject(new String(transaction.getTransSignature()), ECKey.ECDSASignature.class));
                    if (!verifyTranSignature) {
                        //contractTransaction
                        Transaction transContract = transactionRepository.findByContract(transaction.getContractAddress(), 3);
                        if (transContract == null) {
                            for (Block block1 : blockList) {
                                if (transaction.getRemark().equals(String.valueOf(block1.getBlockHeight()))) {
                                    for (Transaction transaction1 : block1.getBlockTransactions()) {
                                        if (transaction1.getTransType() == 3 && transaction1.getContractAddress().equals(transaction.getContractAddress()) && transaction1.getTransTo().equals(transaction.getTransFrom()) && transaction1.getTokenName().equalsIgnoreCase(transaction.getTokenName()) && transaction1.getTransValue() == transaction.getTransValue()) {
                                            transContract = transaction1;
                                        }
                                    }
                                }
                            }
                        }
                        if (transContract == null) {
                            return false;
                        }
                        //exchangeTrans
                        Transaction exchangeTransaction = null;
                        for (Transaction transaction1 : transactionList) {
                            if (transaction1.getTransType() == 5 && transaction.getContractAddress().equals(transaction1.getContractAddress()) && transaction.getTransTo().equals(transaction1.getTransFrom()) && transaction1.getTransTo().equals(transContract.getTransFrom())) {
                                exchangeTransaction = transaction1;
                            }
                        }
                        if (exchangeTransaction == null || transaction.getFee() != 0) {
                            return false;
                        }
                    }
                    break;
                case 6:
                    String from = transaction.getTransFrom();
                    String to = transaction.getTransTo();
                    String contractAddress = transaction.getContractAddress();
                    long transValue = transaction.getTransactionHead().getTransValue();
                    Transaction transactionContract = transactionRepository.findByContract(contractAddress, 3);
                    if (transactionContract == null) {
                        for (Block block1 : blockList) {
                            if (String.valueOf(block1.getBlockHeight()).equals(transaction.getRemark())) {
                                for (Transaction transaction1 : block1.getBlockTransactions()) {
                                    if (transaction1.getContractAddress().equals(contractAddress) && transaction1.getTransType() == 3) {
                                        transactionContract = transaction1;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if (transactionContract == null || !transactionContract.getTransFrom().equals(to) || !transactionContract.getTransTo().equals(from) || transactionContract.getTransactionHead().getTransValue() != transValue || !transactionContract.getTokenName().equalsIgnoreCase(transaction.getTokenName()) || transaction.getFee() != 0) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
            transactionSHAList.add(SHAEncrypt.SHA256(transaction.getTransactionHead().toString()));
        }
        byte[] hashMerkleRoot = transactionSHAList.isEmpty() ? new byte[]{} : HashMerkle.getHashMerkleRoot(transactionSHAList);
        boolean verifyHashMerkleRoot = Arrays.equals(hashMerkleRoot, block.getBlockHead().getHashMerkleRoot());
        if (verifySignature && verifyPrevHash && verifyHashMerkleRoot) {
            return true;
        }
        logger.info("verifySignature：" + verifySignature);
        logger.info("verifyPrevHash：" + verifyPrevHash);
        logger.info("verifyHashMerkleRoot：" + verifyHashMerkleRoot);
        logger.info("block：" + block);
        return false;
    }


    public long getExchangePtn(String bin) {
        Map<String, Object> contractMap = JSON.parseObject(Hex.decode(bin), Map.class);
        String parameter = null;
        BigDecimal fee = new BigDecimal(0);
        List<String> list = new ArrayList<>();
        for (String s : contractMap.keySet()) {
            if (StringUtils.startsWith(contractMap.get(s).toString().trim(), "set")) {
                String[] variable = ArrayUtils.removeAllOccurences(contractMap.get(s).toString().split(" "), "");
                if (variable[1].equals("coin")) {
                    list.add(contractMap.get(s).toString());
                }
            }
            if (StringUtils.startsWith(contractMap.get(s).toString().trim(), "event")) {
                if (StringUtils.substringBetween(contractMap.get(s).toString(), "event", "(").trim().equals("business")) {
                    parameter = StringUtils.substringBetween(contractMap.get(s).toString(), "(", ")");
                }
            }
        }
        String[] eventParams = parameter.split(",");
        for (int i = 0; i < eventParams.length; i++)
            eventParams[i] = eventParams[i].trim();
        for (String s : list) {
            String[] variable = ArrayUtils.removeAllOccurences(s.split(" "), "");
            if (variable[variable.length - 1].toLowerCase().equals(Constants.PTN) && (variable[2].equals(eventParams[0]) || variable[2].equals(eventParams[1]))) {
                fee = new BigDecimal(variable[4]).multiply(new BigDecimal(Constants.MININUMUNIT));
            }
        }
        return fee.longValue();
    }



    public boolean verifyTransHead(Transaction transaction) {
        TransactionHead head = transaction.getTransactionHead();
        if (transaction.getTransFrom().equals(head.getTransFrom()) && transaction.getTransTo().equals(head.getTransTo()) && transaction.getTransValue() == head.getTransValue() && transaction.getFee() == head.getFee()) {
            return true;
        }
        return false;
    }



    public boolean transFromOrToIsNull(String from, String to) {
        if (from == null || from.equals("") || to == null || to.equals("")) {
            return true;
        } else {
            return false;
        }
    }

}
