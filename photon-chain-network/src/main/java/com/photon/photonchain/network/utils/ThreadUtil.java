package com.photon.photonchain.network.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @Author:PTN
 * @Description:
 * @Date:20:06 2017/12/26
 * @Modified by:
 */
public class ThreadUtil {
    public static ExecutorService getExecutorService(String nameFormat, int corePoolSize, int maximumPoolSize) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(nameFormat).build();

        ExecutorService singleThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024) {
                }, namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        return singleThreadPool;
    }
}
