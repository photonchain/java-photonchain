package com.photon.photonchain.network.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @Author:PTN
 * @Description:
 * @Date:19:26 2018/1/16
 * @Modified by:
 */
public class DateUtil {
    private static final String WEB_URL = "http://www.google.com";

    public static long getWebTime() {
        long time = System.currentTimeMillis();
        try {
            URL url = new URL(WEB_URL);
            URLConnection uc = url.openConnection();
            uc.connect();
            time = uc.getDate();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return time;
    }
}
