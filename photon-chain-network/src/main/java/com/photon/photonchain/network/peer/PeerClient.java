package com.photon.photonchain.network.peer;


import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.excutor.PeerClientExcutor;
import com.photon.photonchain.network.utils.NetWorkUtil;
import com.photon.photonchain.storage.repository.NodeAddressRepository;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static com.photon.photonchain.network.utils.NetWorkUtil.bytesToInt;

/**
 * @author PTN
 * <p>
 * Created by PTN on 2017/12/26.
 */
@Component
public class PeerClient {

    private Logger logger = LoggerFactory.getLogger(PeerClient.class);

    @Autowired
    private PeerClientExcutor peerClientExcutor;

    @Autowired
    private PeerClientInitializer peerClientInitializer;

    @Autowired
    NodeAddressRepository nodeAddressRepository;

    @Autowired
    InitializationManager initializationManager;

    private final Integer PORT = 1906;

    private void connetServer(InetAddress host, Integer port) {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(peerClientInitializer);
        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().await();
        } catch (Exception e) {
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }


    public void poolsConnect(int peer) {
        peerClientExcutor.execute(() -> {
            try {
                connetServer(NetWorkUtil.convertAddress(peer), PORT);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });
    }

    public void init() {
        List<String> nodeAddressList = initializationManager.getCloneNodeList();
        nodeAddressList.forEach(nodeAddress -> {
            poolsConnect(bytesToInt(Hex.decode(nodeAddress)));
        });
    }
}
