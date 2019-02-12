package com.zw.netty.pri.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zw.netty.pri.enumrate.MessageType;
import com.zw.netty.pri.message.Header;
import com.zw.netty.pri.message.NettyMessage;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatRespHandler extends ChannelHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HeartBeatRespHandler.class);
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		NettyMessage nettyMsg = (NettyMessage)msg;
		if(nettyMsg.getHeader() != null && nettyMsg.getHeader().getType() == MessageType.HEARTBEAT_REQ.value()) {
			logger.info("recieved client's heartbeat message");
			ctx.writeAndFlush(buildHeartBeat());
		} else {
			ctx.fireChannelRead(msg);
		}
	}
	
	private NettyMessage buildHeartBeat() {
		NettyMessage msg = new NettyMessage();
		Header header = new Header();
		header.setType(MessageType.HEARTBEAT_RESP.value());
		msg.setHeader(header);
		return msg;
	}
	
}

