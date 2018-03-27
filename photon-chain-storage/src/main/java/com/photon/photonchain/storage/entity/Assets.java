package com.photon.photonchain.storage.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @Author:PTN
 * @Description:
 * @Date:9:33 2018/3/20
 * @Modified by:
 */
@Entity
@Table(name = "Assets", indexes = {@Index(name = "idx_pubKey", columnList = "pubKey")}, uniqueConstraints = {@UniqueConstraint(columnNames = {"pubKey", "tokenName"})})
public class Assets implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;
    private String pubKey;
    private String tokenName;
    private long totalIncome;
    private long totalExpenditure;
    private long totalEffectiveIncome;
    private String address;

    public Assets() {
    }

    public Assets(String pubKey, String tokenName, long totalIncome, long totalExpenditure, long totalEffectiveIncome, String address) {
        this.pubKey = pubKey;
        this.tokenName = tokenName;
        this.totalIncome = totalIncome;
        this.totalExpenditure = totalExpenditure;
        this.totalEffectiveIncome = totalEffectiveIncome;
        this.address = address;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public long getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(long totalIncome) {
        this.totalIncome = totalIncome;
    }

    public long getTotalExpenditure() {
        return totalExpenditure;
    }

    public void setTotalExpenditure(long totalExpenditure) {
        this.totalExpenditure = totalExpenditure;
    }

    public long getTotalEffectiveIncome() {
        return totalEffectiveIncome;
    }

    public void setTotalEffectiveIncome(long totalEffectiveIncome) {
        this.totalEffectiveIncome = totalEffectiveIncome;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Assets assets = (Assets) o;

        if (totalIncome != assets.totalIncome) return false;
        if (totalExpenditure != assets.totalExpenditure) return false;
        if (totalEffectiveIncome != assets.totalEffectiveIncome) return false;
        if (pubKey != null ? !pubKey.equals(assets.pubKey) : assets.pubKey != null) return false;
        if (tokenName != null ? !tokenName.equals(assets.tokenName) : assets.tokenName != null) return false;
        return address != null ? address.equals(assets.address) : assets.address == null;
    }

    @Override
    public int hashCode() {
        int result = pubKey != null ? pubKey.hashCode() : 0;
        result = 31 * result + (tokenName != null ? tokenName.hashCode() : 0);
        result = 31 * result + (int) (totalIncome ^ (totalIncome >>> 32));
        result = 31 * result + (int) (totalExpenditure ^ (totalExpenditure >>> 32));
        result = 31 * result + (int) (totalEffectiveIncome ^ (totalEffectiveIncome >>> 32));
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Assets{" +
                "pubKey='" + pubKey + '\'' +
                ", tokenName='" + tokenName + '\'' +
                ", totalIncome=" + totalIncome +
                ", totalExpenditure=" + totalExpenditure +
                ", totalEffectiveIncome=" + totalEffectiveIncome +
                ", address='" + address + '\'' +
                '}';
    }
}
