package com.photon.photonchain.storage.encryption;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;



@Component
public class RSAEncrypt {

    private RSAPrivateKey privateKey;


    private RSAPublicKey publicKey;


    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }


    public RSAPublicKey getPublicKey() {
        return publicKey;
    }


    public static RSAPrivateKey getPrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return (RSAPrivateKey) privateKey;
    }


    public static RSAPublicKey getPublicKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return (RSAPublicKey) publicKey;
    }


    public void genKeyPair() {
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }


    public byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData) throws Exception {
        if (publicKey == null) {
            throw new Exception("pubkey is null, pls set");
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(plainTextData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("no algorithm");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("public invalid,pls check");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("data length invalid");
        } catch (BadPaddingException e) {
            throw new Exception("data broken");
        }
    }


    public byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData) throws Exception {
        if (privateKey == null) {
            throw new Exception("private key is null");
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] output = cipher.doFinal(cipherData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("no algorithm");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("private invalid,pls check");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("data length invalid");
        } catch (BadPaddingException e) {
            throw new Exception("data broken");
        }
    }


    public byte[] executeSignature(byte[] keyBytes, byte[] signDate) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(privateKey);
        signature.update(signDate);
        byte[] result = signature.sign();
        return result;
    }


    public boolean verifySignature(byte[] keyBytes, byte[] signDate, byte[] result) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicKey);
        signature.update(signDate);
        boolean bool = signature.verify(result);
        return bool;
    }

    public byte[] getPubFromPri(byte[] privateKey) {
        byte[] head = new byte[]{48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0};
        byte[] content = new byte[133];
        byte[] publicKey = new byte[head.length + content.length];
        System.arraycopy(privateKey, 37, content, 0, content.length);
        System.arraycopy(head, 0, publicKey, 0, head.length);
        System.arraycopy(content, 0, publicKey, head.length, content.length);
        return publicKey;
    }
}