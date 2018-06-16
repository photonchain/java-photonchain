package com.photon.photonchain.network.excutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author PTN
 *
 * Created by PTN on 2017/12/26.
 */
@Component
public class PeerServerExcutor extends ThreadPoolTaskExecutor {

  private int corePoolSize = 1;


  private int maxPoolSize = 2;


  private int queueCapacity = 1;


  private int keepAlive = 0;


  public PeerServerExcutor() {
    super.setCorePoolSize(corePoolSize);
    super.setMaxPoolSize(maxPoolSize);
    super.setQueueCapacity(queueCapacity);
    super.setThreadNamePrefix("PeerServerExecutor-");
    super.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    super.setKeepAliveSeconds(keepAlive);
    super.initialize();
  }

}
