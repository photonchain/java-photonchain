package com.photon.photonchain.storage.entity;


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
@Table(name = "AddressAndPubKey", indexes = {@Index(name = "address", columnList = "address", unique = true)})
public class AddressAndPubKey implements Serializable {
    @Id
    private String address;
    private String pubKey;

    public AddressAndPubKey() {
    }

    public AddressAndPubKey(String address, String pubKey) {
        this.address = address;
        this.pubKey = pubKey;
    }

    public String getAddress() {
        return address;
    }

    public AddressAndPubKey setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getPubKey() {
        return pubKey;
    }

    public AddressAndPubKey setPubKey(String pubKey) {
        this.pubKey = pubKey;
        return this;
    }
}