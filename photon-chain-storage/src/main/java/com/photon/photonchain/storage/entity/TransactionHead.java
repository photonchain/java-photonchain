package com.photon.photonchain.storage.entity;

import java.io.Serializable;

/**
 * @Author:PTN
 * @Description:
 * @Date:9:58 2018/1/16
 * @Modified by:
 */
public class TransactionHead implements Serializable {
    private static final long serialVersionUID = 5176699107596785415L;
    private String transFrom;
    private String transTo;
    private long transValue;
    private long fee;
    private long timeStamp;

    public TransactionHead() {
    }

    public TransactionHead(String transFrom, String transTo, long transValue, long fee, long timeStamp) {
        this.transFrom = transFrom;
        this.transTo = transTo;
        this.transValue = transValue;
        this.fee = fee;
        this.timeStamp = timeStamp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionHead that = (TransactionHead) o;

        if (transValue != that.transValue) return false;
        if (fee != that.fee) return false;
        if (timeStamp != that.timeStamp) return false;
        if (transFrom != null ? !transFrom.equals(that.transFrom) : that.transFrom != null) return false;
        return transTo != null ? transTo.equals(that.transTo) : that.transTo == null;
    }

    @Override
    public int hashCode() {
        int result = transFrom != null ? transFrom.hashCode() : 0;
        result = 31 * result + (transTo != null ? transTo.hashCode() : 0);
        result = 31 * result + (int) (transValue ^ (transValue >>> 32));
        result = 31 * result + (int) (fee ^ (fee >>> 32));
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TransactionHead{" +
                "transFrom='" + transFrom + '\'' +
                ", transTo='" + transTo + '\'' +
                ", transValue=" + transValue +
                ", fee=" + fee +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
