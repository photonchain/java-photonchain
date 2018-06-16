package com.photon.photonchain.network.ehcacheManager;

import com.photon.photonchain.network.proto.BlockMessage;
import com.photon.photonchain.network.proto.MessageManager;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.entity.Block;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author:PTN
 * @Description:
 * @Date:17:40 2018/1/17
 * @Modified by:
 */
@Component
public class SyncBlockManager {
    final static Logger logger = LoggerFactory.getLogger(SyncBlockManager.class);

    private Cache syncBlockCache = EhCacheManager.getCache("syncBlockCache");

    private static final String SYNC_BLOCK = "SYNC_BLOCK";
    private static final String SYNC_COUNT = "SYNC_COUNT";
    private static final String SYNC_BLOCK_QUEUE = "SYNC_BLOCK_QUEUE";
    private static final String HAS_NEW_BLOCK = "HAS_NEW_BLOCK";
    private static final String BASE_HASH_MARKLE_ROOT = "BASE_HASH_MARKLE_ROOT";
    private static final String COINCIDENT = "COINCIDENT";
    private static final String NEED_CONTRAST = "NEED_CONTRAST";
    private static final String NEED_SYNC_BLOCK_HEIGHT = "NEED_SYNC_BLOCK_HEIGHT";


    public void setSyncBlock(boolean syncBlock) {
        logger.info("【syncBlock："+syncBlock+"】");
        EhCacheManager.put(syncBlockCache, SYNC_BLOCK, syncBlock);
    }

    public boolean isSyncBlock() {
        return EhCacheManager.getCacheValue(syncBlockCache, SYNC_BLOCK, boolean.class);
    }

    public void setSyncCount(int syncCount) {
        EhCacheManager.put(syncBlockCache, SYNC_COUNT, syncCount);
    }

    public int getSyncCount() {
        return EhCacheManager.getCacheValue(syncBlockCache, SYNC_COUNT, int.class);
    }

    public void setSyncBlockQueue() {
        Queue<Map> syncBlockQueue = new LinkedList<Map>();
        EhCacheManager.put(syncBlockCache, SYNC_BLOCK_QUEUE, syncBlockQueue);
        EhCacheManager.put(syncBlockCache, BASE_HASH_MARKLE_ROOT, new ArrayList<String>());
        EhCacheManager.put(syncBlockCache, NEED_CONTRAST, true);
        EhCacheManager.put(syncBlockCache, NEED_SYNC_BLOCK_HEIGHT, 0L);
    }

    public synchronized Queue<Map> addSyncBlockQueue(List<BlockMessage.Block> blockList, long blockHeight,String mac) {
        logger.info("================ "+blockList.size());
        logger.info("---------------"+mac+"-----"+blockHeight);
        List<String> baseHashMarketRoot = EhCacheManager.getCacheValue(syncBlockCache, BASE_HASH_MARKLE_ROOT, List.class);
        List<Block> syncBlockList = new ArrayList<>();
        List<String> syncHashMerkleRoot = new ArrayList<>();
        blockList.forEach(block -> {
            Block saveBlock = MessageManager.parseBlockMessage(block);
            syncBlockList.add(saveBlock);
            syncHashMerkleRoot.add(Hex.toHexString(saveBlock.getBlockHead().getHashMerkleRoot()));
        });
        if (baseHashMarketRoot.isEmpty()) {
            EhCacheManager.put(syncBlockCache, BASE_HASH_MARKLE_ROOT, syncHashMerkleRoot);
            EhCacheManager.put(syncBlockCache, COINCIDENT, false);
        } else {
            if (EhCacheManager.getCacheValue(syncBlockCache, NEED_CONTRAST, boolean.class)) {
                if (baseHashMarketRoot.size() == syncHashMerkleRoot.size() && baseHashMarketRoot.containsAll(syncHashMerkleRoot)) {
                    EhCacheManager.put(syncBlockCache, COINCIDENT, true);
                } else {
                    EhCacheManager.put(syncBlockCache, COINCIDENT, false);
                    EhCacheManager.put(syncBlockCache, NEED_CONTRAST, false);
                }
            }
        }
        Map queueMap = new HashMap();
        queueMap.put(Constants.SYNC_BLOCK_LIST, syncBlockList);
        queueMap.put(Constants.SYNC_BLOCK_HEIGHT, blockHeight);
        queueMap.put(Constants.SYNC_MAC_ADDRESS, mac);
        Queue<Map> syncBlockQueue = EhCacheManager.getCacheValue(syncBlockCache, SYNC_BLOCK_QUEUE, Queue.class);
        syncBlockQueue.offer(queueMap);
        EhCacheManager.put(syncBlockCache, SYNC_BLOCK_QUEUE, syncBlockQueue);

        if (EhCacheManager.getCacheValue(syncBlockCache, NEED_SYNC_BLOCK_HEIGHT, long.class) < blockHeight) {
            EhCacheManager.put(syncBlockCache, NEED_SYNC_BLOCK_HEIGHT, blockHeight);
        }
        return syncBlockQueue;
    }

    public Map getSyncBlockQueue() {
        Queue<Map> syncBlockQueue = EhCacheManager.getCacheValue(syncBlockCache, SYNC_BLOCK_QUEUE, Queue.class);
        Map queueMap = syncBlockQueue.poll();
        EhCacheManager.put(syncBlockCache, SYNC_BLOCK_QUEUE, syncBlockQueue);
        return queueMap;
    }

    public void setHasNewBlock(boolean hasNewBlock) {
        EhCacheManager.put(syncBlockCache, HAS_NEW_BLOCK, hasNewBlock);
    }

    public boolean getHasNewBlock() {
        return EhCacheManager.getCacheValue(syncBlockCache, HAS_NEW_BLOCK, boolean.class);
    }

    public boolean isCoincident() {
        return EhCacheManager.getCacheValue(syncBlockCache, COINCIDENT, boolean.class);
    }

    public long needSyncBlockHeight() {
        return EhCacheManager.getCacheValue(syncBlockCache, NEED_SYNC_BLOCK_HEIGHT, long.class);
    }

    public Queue<Map> getSyncQueue(){
        return EhCacheManager.getCacheValue(syncBlockCache, SYNC_BLOCK_QUEUE, Queue.class);
    }
}