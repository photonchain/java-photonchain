package com.photon.photonchain.interfaces.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Author:PTN
 * @Description:
 * @Date:11:56 2018/3/5
 * @Modified by:
 */
public class PropertyUtil {

    private static Properties props;

    static {
        loadProps();
    }

    synchronized static private void loadProps() {
        props = new Properties();
        InputStream in = null;
        try {
            in = PropertyUtil.class.getClassLoader().getResourceAsStream("application.properties");
            props.load(in);
            String config = "application-" + props.get("spring.profiles.active") + ".properties";
            in = PropertyUtil.class.getClassLoader().getResourceAsStream(config);
            props.load(in);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static String getProperty(String key) {
        if (null == props) {
            loadProps();
        }
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if (null == props) {
            loadProps();
        }
        return props.getProperty(key, defaultValue);
    }
}

