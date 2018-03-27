package com.photon.photonchain.storage.encryption;

import java.util.ArrayList;
import java.util.List;


public class HashMerkle {
    public static byte[] getHashMerkleRoot(List<byte[]> shalist) {
        byte[] shaone = null;
        byte[] shatwo = null;
        List<byte[]> newSHAList = new ArrayList<>();
        for (int i = 0; i < shalist.size(); i++) {
            if (i % 2 == 0) {
                shaone = shalist.get(i);
            } else {
              shatwo = shalist.get(i);
                newSHAList.add(SHAEncrypt.SHA256(new String(shaone) + new String(shatwo)));
            }
            if (shalist.size() % 2 == 1 && i == shalist.size() - 1) {
              shatwo = shaone;
                newSHAList.add(SHAEncrypt.SHA256(new String(shaone) + new String(shatwo)));
            }
        }
        if (newSHAList.size() > 1) {
            return getHashMerkleRoot(newSHAList);
        } else {
            return newSHAList.get(0);
        }
    }
}
