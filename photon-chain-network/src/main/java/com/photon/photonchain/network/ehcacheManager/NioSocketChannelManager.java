package com.photon.photonchain.network.ehcacheManager;

import io.netty.channel.ChannelHandlerContext;
import net.sf.ehcache.Cache;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:32 2017/12/28
 * @Modified by:
 */
@Component
public class NioSocketChannelManager {

    private Cache nioSocketChannelCache = EhCacheManager.getCache("nioSocketChannelCache");

    public void addNioSocketChannel(String mac, ChannelHandlerContext ctx) {
        if (!EhCacheManager.existKey(nioSocketChannelCache, mac)) {
            EhCacheManager.put(nioSocketChannelCache, mac, ctx);
        } else {
            EhCacheManager.remove(nioSocketChannelCache, mac.toString());
            EhCacheManager.put(nioSocketChannelCache, mac, ctx);
        }
    }

    public int getActiveNioSocketChannelCount() {
        removeInvalidChannel();
        List<ChannelHandlerContext> nioSocketChannelList = EhCacheManager.getAllCacheValue(nioSocketChannelCache, ChannelHandlerContext.class);
        return nioSocketChannelList.size();
    }

    public List<String> getChannelHostList() {
        List<String> hostList = new ArrayList<>();
        removeInvalidChannel();
        nioSocketChannelCache.getKeys().forEach(mac -> {
            hostList.add(mac.toString());
        });
        return hostList;
    }

    public void removeInvalidChannel() {
        nioSocketChannelCache.getKeys().forEach(mac -> {
            boolean isActive = ((ChannelHandlerContext) nioSocketChannelCache.get(mac).getObjectValue()).channel().isActive();
            if (!isActive) {
                EhCacheManager.remove(nioSocketChannelCache, mac.toString());
            }
        });
    }


    public void removeTheMac(String mac) {
        EhCacheManager.remove(nioSocketChannelCache, mac);
    }

    public void write(Object o) {
        removeInvalidChannel();
        List<ChannelHandlerContext> nioSocketChannelList = EhCacheManager.getAllCacheValue(nioSocketChannelCache, ChannelHandlerContext.class);
        for (ChannelHandlerContext ctx : nioSocketChannelList) {
            ctx.writeAndFlush(o);
        }
    }

    public void writeWithOutCtxList(Object o, List<String> hostList) {
        nioSocketChannelCache.getKeys().forEach(mac -> {
            ChannelHandlerContext ctx = (ChannelHandlerContext) nioSocketChannelCache.get(mac).getObjectValue();
            boolean isActive = ctx.channel().isActive();
            if (!isActive) {
                EhCacheManager.remove(nioSocketChannelCache, mac.toString());
            } else {
                if (!hostList.contains(mac)) {
                    ctx.writeAndFlush(o);
                }
            }
        });
    }
}