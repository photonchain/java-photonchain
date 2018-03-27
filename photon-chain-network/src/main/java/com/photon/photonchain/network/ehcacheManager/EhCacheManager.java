package com.photon.photonchain.network.ehcacheManager;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:17:21 2017/12/27
 * @Modified by:
 */
public class EhCacheManager {
    private static CacheManager cacheManager = CacheManager.newInstance();

    public static Cache getCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        return cache;
    }

    public static <T> T getCacheValue(Cache cache, String key, Class<T> tClass) {
        return (T) cache.get(key).getObjectValue();
    }

    public static <T> List<T> getAllCacheValue(Cache cache, Class<T> tClass) {
        List<T> cacheValueList = new ArrayList<T>();
        Collection<Element> elements = cache.getAll(cache.getKeys()).values();
        for (Element element : elements) {
            cacheValueList.add((T) element.getObjectValue());
        }
        return cacheValueList;
    }

    public static boolean existKey(Cache cache, String key) {
        return cache.get(key) != null;
    }

    public static void put(Cache cache, String key, Object o) {
        cache.put(new Element(key, o));
    }

    public static void remove(Cache cache, String key) {
        cache.remove(key);
    }
}
