package com.photon.photonchain.network.utils;

import java.nio.charset.Charset;

/**
 * @Author:PTN
 * @Description:
 * @Date:15:38 2018/1/11
 * @Modified by:
 */
public class DeEnCode {
    private static final String KEY = "FECOI()*&<MNCXZPKL";
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final byte[] KEY_BYTES = KEY.getBytes(CHARSET);

    public static String encode(String enc) {
        byte[] b = enc.getBytes(CHARSET);
        for (int i = 0, size = b.length; i < size; i++) {
            for (byte keyBytes0 : KEY_BYTES) {
                b[i] = (byte) (b[i] ^ keyBytes0);
            }
        }
        return new String(b);
    }

    public static String decode(String dec) {
        byte[] e = dec.getBytes(CHARSET);
        byte[] dee = e;
        for (int i = 0, size = e.length; i < size; i++) {
            for (byte keyBytes0 : KEY_BYTES) {
                e[i] = (byte) (dee[i] ^ keyBytes0);
            }
        }
        return new String(e);
    }
}
