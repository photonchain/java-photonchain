package com.photon.photonchain.storage.encryption;

import com.photon.photonchain.storage.encryption.eccrypto.SpongyCastleProvider;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import static java.util.Arrays.copyOfRange;


public class SHAEncrypt {

    private static final Provider CRYPTO_PROVIDER;

    private static final String HASH_256_KECCAK_ALGORITHM_NAME;

    private static final String HASH_512_KECCAK_ALGORITHM_NAME;

    private static final String SHA_256;

    private static final String SHA_512;

    private static Logger logger = LoggerFactory.getLogger(SHAEncrypt.class);

    static {
        Security.addProvider(SpongyCastleProvider.getInstance());
        CRYPTO_PROVIDER = Security.getProvider("SC");
        HASH_256_KECCAK_ALGORITHM_NAME = "PTN-KECCAK-256";
        HASH_512_KECCAK_ALGORITHM_NAME = "PTN-KECCAK-512";
        SHA_256 = "SHA-256";
        SHA_512 = "SHA-512";
    }

    public static byte[] sha3(byte[] input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_256_KECCAK_ALGORITHM_NAME, CRYPTO_PROVIDER);
            digest.update(input);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Can't find such algorithm", e);
            throw new RuntimeException(e);
        }

    }

    public static byte[] sha3omit12(byte[] input) {
        byte[] hash = sha3(input);
        return copyOfRange(hash, 12, hash.length);
    }

    public static byte[] SHA256(final Serializable o) {
        return SHA(SerializationUtils.serialize(o), SHA_256);
    }

    public static byte[] SHA512(final Serializable o) {
        return SHA(SerializationUtils.serialize(o), SHA_512);
    }

    private static byte[] SHA(final byte[] strText, final String strType) {
        byte[] strResult = null;
        if (strText != null && strText.length > 0) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                messageDigest.update(strText);
                byte byteBuffer[] = messageDigest.digest();
                StringBuilder strHexString = new StringBuilder();
                for (byte aByteBuffer : byteBuffer) {
                    String hex = Integer.toHexString(0xff & aByteBuffer);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                strResult = strHexString.toString().getBytes();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }
}
