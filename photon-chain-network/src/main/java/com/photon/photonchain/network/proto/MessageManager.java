package com.photon.photonchain.network.proto;

import com.google.protobuf.ByteString;
import com.photon.photonchain.storage.entity.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:Lin
 * @Description:
 * @Date:17:18 2018/1/3
 * @Modified by:
 */
public class MessageManager {

    public static InesvMessage.Message.Builder createMessageBuilder(MessageTypeEnum.MessageType messageType, EventTypeEnum.EventType eventType) {
        return InesvMessage.Message.newBuilder().setMessageType(messageType).setEventType(eventType);
    }

    public static BlockMessage.Block createBlockMessage(Block block) {
        BlockMessage.Block.Builder builder = BlockMessage.Block.newBuilder();
        builder.setBlockHeight(block.getBlockHeight());
        builder.setBlockSize(block.getBlockSize());
        builder.setTotalAmount(block.getTotalAmount());
        builder.setTotalFee(block.getTotalFee());
        builder.setBlockSignature(ByteString.copyFrom(block.getBlockSignature()));
        builder.setFoundryPublicKey(ByteString.copyFrom(block.getFoundryPublicKey()));
        builder.setBlockHead(createBlockHeadMessage(block.getBlockHead()));
        if (block.getBlockTransactions() != null) {
            for (int i = 0; i < block.getBlockTransactions().size(); i++) {
                builder.addBlockTransactions(createTransactionMessage(block.getBlockTransactions().get(i)));
            }
        }
        builder.setBlockHash(block.getBlockHash());
        return builder.build();
    }

    public static Block parseBlockMessage(BlockMessage.Block messageBlock) {
        Block block = new Block();
        block.setBlockHeight(messageBlock.getBlockHeight());
        block.setBlockSize(messageBlock.getBlockSize());
        block.setTotalAmount(messageBlock.getTotalAmount());
        block.setTotalFee(messageBlock.getTotalFee());
        block.setBlockSignature(messageBlock.getBlockSignature().toByteArray());
        block.setFoundryPublicKey(messageBlock.getFoundryPublicKey().toByteArray());
        block.setBlockHead(parseBlockHeadMessage(messageBlock.getBlockHead()));
        List<Transaction> transactionList = new ArrayList<Transaction>();
        messageBlock.getBlockTransactionsList().forEach(transactionMessage -> {
            transactionList.add(parseTransactionMessage(transactionMessage));
        });
        block.setBlockTransactions(transactionList);
        block.setBlockHash(messageBlock.getBlockHash());
        return block;
    }

    public static BlockHeadMessage.BlockHead createBlockHeadMessage(BlockHead blockHead) {
        BlockHeadMessage.BlockHead.Builder builder = BlockHeadMessage.BlockHead.newBuilder();
        builder.setVersion(blockHead.getVersion());
        builder.setTimeStamp(blockHead.getTimeStamp());
        builder.setCumulativeDifficulty(blockHead.getCumulativeDifficulty().longValue());
        builder.setHashPrevBlock(ByteString.copyFrom(blockHead.getHashPrevBlock()));
        builder.setHashMerkleRoot(ByteString.copyFrom(blockHead.getHashMerkleRoot()));
        return builder.build();
    }

    public static BlockHead parseBlockHeadMessage(BlockHeadMessage.BlockHead messageBlockHead) {
        BlockHead blockHead = new BlockHead();
        blockHead.setVersion(messageBlockHead.getVersion());
        blockHead.setTimeStamp(messageBlockHead.getTimeStamp());
        blockHead.setCumulativeDifficulty(BigInteger.valueOf(messageBlockHead.getCumulativeDifficulty()));
        blockHead.setHashPrevBlock(messageBlockHead.getHashPrevBlock().toByteArray());
        blockHead.setHashMerkleRoot(messageBlockHead.getHashMerkleRoot().toByteArray());
        return blockHead;
    }

    public static TransactionMessage.Transaction createTransactionMessage(Transaction transaction) {
        TransactionMessage.Transaction.Builder builder = TransactionMessage.Transaction.newBuilder();
        builder.setBlockHeight(transaction.getBlockHeight());
        builder.setLockTime(transaction.getLockTime());
        builder.setTransSignature(ByteString.copyFrom(transaction.getTransSignature()));
        builder.setTransactionHead(createTransactionHeadMessage(transaction.getTransactionHead()));
        builder.setTransFrom(transaction.getTransFrom());
        builder.setTransTo(transaction.getTransTo());
        builder.setRemark(transaction.getRemark());
        builder.setTokenName(transaction.getTokenName());
        builder.setTransType(transaction.getTransType());
        builder.setContractAddress(transaction.getContractAddress());
        builder.setContractBin(transaction.getContractBin());
        builder.setContractType(transaction.getContractType());
        builder.setContractState(transaction.getContractState());
        builder.setExchengeToken(transaction.getExchengeToken() != null ? transaction.getExchengeToken() : "");
        builder.setTransValue(transaction.getTransValue());
        builder.setFee(transaction.getFee());
        return builder.build();
    }

    public static Transaction parseTransactionMessage(TransactionMessage.Transaction messageTransaction) {
        Transaction transaction = new Transaction();
        transaction.setBlockHeight(messageTransaction.getBlockHeight());
        transaction.setLockTime(messageTransaction.getLockTime());
        transaction.setTransactionHead(paresTransactionHeadMessage(messageTransaction.getTransactionHead()));
        transaction.setTransSignature(messageTransaction.getTransSignature().toByteArray());
        transaction.setTransFrom(messageTransaction.getTransFrom());
        transaction.setTransTo(messageTransaction.getTransTo());
        transaction.setRemark(messageTransaction.getRemark());
        transaction.setTokenName(messageTransaction.getTokenName());
        transaction.setTransType(messageTransaction.getTransType());
        transaction.setContractAddress(messageTransaction.getContractAddress());
        transaction.setContractBin(messageTransaction.getContractBin());
        transaction.setContractState(messageTransaction.getContractState());
        transaction.setContractType(messageTransaction.getContractType());
        transaction.setExchengeToken(messageTransaction.getExchengeToken());
        transaction.setTransValue(messageTransaction.getTransValue());
        transaction.setFee(messageTransaction.getFee());
        return transaction;
    }

    public static TransactionHeadMessage.TransactionHead createTransactionHeadMessage(TransactionHead transactionHead) {
        TransactionHeadMessage.TransactionHead.Builder builder = TransactionHeadMessage.TransactionHead.newBuilder();
        builder.setTransFrom(transactionHead.getTransFrom());
        builder.setTransTo(transactionHead.getTransTo());
        builder.setTransValue(transactionHead.getTransValue());
        builder.setFee(transactionHead.getFee());
        builder.setTimeStamp(transactionHead.getTimeStamp());
        return builder.build();
    }

    public static TransactionHead paresTransactionHeadMessage(TransactionHeadMessage.TransactionHead messageTransactionHead) {
        TransactionHead transactionHead = new TransactionHead();
        transactionHead.setFee(messageTransactionHead.getFee());
        transactionHead.setTimeStamp(messageTransactionHead.getTimeStamp());
        transactionHead.setTransFrom(messageTransactionHead.getTransFrom());
        transactionHead.setTransTo(messageTransactionHead.getTransTo());
        transactionHead.setTransValue(messageTransactionHead.getTransValue());
        return transactionHead;
    }

    public static TokenMessage.Token createTokenMessage(Token token) {
        TokenMessage.Token.Builder builder = TokenMessage.Token.newBuilder();
        builder.setName(token.getName());
        builder.setSymbol(token.getSymbol());
        builder.setIcon(token.getIcon());
        builder.setDecimals(token.getDecimals());
        return builder.build();
    }

    public static Token paresTokenMessage(TokenMessage.Token messageToken) {
        Token token = new Token();
        token.setName(messageToken.getName());
        token.setSymbol(messageToken.getSymbol());
        token.setIcon(messageToken.getIcon());
        token.setDecimals(messageToken.getDecimals());
        return token;
    }

    public static UnconfirmedTranMessage.UnconfirmedTran createUnconfirmedTranMessage(UnconfirmedTran unconfirmedTran) {
        UnconfirmedTranMessage.UnconfirmedTran.Builder builder = UnconfirmedTranMessage.UnconfirmedTran.newBuilder();
        builder.setTransFrom(unconfirmedTran.getTransFrom());
        builder.setTransTo(unconfirmedTran.getTransTo());
        builder.setRemark(unconfirmedTran.getRemark());
        builder.setTokenName(unconfirmedTran.getTokenName());
        builder.setTransValue(unconfirmedTran.getTransValue());
        builder.setFee(unconfirmedTran.getFee());
        builder.setTimeStamp(unconfirmedTran.getTimeStamp());
        builder.setTransType(unconfirmedTran.getTransType());
        builder.setTransSignature(ByteString.copyFrom(unconfirmedTran.getTransSignature()));
        builder.setContractAddress(unconfirmedTran.getContractAddress());
        builder.setContractBin(unconfirmedTran.getContractBin());
        builder.setContractState(unconfirmedTran.getContractState());
        builder.setContractType(unconfirmedTran.getContractType());
        builder.setUniqueAddress(unconfirmedTran.getUniqueAddress() != null ? unconfirmedTran.getUniqueAddress() : "");
        builder.setExchengeToken(unconfirmedTran.getExchengeToken() == null ? "" : unconfirmedTran.getExchengeToken());
        return builder.build();
    }

    public static UnconfirmedTran paresUnconfirmedTranMessage(UnconfirmedTranMessage.UnconfirmedTran messageUnconfirmedTran) {
        UnconfirmedTran unconfirmedTran = new UnconfirmedTran();
        unconfirmedTran.setTransFrom(messageUnconfirmedTran.getTransFrom());
        unconfirmedTran.setTransTo(messageUnconfirmedTran.getTransTo());
        unconfirmedTran.setRemark(messageUnconfirmedTran.getRemark());
        unconfirmedTran.setTokenName(messageUnconfirmedTran.getTokenName());
        unconfirmedTran.setTransValue(messageUnconfirmedTran.getTransValue());
        unconfirmedTran.setFee(messageUnconfirmedTran.getFee());
        unconfirmedTran.setTimeStamp(messageUnconfirmedTran.getTimeStamp());
        unconfirmedTran.setTransType(messageUnconfirmedTran.getTransType());
        unconfirmedTran.setTransSignature(messageUnconfirmedTran.getTransSignature().toByteArray());
        unconfirmedTran.setContractAddress(messageUnconfirmedTran.getContractAddress());
        unconfirmedTran.setContractBin(messageUnconfirmedTran.getContractBin());
        unconfirmedTran.setContractState(messageUnconfirmedTran.getContractState());
        unconfirmedTran.setContractType(messageUnconfirmedTran.getContractType());
        if ("".equals(messageUnconfirmedTran.getUniqueAddress()) || messageUnconfirmedTran.getUniqueAddress() == null) {
            unconfirmedTran.setUniqueAddress(null);
        } else {
            unconfirmedTran.setUniqueAddress(messageUnconfirmedTran.getUniqueAddress());
        }
        unconfirmedTran.setExchengeToken(messageUnconfirmedTran.getExchengeToken());
        return unconfirmedTran;
    }
}




