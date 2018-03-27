package com.photon.photonchain.storage.entity;

import org.spongycastle.util.encoders.Hex;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @Author:lqh
 * @Description:
 * @Date:14:00 2018/01/09
 * @Modified by:
 */
@Entity
public class Transaction implements Serializable {
    @Id
    private byte[] transSignature;
    @Column(columnDefinition = "TEXT")
    private TransactionHead transactionHead;

    private long blockHeight;

    private long lockTime;

    private String transFrom;

    private String transTo;

    private String remark;

    private String tokenName;

    private int transType;//0 trans 1 new token 2 miner

    public Transaction() {
    }

    public Transaction(byte[] transSignature, TransactionHead transactionHead, long blockHeight, long lockTime, String transFrom, String transTo, String remark, String tokenName, int transType) {
        this.transSignature = transSignature;
        this.transactionHead = transactionHead;
        this.blockHeight = blockHeight;
        this.lockTime = lockTime;
        this.transFrom = transFrom;
        this.transTo = transTo;
        this.remark = remark;
        this.tokenName = tokenName;
        this.transType = transType;
    }

    public byte[] getTransSignature() {
        return transSignature;
    }

    public void setTransSignature(byte[] transSignature) {
        this.transSignature = transSignature;
    }

    public TransactionHead getTransactionHead() {
        return transactionHead;
    }

    public void setTransactionHead(TransactionHead transactionHead) {
        this.transactionHead = transactionHead;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public String getTransFrom() {
        return transFrom;
    }

    public void setTransFrom(String transFrom) {
        this.transFrom = transFrom;
    }

    public String getTransTo() {
        return transTo;
    }

    public void setTransTo(String transTo) {
        this.transTo = transTo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (blockHeight != that.blockHeight) return false;
        if (lockTime != that.lockTime) return false;
        if (transType != that.transType) return false;
        if (!Arrays.equals(transSignature, that.transSignature)) return false;
        if (transactionHead != null ? !transactionHead.equals(that.transactionHead) : that.transactionHead != null)
            return false;
        if (transFrom != null ? !transFrom.equals(that.transFrom) : that.transFrom != null) return false;
        if (transTo != null ? !transTo.equals(that.transTo) : that.transTo != null) return false;
        if (remark != null ? !remark.equals(that.remark) : that.remark != null) return false;
        return tokenName != null ? tokenName.equals(that.tokenName) : that.tokenName == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(transSignature);
        result = 31 * result + (transactionHead != null ? transactionHead.hashCode() : 0);
        result = 31 * result + (int) (blockHeight ^ (blockHeight >>> 32));
        result = 31 * result + (int) (lockTime ^ (lockTime >>> 32));
        result = 31 * result + (transFrom != null ? transFrom.hashCode() : 0);
        result = 31 * result + (transTo != null ? transTo.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (tokenName != null ? tokenName.hashCode() : 0);
        result = 31 * result + transType;
        return result;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transSignature=" + Hex.toHexString(transSignature) +
                ", transactionHead=" + transactionHead +
                ", blockHeight=" + blockHeight +
                ", lockTime=" + lockTime +
                ", transFrom='" + transFrom + '\'' +
                ", transTo='" + transTo + '\'' +
                ", remark='" + remark + '\'' +
                ", tokenName='" + tokenName + '\'' +
                ", transType=" + transType +
                '}';
    }

    public String toSignature() {
        return "UnconfirmedTran{" +
                "transFrom='" + transFrom + '\'' +
                ", transTo='" + transTo + '\'' +
                ", remark='" + remark + '\'' +
                ", tokenName='" + tokenName + '\'' +
                ", transValue=" + transactionHead.getTransValue() +
                ", fee=" + transactionHead.getFee() +
                ", timeStamp=" + transactionHead.getTimeStamp() +
                ", transType=" + transType +
                '}';
    }

    public UnconfirmedTran getUnconfirmedTran() {
        return new UnconfirmedTran(transFrom, transTo, remark, tokenName, transactionHead.getTransValue(), transactionHead.getFee(), transactionHead.getTimeStamp(), transType);
    }
}
