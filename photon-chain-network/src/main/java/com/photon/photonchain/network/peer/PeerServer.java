package com.photon.photonchain.network.peer;


import com.photon.photonchain.network.excutor.PeerServerExcutor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**@Author wu
 * Created by SKINK on 2017/12/24.
 */
@Component
public class PeerServer {
    private Logger logger = LoggerFactory.getLogger(PeerServer.class);

    @Value("${peer.port}")
    private Integer port;

    @Autowired
    private PeerServerExcutor peerServerExcutor;

    @Autowired
    private PeerServerInitializer peerServerInitializer;

    @PostConstruct
    public void init() {

        port = port != 0 ? port : 1905;
        logger.info("Peer Server Init...");
        logger.info("Peer Port:[{}]", port);
        peerServerExcutor.execute(() -> {
            ServerBootstrap bootstrap = new ServerBootstrap();
            EventLoopGroup bossLoopGroup = new NioEventLoopGroup();
            EventLoopGroup childLoopGroup = new NioEventLoopGroup();
            bootstrap.group(bossLoopGroup, childLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(peerServerInitializer)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            try {
                ChannelFuture channelFuture = bootstrap.bind(port).sync();
                logger.info("Peer Server Startd...");
                channelFuture.channel().closeFuture().await();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Peer Server Got a Exception.");
            } finally {
                childLoopGroup.shutdownGracefully();
                bossLoopGroup.shutdownGracefully();
                logger.info("Peer Server ShutDown Success.");
            }
        });
    }
}
