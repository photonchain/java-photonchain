package com.photon.photonchain.network.ehcacheManager;

import net.sf.ehcache.Cache;
import org.springframework.stereotype.Component;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:42 2018/1/12
 * @Modified by:
 */
@Component
public class FoundryMachineManager {
    private Cache foundryMachineCache = EhCacheManager.getCache("foundryMachineCache");

    public void setFoundryMachine(String pubKey, boolean isStart) {
        EhCacheManager.put(foundryMachineCache, pubKey, isStart);
    }

    public boolean foundryMachineIsStart(String pubKey) {
        boolean res = false;
        try {
            res =  EhCacheManager.getCacheValue(foundryMachineCache, pubKey, boolean.class);
        }catch (Exception e){
            System.out.println("foundryMachineIsStart exception ..");
        }
        return res;
    }
}
