package com.photon.photonchain.network.core;

import com.photon.photonchain.network.ehcacheManager.*;
import com.photon.photonchain.network.proto.InesvMessage;
import com.photon.photonchain.network.proto.MessageManager;
import com.photon.photonchain.storage.repository.UnconfirmedTranRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.SYNC_BLOCK;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.REQUEST;

/**
 * @Author:PTN
 * @Description:
 * @Date:15:48 2018/1/17
 * @Modified by:
 */
@Component
public class SyncBlock {
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;
    @Autowired
    private SyncBlockManager syncBlockManager;
    @Autowired
    private InitializationManager initializationManager;
    @Autowired
    private SyncUnconfirmedTranManager syncUnconfirmedTranManager;
    @Autowired
    private SyncTokenManager syncTokenManager;
    @Autowired
    private UnconfirmedTranRepository unconfirmedTranRepository;

    public void init() {
        if (nioSocketChannelManager.getActiveNioSocketChannelCount() < BigInteger.ONE.longValue()) {
            syncBlockManager.setHasNewBlock(false);
            syncUnconfirmedTranManager.setHasNewTransaction(false);
            syncTokenManager.setHasNewToken(false);
            syncBlockManager.setSyncBlock(false);
            syncUnconfirmedTranManager.setSyncTransaction(false);
            syncTokenManager.setSyncToken(false);
            return;
        }
        syncBlockManager.setSyncBlock(true);
        syncBlockManager.setHasNewBlock(false);
        syncBlockManager.setSyncBlockQueue();
        syncUnconfirmedTranManager.setSyncTransaction(false);
        syncUnconfirmedTranManager.setHasNewTransaction(false);
        syncUnconfirmedTranManager.setTransactionQueue();
        syncTokenManager.setSyncToken(false);
        syncTokenManager.setHasNewToken(false);
        syncTokenManager.setSyncTokenQueue();
        syncBlockManager.setSyncCount(nioSocketChannelManager.getActiveNioSocketChannelCount());
        InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(REQUEST, SYNC_BLOCK);
        long blockHeight = initializationManager.getBlockHeight();
        builder.setBlockHeight(blockHeight);
        unconfirmedTranRepository.deleteAll();
        nioSocketChannelManager.write(builder.build());
    }
}
