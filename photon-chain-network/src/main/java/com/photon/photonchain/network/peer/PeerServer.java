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

/**
 * @Author PTN
 * Created by PTN on 2017/12/24.
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
        peerServerExcutor.execute(() -> {
            ServerBootstrap bootstrap = new ServerBootstrap();
            EventLoopGroup bossLoopGroup = new NioEventLoopGroup();
            EventLoopGroup childLoopGroup = new NioEventLoopGroup();
            bootstrap.group(bossLoopGroup, childLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(peerServerInitializer)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);
            try {
                ChannelFuture channelFuture = bootstrap.bind(port).sync();
                channelFuture.channel().closeFuture().await();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                childLoopGroup.shutdownGracefully();
                bossLoopGroup.shutdownGracefully();
            }
        });
    }
}
