package com.photon.photonchain.network.excutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author:PTN
 * @Description:
 * @Date:10:06 2017/12/28
 * @Modified by:
 */
@Component
public class FoundryMachineExcutor extends ThreadPoolTaskExecutor {

    private int corePoolSize = 5;


    private int maxPoolSize = 5;


    private int queueCapacity = 5;


    private int keepAlive = 0;


    public FoundryMachineExcutor() {
        super.setCorePoolSize(corePoolSize);
        super.setMaxPoolSize(maxPoolSize);
        super.setQueueCapacity(queueCapacity);
        super.setThreadNamePrefix("FoundryMachineExcutor-");
        super.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        super.setKeepAliveSeconds(keepAlive);
        super.initialize();
    }
}
