package com.photon.photonchain.network.ehcacheManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger logger = LoggerFactory.getLogger(NioSocketChannelManager.class);

    private Cache nioSocketChannelCache = EhCacheManager.getCache("nioSocketChannelCache");

    public void addNioSocketChannel(String mac, ChannelHandlerContext ctx) {
        if (!EhCacheManager.existKey(nioSocketChannelCache, mac)) {
            EhCacheManager.put(nioSocketChannelCache, mac, ctx);
        } else {
            EhCacheManager.remove(nioSocketChannelCache, mac.toString());
            EhCacheManager.put(nioSocketChannelCache, mac, ctx);
        }
    }

    public void closeNioSocketChannelByMac(String mac) {
        if (EhCacheManager.existKey(nioSocketChannelCache, mac)) {
            ChannelHandlerContext channelHandlerContext = EhCacheManager.getCacheValue(nioSocketChannelCache, mac, ChannelHandlerContext.class);
            channelHandlerContext.channel().closeFuture();
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
            Channel ctx = ((ChannelHandlerContext) nioSocketChannelCache.get(mac).getObjectValue()).channel();
            boolean isActive = ctx.isActive();
            if (!isActive) {
                EhCacheManager.remove(nioSocketChannelCache, mac.toString());
            }
        });
    }

    /**
     * 移除指定的mac地址
     */
    public void removeTheMac(String mac) {
        EhCacheManager.remove(nioSocketChannelCache, mac);
        this.closeNioSocketChannelByMac(mac);
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