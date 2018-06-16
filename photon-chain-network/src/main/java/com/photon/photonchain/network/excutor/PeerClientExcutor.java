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
public class PeerClientExcutor extends ThreadPoolTaskExecutor {

  private int corePoolSize = 10;


  private int maxPoolSize = 300;

  private int queueCapacity = 8;


  private int keepAlive = 600;

  public PeerClientExcutor() {
    super.setCorePoolSize(corePoolSize);
    super.setMaxPoolSize(maxPoolSize);
    super.setQueueCapacity(queueCapacity);
    super.setThreadNamePrefix("PeerClientExcutor-");
    super.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    super.setKeepAliveSeconds(keepAlive);
    super.initialize();
  }

}
