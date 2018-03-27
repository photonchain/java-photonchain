package com.photon.photonchain.storage.entity;

import org.spongycastle.util.encoders.Hex;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;

/**
 * @Author:PTN
 * @Description:
 * @Date:16:10 2018/2/5
 * @Modified by:
 */
@Entity
public class UnconfirmedTran {
    private String transFrom;

    private String transTo;

    private String remark;

    private String tokenName;

    private long transValue;

    private long fee;

    private long timeStamp;

    private int transType;//0 trans 1 new token
    @Id
    private byte[] transSignature;

    public UnconfirmedTran() {
    }

    public UnconfirmedTran(String transFrom, String transTo, String remark, String tokenName, long transValue, long fee, long timeStamp, int transType) {
        this.transFrom = transFrom;
        this.transTo = transTo;
        this.remark = remark;
        this.tokenName = tokenName;
        this.transValue = transValue;
        this.fee = fee;
        this.timeStamp = timeStamp;
        this.transType = transType;
        this.transSignature = transSignature;
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

    public long getTransValue() {
        return transValue;
    }

    public void setTransValue(long transValue) {
        this.transValue = transValue;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    public byte[] getTransSignature() {
        return transSignature;
    }

    public void setTransSignature(byte[] transSignature) {
        this.transSignature = transSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnconfirmedTran that = (UnconfirmedTran) o;

        if (transValue != that.transValue) return false;
        if (fee != that.fee) return false;
        if (timeStamp != that.timeStamp) return false;
        if (transType != that.transType) return false;
        if (transFrom != null ? !transFrom.equals(that.transFrom) : that.transFrom != null) return false;
        if (transTo != null ? !transTo.equals(that.transTo) : that.transTo != null) return false;
        if (remark != null ? !remark.equals(that.remark) : that.remark != null) return false;
        if (tokenName != null ? !tokenName.equals(that.tokenName) : that.tokenName != null) return false;
        return Arrays.equals(transSignature, that.transSignature);
    }

    @Override
    public int hashCode() {
        int result = transFrom != null ? transFrom.hashCode() : 0;
        result = 31 * result + (transTo != null ? transTo.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (tokenName != null ? tokenName.hashCode() : 0);
        result = 31 * result + (int) (transValue ^ (transValue >>> 32));
        result = 31 * result + (int) (fee ^ (fee >>> 32));
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        result = 31 * result + transType;
        result = 31 * result + Arrays.hashCode(transSignature);
        return result;
    }

    @Override
    public String toString() {
        return "UnconfirmedTran{" +
                "transFrom='" + transFrom + '\'' +
                ", transTo='" + transTo + '\'' +
                ", remark='" + remark + '\'' +
                ", tokenName='" + tokenName + '\'' +
                ", transValue=" + transValue +
                ", fee=" + fee +
                ", timeStamp=" + timeStamp +
                ", transType=" + transType +
                '}';
    }
}