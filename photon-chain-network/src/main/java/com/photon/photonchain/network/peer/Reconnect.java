package com.photon.photonchain.network.peer;

import com.photon.photonchain.network.core.CheckPoint;
import com.photon.photonchain.network.core.SyncBlock;
import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.network.ehcacheManager.SyncBlockManager;
import com.photon.photonchain.storage.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author:PTN
 * @Description:
 * @Date:16:52 2018/5/15
 * @Modified by:
 */
@Component
public class Reconnect {
    private Logger logger = LoggerFactory.getLogger(Reconnect.class);
    @Autowired
    private CheckPoint checkPoint;
    @Autowired
    private PeerClient peerClient;
    @Autowired
    private SyncBlock syncBlock;
    @Autowired
    private SyncBlockManager syncBlockManager;
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;

    public void init() {
        if (nioSocketChannelManager.getActiveNioSocketChannelCount() < Constants.FORGABLE_NODES) {
            logger.info("reconnet...");
            syncBlockManager.setSyncBlock(true);
            nioSocketChannelManager.removeInvalidChannel();
            peerClient.init();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            syncBlock.init();
        }
    }
}
