package com.photon.photonchain.network.test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class AESOperator {

//  a0b891c2d563e4f7
    private String sKey = "abcdef0123456789";
    private String ivParameter = "0123456789abcdef";
    private static AESOperator instance = null;

    private AESOperator() {

    }

    public static AESOperator getInstance() {
        if (instance == null)
            instance = new AESOperator();
        return instance;
    }


    public String encrypt(String sSrc){
        String result = "";
        try {
            Cipher cipher;
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] raw = sKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
            result = new BASE64Encoder().encode(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }


    public String decrypt(String sSrc){
        try {
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "utf-8");
            return originalString;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args){

        String cSrc = "04b209f1d975cc56ee76f608cfa338dd1c0096cc4c23d8d31ad8796e95c44c16ec845fc20530743982e89fab9ba9e04706887b390ce0efd3cfdb779ba03868c6e";
        System.out.println(cSrc + "  length" + cSrc.length());

        long lStart = System.currentTimeMillis();
        String enString = AESOperator.getInstance().encrypt(cSrc);
        System.out.println("after：" + enString + "length" + enString.length());

        long lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("encrypt use：" + lUseTime + "ms");

        lStart = System.currentTimeMillis();
        String DeString = AESOperator.getInstance().decrypt(enString);
        System.out.println("after：" + DeString);
        lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("use：" + lUseTime + "ms");
    }
}