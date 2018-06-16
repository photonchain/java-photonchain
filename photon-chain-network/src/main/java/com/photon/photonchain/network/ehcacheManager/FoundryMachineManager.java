package com.photon.photonchain.network.ehcacheManager;

import com.photon.photonchain.storage.constants.Constants;
import net.sf.ehcache.Cache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:42 2018/1/12
 * @Modified by:
 */
@Component
public class FoundryMachineManager {
    private Cache foundryMachineCache = EhCacheManager.getCache("foundryMachineCache");
    private Cache participantCache = EhCacheManager.getCache("participantCache");
    private static final String WAIT = "WAIT";
    private static final String WAIT_COUNT = "WAIT_COUNT";

    public void setFoundryMachine(String pubKey, boolean isStart) {
        EhCacheManager.put(foundryMachineCache, pubKey, isStart);
    }

    public boolean foundryMachineIsStart(String pubKey) {
        boolean res = false;
        try {
            res = EhCacheManager.getCacheValue(foundryMachineCache, pubKey, boolean.class);
        } catch (Exception e) {
        }
        return res;
    }

    public void setParticipant(String pubKey, int count) {
        EhCacheManager.put(participantCache, pubKey, count);
    }

    public void addParticipant(String pubKey, int count) {
        if (!EhCacheManager.existKey(participantCache, pubKey)) {
            EhCacheManager.put(participantCache, pubKey, count);
        }
    }

    public void delParticipant(String pubKey) {
        EhCacheManager.remove(participantCache, pubKey);
    }

    public void delAllParticipant() {
        participantCache.removeAll();
    }

    public int getParticipantCount(String pubKey) {
        return EhCacheManager.getCacheValue(participantCache, pubKey, int.class);
    }

    public Map<String, Integer> getParticipantList() {
        Map<String, Integer> participantMap = new HashMap<>();
        participantCache.getKeys().forEach(key -> {
            participantMap.put((String) key, (Integer) participantCache.get(key).getObjectValue());
        });
        return participantMap;
    }

    public void setWaitfoundryMachine(String pubKey) {
        EhCacheManager.put(foundryMachineCache, WAIT, pubKey);
    }

    public String getWaitfoundryMachine() {
        try {
            return EhCacheManager.getCacheValue(foundryMachineCache, WAIT, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void setWaitFoundryMachineCount(int count) {
        EhCacheManager.put(foundryMachineCache, WAIT_COUNT, count);
    }

    public int getWaitFoundryMachineCount() {
        return EhCacheManager.getCacheValue(foundryMachineCache, WAIT_COUNT, int.class);
    }

    public int getFoundryMachineCount(Map<String, Long> assets) {
        int count = (int) (assets.get(Constants.BALANCE) / 1000000000000L);
        if (count < 1) return 1;
        return count;
    }

}
