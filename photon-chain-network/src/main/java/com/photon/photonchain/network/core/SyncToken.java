package com.photon.photonchain.network.core;

import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.network.ehcacheManager.SyncTokenManager;
import com.photon.photonchain.network.proto.InesvMessage;
import com.photon.photonchain.network.proto.MessageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.SYNC_TOKEN;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.REQUEST;

/**
 * @Author:PTN
 * @Description:
 * @Date:19:57 2018/1/31
 * @Modified by:
 */
@Component
public class SyncToken {
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;
    @Autowired
    private SyncTokenManager syncTokenManager;

    public void init() {
        syncTokenManager.setSyncToken(true);
        syncTokenManager.setSyncCount(nioSocketChannelManager.getActiveNioSocketChannelCount());
        InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(REQUEST, SYNC_TOKEN);
        nioSocketChannelManager.write(builder.build());
    }
}
