package com.photon.photonchain.storage.entity;


import com.photon.photonchain.storage.constants.Constants;
import org.spongycastle.util.encoders.Hex;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:22:25 2017/12/27
 * @Modified by:
 */
@Entity
@Table(name = "Block", indexes = {@Index(name = "idx_blockHeight", columnList = "blockHeight", unique = true)})
public class Block implements Serializable {
    private static final int MAGIC_NO = Constants.MAGIC_NO;
    private long blockHeight;
    private long blockSize;
    private long totalAmount;
    private long totalFee;
    @Id
    private byte[] blockSignature;
    private byte[] foundryPublicKey;
    @Column(columnDefinition = "TEXT")
    private BlockHead blockHead;
    @OneToMany
    private List<Transaction> blockTransactions;

    @Transient
    private String blockHash;

    public Block() {
    }

    public Block(long blockHeight, long blockSize, long totalAmount, long totalFee, byte[] blockSignature, byte[] foundryPublicKey, BlockHead blockHead, List<Transaction> blockTransactions) {
        this.blockHeight = blockHeight;
        this.blockSize = blockSize;
        this.totalAmount = totalAmount;
        this.totalFee = totalFee;
        this.blockSignature = blockSignature;
        this.foundryPublicKey = foundryPublicKey;
        this.blockHead = blockHead;
        this.blockTransactions = blockTransactions;
    }

    public static int getMagicNo() {
        return MAGIC_NO;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(long totalFee) {
        this.totalFee = totalFee;
    }

    public byte[] getBlockSignature() {
        return blockSignature;
    }

    public void setBlockSignature(byte[] blockSignature) {
        this.blockSignature = blockSignature;
    }

    public byte[] getFoundryPublicKey() {
        return foundryPublicKey;
    }

    public void setFoundryPublicKey(byte[] foundryPublicKey) {
        this.foundryPublicKey = foundryPublicKey;
    }

    public BlockHead getBlockHead() {
        return blockHead;
    }

    public void setBlockHead(BlockHead blockHead) {
        this.blockHead = blockHead;
    }

    public List<Transaction> getBlockTransactions() {
        return blockTransactions;
    }

    public void setBlockTransactions(List<Transaction> blockTransactions) {
        this.blockTransactions = blockTransactions;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Block block = (Block) o;

        if (blockHeight != block.blockHeight) return false;
        if (blockSize != block.blockSize) return false;
        if (totalAmount != block.totalAmount) return false;
        if (totalFee != block.totalFee) return false;
        if (!Arrays.equals(blockSignature, block.blockSignature)) return false;
        if (!Arrays.equals(foundryPublicKey, block.foundryPublicKey)) return false;
        if (blockHead != null ? !blockHead.equals(block.blockHead) : block.blockHead != null) return false;
        return blockTransactions != null ? blockTransactions.equals(block.blockTransactions) : block.blockTransactions == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (blockHeight ^ (blockHeight >>> 32));
        result = 31 * result + (int) (blockSize ^ (blockSize >>> 32));
        result = 31 * result + (int) (totalAmount ^ (totalAmount >>> 32));
        result = 31 * result + (int) (totalFee ^ (totalFee >>> 32));
        result = 31 * result + Arrays.hashCode(blockSignature);
        result = 31 * result + Arrays.hashCode(foundryPublicKey);
        result = 31 * result + (blockHead != null ? blockHead.hashCode() : 0);
        result = 31 * result + (blockTransactions != null ? blockTransactions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockHeight=" + blockHeight +
                ", blockSize=" + blockSize +
                ", totalAmount=" + totalAmount +
                ", totalFee=" + totalFee +
                ", foundryPublicKey=" + Hex.toHexString(foundryPublicKey) +
                ", blockHead=" + blockHead +
                ", blockTransactions=" + blockTransactions +
                '}';
    }

    public List<byte[]> getBlockTransactionSignature() {
        List<byte[]> transactionSignatureList = new ArrayList<>();
        blockTransactions.forEach(transaction -> {
            transactionSignatureList.add(transaction.getTransSignature());
        });
        return transactionSignatureList;
    }
}