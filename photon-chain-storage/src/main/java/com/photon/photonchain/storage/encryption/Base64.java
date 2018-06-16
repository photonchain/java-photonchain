package com.photon.photonchain.storage.encryption;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @Author:PTN
 * @Description:
 * @Date:17:20 2018/1/8
 * @Modified by:
 */
public class Base64 {

    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    public static String encryptBASE64(byte[] key) throws Exception {
        return (new BASE64Encoder()).encodeBuffer(key);
    }
}
