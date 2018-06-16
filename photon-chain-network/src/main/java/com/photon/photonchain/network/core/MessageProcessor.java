package com.photon.photonchain.network.core;

import com.photon.photonchain.network.ehcacheManager.*;
import com.photon.photonchain.network.peer.PeerClient;
import com.photon.photonchain.network.proto.*;
import com.photon.photonchain.network.utils.FoundryUtils;
import com.photon.photonchain.network.utils.NetWorkUtil;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.entity.*;
import com.photon.photonchain.storage.repository.*;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.*;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.REQUEST;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.RESPONSE;
import static com.photon.photonchain.network.utils.NetWorkUtil.bytesToInt;

/**
 * @Author:PTN
 * @Description:
 * @Date:15:35 2018/2/6
 * @Modified by:
 */
@Component
public class MessageProcessor {

    private static Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    @Autowired
    private InitializationManager initializationManager;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private SyncBlockManager syncBlockManager;
    @Autowired
    private SyncUnconfirmedTranManager syncUnconfirmedTranManager;
    @Autowired
    private SyncTokenManager syncTokenManager;
    @Autowired
    private SyncBlock syncBlock;
    @Autowired
    private SyncUnconfirmedTran syncUnconfirmedTran;
    @Autowired
    private SyncToken syncToken;
    @Autowired
    private NodeAddressRepository nodeAddressRepository;
    @Autowired
    private PeerClient peerClient;
    @Autowired
    private Verification verification;
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;
    @Autowired
    private ResetData resetData;
    @Autowired
    private FoundryMachineManager foundryMachineManager;
    @Autowired
    private FoundryUtils foundryUtils;
    @Autowired
    private UnconfirmedTranManager unconfirmedTranManager;

    @Transactional(rollbackFor = Exception.class)
    public void requestProcessor(ChannelHandlerContext ctx, InesvMessage.Message msg) {
        switch (msg.getEventType()) {
            case SYNC_BLOCK:
                long blockHieght = 0;
                long requestBlockHeight = msg.getBlockHeight();
                List<BlockMessage.Block> blockList = new ArrayList<>();
                if (!syncBlockManager.isSyncBlock() && !syncUnconfirmedTranManager.isSyncTransaction() && !syncTokenManager.isSyncToken()) {
                    blockHieght = initializationManager.getBlockHeight();
                    if (blockHieght > requestBlockHeight) {
                        Iterable<Block> blockIterable = blockRepository.findByBlockHeight(requestBlockHeight, requestBlockHeight + Constants.SYNC_SIZE);
                        blockIterable.forEach(block -> {
                            blockList.add(MessageManager.createBlockMessage(block));
                        });
                    }
                }
                InesvMessage.Message.Builder syncBlockBuilder = MessageManager.createMessageBuilder(RESPONSE, SYNC_BLOCK);
                syncBlockBuilder.setBlockHeight(blockHieght);
                syncBlockBuilder.addAllBlockList(blockList);
                syncBlockBuilder.setMac(NetWorkUtil.getMACAddress());
                ctx.writeAndFlush(syncBlockBuilder.build());
                break;
            case SYNC_TRANSACTION:
                List<UnconfirmedTranMessage.UnconfirmedTran> unconfirmedTranList = new ArrayList<>();
                long blockHeight = 0;
                if (!syncBlockManager.isSyncBlock() && !syncUnconfirmedTranManager.isSyncTransaction() && !syncTokenManager.isSyncToken()) {
                    Iterable<UnconfirmedTran> unconfirmedTranIterable =unconfirmedTranManager.queryUnconfirmedTran(null,-1,null,null,-1);
                    unconfirmedTranIterable.forEach(unconfirmedTran -> {
                        unconfirmedTranList.add(MessageManager.createUnconfirmedTranMessage(unconfirmedTran));
                    });
                    blockHeight = initializationManager.getBlockHeight();
                }
                InesvMessage.Message.Builder syncTransactionBuilder = MessageManager.createMessageBuilder(RESPONSE, SYNC_TRANSACTION);
                syncTransactionBuilder.addAllUnconfirmedTranList(unconfirmedTranList);
                syncTransactionBuilder.setBlockHeight(blockHeight);
                ctx.writeAndFlush(syncTransactionBuilder.build());
                break;
            case SYNC_TOKEN:
                List<TokenMessage.Token> tokenList = new ArrayList<>();
                if (!syncBlockManager.isSyncBlock() && !syncUnconfirmedTranManager.isSyncTransaction() && !syncTokenManager.isSyncToken()) {
                    Iterable<Token> tokenIterable = tokenRepository.findAll();
                    tokenIterable.forEach(token -> {
                        tokenList.add(MessageManager.createTokenMessage(token));
                    });
                }
                InesvMessage.Message.Builder syncTokenBuilder = MessageManager.createMessageBuilder(RESPONSE, SYNC_TOKEN);
                syncTokenBuilder.addAllTokenList(tokenList);
                ctx.writeAndFlush(syncTokenBuilder.build());
                break;
            case SYNC_PARTICIPANT:
                List<ParticipantMessage.Participant> participantList = new ArrayList<>();
                Map<String, Integer> participantMap = foundryMachineManager.getParticipantList();
                for (String key : participantMap.keySet()) {
                    ParticipantMessage.Participant.Builder participant = ParticipantMessage.Participant.newBuilder();
                    participant.setParticipant(key);
                    participant.setCount(participantMap.get(key));
                    participantList.add(participant.build());
                }
                InesvMessage.Message.Builder participantBuilder = MessageManager.createMessageBuilder(RESPONSE, SYNC_PARTICIPANT);
                participantBuilder.addAllParticipantList(participantList);
                participantBuilder.setBlockHeight(initializationManager.getBlockHeight());
                ctx.writeAndFlush(participantBuilder.build());
                break;
            default:
                break;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void responseProcessor(ChannelHandlerContext ctx, InesvMessage.Message msg) {
        L:
        switch (msg.getEventType()) {
            case NEW_BLOCK:
                logger.info("【NEW_BLOCK,height={}】", initializationManager.getLastBlock().getBlockHeight());
                if (!syncBlockManager.isSyncBlock() && !syncUnconfirmedTranManager.isSyncTransaction() && !syncTokenManager.isSyncToken()) {
                    Block newBlock = MessageManager.parseBlockMessage(msg.getBlock());
                    if (initializationManager.getBlockHeight() - newBlock.getBlockHeight() > 3) {
                        ctx.close();
                        break L;
                    } else if (newBlock.getBlockHeight() - initializationManager.getBlockHeight() > 3) {
                        syncBlock.init();
                        break L;
                    }
                    if (verification.verificationBlock(newBlock)) {
                        List<byte[]> transSignatureList = new ArrayList<>();
                        List<Transaction> transactionList = newBlock.getBlockTransactions();
                        for (Transaction transaction : transactionList) {
                            transSignatureList.add(transaction.getTransSignature());
                            try {
                                transactionRepository.saveTransaction(transaction);
                                if (transaction.getTransType() == 6) {
//                                    unconfirmedTranRepository.deleteByTypeAndAddress(transaction.getContractAddress(), 5);
                                    List<UnconfirmedTran> li = unconfirmedTranManager.queryUnconfirmedTran(transaction.getContractAddress(),5,null,null,-1);
                                    unconfirmedTranManager.deleteUnconfirmedTrans(li);
                                }
                            } catch (Exception e) {
                                break L;
                            }
                        }
                        initializationManager.setLastTransaction(transactionList.get(transactionList.size() - 1));
                        try {
                            unconfirmedTranManager.deleteUnconfirmedTransBysignatures(transSignatureList);
                            int count = foundryMachineManager.getParticipantCount(Hex.toHexString(newBlock.getFoundryPublicKey())) - 1;
                            foundryMachineManager.setParticipant(Hex.toHexString(newBlock.getFoundryPublicKey()), count);
                            if (count < 0) {
                                InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(RESPONSE, DEL_PARTICIPANT);
                                builder.setParticipant(Hex.toHexString(newBlock.getFoundryPublicKey()));
                                List<String> hostList = nioSocketChannelManager.getChannelHostList();
                                builder.addAllNodeAddressList(hostList);
                                nioSocketChannelManager.write(builder.build());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        foundryMachineManager.setWaitFoundryMachineCount(1);
                        blockRepository.save(newBlock);
                        initializationManager.setLastBlock(newBlock);
                        relayMessage(msg);
                        foundryUtils.resetParticipant();
                    }
                } else {
                    syncBlockManager.setHasNewBlock(true);
                }
                break;
            case NEW_TRANSACTION:
                logger.info("【NEW_TRANSACTION,height={}】", initializationManager.getLastBlock().getBlockHeight());
                logger.info("!syncBlockManager.isSyncBlock():" + !syncBlockManager.isSyncBlock());
                logger.info("!syncUnconfirmedTranManager.isSyncTransaction():" + !syncUnconfirmedTranManager.isSyncTransaction());
                logger.info("!syncTokenManager.isSyncToken():" + !syncTokenManager.isSyncToken());
                if (!syncBlockManager.isSyncBlock() && !syncUnconfirmedTranManager.isSyncTransaction() && !syncTokenManager.isSyncToken()) {
                    UnconfirmedTran unconfirmedTran = MessageManager.paresUnconfirmedTranMessage(msg.getUnconfirmedTran());
                    Transaction transaction = transactionRepository.findByTransSignature(unconfirmedTran.getTransSignature());
                    if (transaction != null) {
                        break L;
                    }
                    if (verification.verificationUnconfirmedTran(unconfirmedTran)) {
                        try {
                            if (unconfirmedTran.getTransType() == 5 || unconfirmedTran.getTransType() == 6) {
                                Transaction contractTrans = transactionRepository.findByContract(unconfirmedTran.getContractAddress(), 3);
                                if (contractTrans == null) {
                                    break L;
                                }
                                if (unconfirmedTran.getTransType() == 5 && contractTrans.getContractState() == 2) {
                                    break L;
                                }
                                if (unconfirmedTran.getTransType() == 6 && contractTrans.getContractState() == 1) {
                                    break L;
                                }
                            }
                            unconfirmedTranManager.setUnconfirmedTranMap(unconfirmedTran);
                            if (unconfirmedTran.getTransType() == 5) {
                                String contractAddress = unconfirmedTran.getUniqueAddress().substring(0, unconfirmedTran.getUniqueAddress().indexOf(","));

                                if ( unconfirmedTranManager.queryUnconfirmedTran(contractAddress,5,null,null,-1).size() == 2) {
                                    Transaction transaction1 = new Transaction();
                                    transaction1.setContractAddress(contractAddress);
                                    transaction1.setContractState(1);
                                    transaction1.setTransType(3);
                                    transactionRepository.updateTransactionState(transaction1);
                                    //clear cache
                                    initializationManager.removeContract(contractAddress);
                                }
                            }
                            if (unconfirmedTran.getTransType() == 6) {
                                Transaction transaction1 = new Transaction();
                                transaction1.setContractAddress(unconfirmedTran.getContractAddress());
                                transaction1.setTransType(3);
                                transaction1.setContractState(2);
                                transactionRepository.updateTransactionState(transaction1);
                                //clear cache
                                initializationManager.removeCancelContract(unconfirmedTran.getContractAddress());
                            }

                            //TODO:addressAndPubkey
                            Set<String> pubkeySet = new HashSet<>();
                            pubkeySet.add(unconfirmedTran.getTransFrom());
                            pubkeySet.add(unconfirmedTran.getTransTo());
                            initializationManager.saveAddressAndPubKey(pubkeySet);
                        } catch (Exception e) {
                            logger.info(e.getMessage());
                            break L;
                        }
                        relayMessage(msg);}
                } else {
                    syncUnconfirmedTranManager.setHasNewTransaction(true);
                }
                break;
            case NEW_TOKEN:
                if (!syncBlockManager.isSyncBlock() && !syncUnconfirmedTranManager.isSyncTransaction() && !syncTokenManager.isSyncToken()) {
                    Token token = MessageManager.paresTokenMessage(msg.getToken());
                    UnconfirmedTran unconfirmedToeknTran = MessageManager.paresUnconfirmedTranMessage(msg.getUnconfirmedTran());
                    if (verification.verificationToken(token) && verification.verificationUnconfirmedTran(unconfirmedToeknTran)) {
                        try {
                            unconfirmedTranManager.setUnconfirmedTranMap(unconfirmedToeknTran);
                        } catch (Exception e) {
                            break L;
                        }
                        tokenRepository.save(token);
                        //token cache
                        initializationManager.addTokenDecimal(token.getName(), token.getDecimals());
                        relayMessage(msg);
                    }
                } else {
                    syncTokenManager.setHasNewToken(true);
                }
                break;
            case SYNC_BLOCK:
                String activeMac = msg.getMac();
                boolean isExist = nioSocketChannelManager.getChannelHostList().contains(activeMac);
                Queue<Map> syncBlockQueue = syncBlockManager.getSyncQueue();
                if (isExist) {
                    syncBlockQueue = syncBlockManager.addSyncBlockQueue(msg.getBlockListList(), msg.getBlockHeight(), activeMac);
                } else {
                    ctx.close();
                }
                int syncBlockResponseCount = syncBlockQueue.size();
                logger.info("【SYNC_BLOCK】");
                if (syncBlockManager.getSyncCount() == syncBlockResponseCount || syncBlockResponseCount == nioSocketChannelManager.getActiveNioSocketChannelCount()) {
                    boolean coincident = syncBlockManager.isCoincident();
                    if (coincident) {
                        for (int i = 0; i < syncBlockResponseCount; i++) {
                            Map queueMap = syncBlockManager.getSyncBlockQueue();
                            List<Block> syncBlockList = (List<Block>) queueMap.get(Constants.SYNC_BLOCK_LIST);
                            if (!syncBlockList.isEmpty()) {
                                boolean verifyPrevHash = Arrays.equals(syncBlockList.get(0).getBlockHead().getHashPrevBlock(), Hex.decode(initializationManager.getLastBlock().getBlockHash()));
                                if (verifyPrevHash) {
                                    saveBlocks(syncBlockList);
                                    blockRepository.save(syncBlockList);
                                    initializationManager.setLastBlock(syncBlockList.get(syncBlockList.size() - 1));
                                    break;
                                } else {
                                    resetData.resetAll();
                                    break L;
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i < syncBlockResponseCount; i++) {
                            long blockHeight = initializationManager.getBlockHeight();
                            Map queueMap = syncBlockManager.getSyncBlockQueue();
                            long syncBlockHieght = (long) queueMap.get(Constants.SYNC_BLOCK_HEIGHT);
                            List<Block> syncBlockList = (List<Block>) queueMap.get(Constants.SYNC_BLOCK_LIST);


                            if (blockHeight < syncBlockHieght) {
                                String macAddress = (String) queueMap.get(Constants.SYNC_MAC_ADDRESS);
                                String localMacAddress = NetWorkUtil.getMACAddress();
                                if (!localMacAddress.equals(macAddress)) {
                                    if (syncBlockList.size() == 0) {
                                        nioSocketChannelManager.removeTheMac(macAddress);
                                        break;
                                    }
                                }
                                List<Block> saveBlock = verification.verificationSyncBlockList(syncBlockList, macAddress);
                                if (saveBlock.size() > 0) {
                                    saveBlocks(saveBlock);
                                    blockRepository.save(saveBlock);
                                    initializationManager.setLastBlock(saveBlock.get(saveBlock.size() - 1));
                                }
                            }
                        }
                    }
                    if (syncBlockManager.needSyncBlockHeight() > initializationManager.getBlockHeight()) {
                        syncBlock.init();
                    } else {
                        syncBlockManager.setSyncBlock(false);
                        syncUnconfirmedTran.init();
                    }
                }
                break;
            case SYNC_TRANSACTION:
                logger.info("【SYNC_TRANSACTION】" + msg.getUnconfirmedTranListList().size());
                Queue<Map> syncTransactionQueue = syncUnconfirmedTranManager.addTransactionQueue(msg.getUnconfirmedTranListList(), msg.getBlockHeight());
                int syncTransactionResponseCount = syncTransactionQueue.size();
                if (syncUnconfirmedTranManager.getSyncCount() == syncTransactionResponseCount || syncTransactionResponseCount == nioSocketChannelManager.getActiveNioSocketChannelCount()) {
                    for (int i = 0; i < syncTransactionResponseCount; i++) {
                        Map queueMap = syncUnconfirmedTranManager.getTransactionQueue();
                        List<UnconfirmedTran> syncTransactionList = (List<UnconfirmedTran>) queueMap.get(Constants.SYNC_TRANSACTION_LIST);
                        long blockHeight = (long) queueMap.get(Constants.SYNC_BLOCK_HEIGHT);
                        if (syncUnconfirmedTranManager.getBlockHeight() == blockHeight) {
                            List<UnconfirmedTran> saveTransaction = verification.verificationSyncUnconfirmedTranList(syncTransactionList);
                            if (saveTransaction.size() > 0) {
                                for(UnconfirmedTran unconfirmedTran:saveTransaction){
                                    unconfirmedTranManager.setUnconfirmedTranMap(unconfirmedTran);
                                }
                                //TODO:addressAndPubKey
                                Set<String> pubkeySet = new HashSet<>();
                                saveTransaction.forEach(transaction -> {
                                    pubkeySet.add(transaction.getTransFrom());
                                    pubkeySet.add(transaction.getTransTo());
                                });
                                initializationManager.saveAddressAndPubKey(pubkeySet);
                            }
                            break;
                        }
                    }
                    syncUnconfirmedTranManager.setSyncTransaction(false);
                    syncToken.init();
                }
                break;
            case SYNC_TOKEN:
                Queue<List> syncTokenQueue = syncTokenManager.addSyncTokenQueue(msg.getTokenListList());
                int syncTokenResponseCount = syncTokenQueue.size();
                if (syncTokenManager.getSyncCount() == syncTokenResponseCount || syncTokenResponseCount == nioSocketChannelManager.getActiveNioSocketChannelCount()) {
                    for (int i = 0; i < syncTokenResponseCount; i++) {
                        List<Token> saveTokenList = verification.verificationSyncTokenList(syncTokenManager.getSyncTokenQueue());
                        if (saveTokenList.size() > 0) {
                            tokenRepository.save(saveTokenList);
                            for (Token token : saveTokenList) {
                                initializationManager.addTokenDecimal(token.getName(), token.getDecimals());
                            }
                        }
                    }
                    if (syncUnconfirmedTranManager.getHasNewTransaction() || syncBlockManager.getHasNewBlock() || syncTokenManager.getHasNewToken()) {
                        syncBlock.init();
                    } else {
                        foundryMachineManager.delAllParticipant();
                        InesvMessage.Message.Builder syncbuilder = MessageManager.createMessageBuilder(REQUEST, SYNC_PARTICIPANT);
                        nioSocketChannelManager.write(syncbuilder.build());
                    }
                }
                break;
            case NODE_ADDRESS:
                List<String> nodeList = new ArrayList<>();
                msg.getNodeAddressListList().forEach(nodeList::add);
                nodeList.removeAll(initializationManager.getNodeList());
                if (!nodeList.isEmpty()) {
                    List<NodeAddress> saveNodeList = new ArrayList<>();
                    initializationManager.getNodeList().addAll(nodeList);
                    nodeList.forEach(node -> {
                        peerClient.poolsConnect(bytesToInt(Hex.decode(node)));
                        saveNodeList.add(new NodeAddress(node, 0));
                    });
                    nodeAddressRepository.save(saveNodeList);
                }
                break;
            case PUSH_MAC:
                String mac = msg.getMac();
                String localhostMac = NetWorkUtil.getMACAddress();
                if (mac.equals(localhostMac)) {
                    ctx.close();
                } else {
                    nioSocketChannelManager.addNioSocketChannel(mac, ctx);
                }
                break;
            case ADD_PARTICIPANT:
                String addParticipant = msg.getParticipant();
                foundryMachineManager.addParticipant(addParticipant, 0);
                relayMessage(msg);
                foundryUtils.resetParticipant();
                break;
            case DEL_PARTICIPANT:
                String delParticipant = msg.getParticipant();
                foundryMachineManager.delParticipant(delParticipant);
                foundryMachineManager.setWaitfoundryMachine(null);
                foundryMachineManager.setWaitFoundryMachineCount(1);
                relayMessage(msg);
                foundryUtils.resetParticipant();
                break;
            case SYNC_PARTICIPANT:
                if (initializationManager.getBlockHeight() == msg.getBlockHeight()) {
                    msg.getParticipantListList().forEach(participant -> {
                        foundryMachineManager.addParticipant(participant.getParticipant(), participant.getCount());
                    });
                    foundryMachineManager.setWaitFoundryMachineCount(1);
                    if (syncUnconfirmedTranManager.getHasNewTransaction() || syncBlockManager.getHasNewBlock() || syncTokenManager.getHasNewToken()) {
                        syncBlock.init();
                    } else {
                        syncTokenManager.setSyncToken(false);
                    }
                }
                break;
            case SET_ZERO_PARTICIPANT:
                break;
            case NEW_CONTRACT:
                String contractAddress = msg.getContractAddresss();
                initializationManager.setContract(contractAddress);
                break;
            case IS_CANCEL:
                String cancelContractAddress = msg.getContractAddresss();
                initializationManager.setCancelContract(cancelContractAddress);
                break;
            case CANCEL_CONTRACT:
                String address = msg.getContractAddresss();
                Transaction transaction = transactionRepository.findByContract(address, 3);
                transaction.setContractState(2);
                transactionRepository.updateContranctState(transaction);
                relayMessage(msg);
                break;
            default:
                break;
        }

    }

    private void saveBlocks(List<Block> saveBlock) {
        saveBlock.forEach(block -> {
            try {
                transactionRepository.save(block.getBlockTransactions());
                //TODO:addressAndPubkey
                Set<String> pubkeySet = new HashSet<>();
                block.getBlockTransactions().forEach(transaction -> {
                    pubkeySet.add(transaction.getTransFrom());
                    pubkeySet.add(transaction.getTransTo());
                });
                initializationManager.saveAddressAndPubKey(pubkeySet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void relayMessage(InesvMessage.Message msg) {
        List<String> notifiedHostList = new ArrayList<>();
        List<String> ignoreList = new ArrayList<>();
        msg.getNodeAddressListList().forEach(host -> {
            notifiedHostList.add(host);
            ignoreList.add(host);
        });
        InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(RESPONSE, msg.getEventType());
        switch (msg.getEventType()) {
            case NEW_TRANSACTION:
                builder.setUnconfirmedTran(msg.getUnconfirmedTran());
                break;
            case NEW_BLOCK:
                builder.setBlock(msg.getBlock());
                break;
            case NEW_TOKEN:
                builder.setUnconfirmedTran(msg.getUnconfirmedTran());
                builder.setToken(msg.getToken());
                break;
            case ADD_PARTICIPANT:
                builder.setParticipant(msg.getParticipant());
                break;
            case DEL_PARTICIPANT:
                builder.setParticipant(msg.getParticipant());
                break;
            case CANCEL_CONTRACT:
                builder.setContractAddresss(msg.getContractAddresss());
                break;
            case SET_ZERO_PARTICIPANT:
                break;
            default:
                break;
        }
        List<String> newHostList = nioSocketChannelManager.getChannelHostList();
        notifiedHostList.removeAll(newHostList);
        newHostList.addAll(notifiedHostList);
        builder.addAllNodeAddressList(newHostList);
        nioSocketChannelManager.writeWithOutCtxList(builder.build(), ignoreList);
    }





}
