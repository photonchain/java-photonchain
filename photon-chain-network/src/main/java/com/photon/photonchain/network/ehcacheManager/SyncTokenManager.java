package com.photon.photonchain.network.ehcacheManager;

import com.photon.photonchain.network.proto.MessageManager;
import com.photon.photonchain.network.proto.TokenMessage;
import com.photon.photonchain.storage.entity.Token;
import net.sf.ehcache.Cache;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @Author:PTN
 * @Description:
 * @Date:20:25 2018/1/31
 * @Modified by:
 */
@Component
public class SyncTokenManager {
    private Cache syncTokenCache = EhCacheManager.getCache("syncTokenCache");

    private static final String SYNC_TOKEN_QUEUE = "SYNC_TOKEN_QUEUE";

    private static final String SYNC_COUNT = "SYNC_COUNT";

    private static final String HAS_NEW_TOKEN = "HAS_NEW_TOKEN";

    private static final String SYNC_TOKEN = "SYNC_TOKEN";

    public void setSyncTokenQueue() {
        Queue<List> syncTokenQueue = new LinkedList<List>();
        EhCacheManager.put(syncTokenCache, SYNC_TOKEN_QUEUE, syncTokenQueue);
    }

    public synchronized Queue<List> addSyncTokenQueue(List<TokenMessage.Token> tokenList) {
        List<Token> syncTokenList = new ArrayList<>();
        tokenList.forEach(tokenMessage -> {
            syncTokenList.add(MessageManager.paresTokenMessage(tokenMessage));
        });
        Queue<List> syncTokenQueue = EhCacheManager.getCacheValue(syncTokenCache, SYNC_TOKEN_QUEUE, Queue.class);
        syncTokenQueue.offer(syncTokenList);
        EhCacheManager.put(syncTokenCache, SYNC_TOKEN_QUEUE, syncTokenQueue);
        return syncTokenQueue;
    }

    public List getSyncTokenQueue() {
        Queue<List> syncTokenQueue = EhCacheManager.getCacheValue(syncTokenCache, SYNC_TOKEN_QUEUE, Queue.class);
        List queueList = syncTokenQueue.poll();
        EhCacheManager.put(syncTokenCache, SYNC_TOKEN_QUEUE, syncTokenQueue);
        return queueList;
    }

    public void setSyncCount(int syncCount) {
        EhCacheManager.put(syncTokenCache, SYNC_COUNT, syncCount);
    }

    public int getSyncCount() {
        return EhCacheManager.getCacheValue(syncTokenCache, SYNC_COUNT, int.class);
    }

    public void setHasNewToken(boolean hasNewToken) {
        EhCacheManager.put(syncTokenCache, HAS_NEW_TOKEN, hasNewToken);
    }

    public boolean getHasNewToken() {
        return EhCacheManager.getCacheValue(syncTokenCache, HAS_NEW_TOKEN, boolean.class);
    }

    public void setSyncToken(boolean syncToken) {
        EhCacheManager.put(syncTokenCache, SYNC_TOKEN, syncToken);
    }

    public boolean isSyncToken() {
        return EhCacheManager.getCacheValue(syncTokenCache, SYNC_TOKEN, boolean.class);
    }
}
