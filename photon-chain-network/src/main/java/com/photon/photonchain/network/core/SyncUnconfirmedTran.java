package com.photon.photonchain.network.core;

import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.network.ehcacheManager.SyncUnconfirmedTranManager;
import com.photon.photonchain.network.proto.InesvMessage;
import com.photon.photonchain.network.proto.MessageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.SYNC_TRANSACTION;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.REQUEST;

/**
 * @Author:PTN
 * @Description:
 * @Date:16:41 2018/1/19
 * @Modified by:
 */
@Component
public class SyncUnconfirmedTran {
    @Autowired
    private SyncUnconfirmedTranManager syncUnconfirmedTranManager;
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;

    public void init() {
        syncUnconfirmedTranManager.setSyncTransaction(true);
        syncUnconfirmedTranManager.setSyncCount(nioSocketChannelManager.getActiveNioSocketChannelCount());
        syncUnconfirmedTranManager.setBlockHeight(0);
        InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(REQUEST, SYNC_TRANSACTION);
        nioSocketChannelManager.write(builder.build());
    }
}
