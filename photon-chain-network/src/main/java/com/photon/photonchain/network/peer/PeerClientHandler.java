package com.photon.photonchain.network.peer;


import com.photon.photonchain.network.core.MessageProcessor;
import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.network.proto.InesvMessage;
import com.photon.photonchain.network.proto.MessageManager;
import com.photon.photonchain.network.utils.NetWorkUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.PUSH_MAC;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.RESPONSE;

/**
 * @author Wu
 * Created by SKINK on 2017/12/24.
 */
@ChannelHandler.Sharable
@Component
public class PeerClientHandler extends SimpleChannelInboundHandler<InesvMessage.Message> {

    private static Logger logger = LoggerFactory.getLogger(PeerClientHandler.class);

    @Autowired
    NioSocketChannelManager nioSocketChannelManager;

    @Autowired
    MessageProcessor messageProcessor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InesvMessage.Message msg) throws Exception {
        switch (msg.getMessageType()) {
            case REQUEST:
                messageProcessor.requestProcessor(ctx, msg);
                break;
            case RESPONSE:
                messageProcessor.responseProcessor(ctx, msg);
                break;
            default:
                break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info(ctx.channel().remoteAddress().toString() + "Peer channel active");
        InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(RESPONSE, PUSH_MAC);
        builder.setMac(NetWorkUtil.getMACAddress());
        ctx.writeAndFlush(builder.build());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }
}
