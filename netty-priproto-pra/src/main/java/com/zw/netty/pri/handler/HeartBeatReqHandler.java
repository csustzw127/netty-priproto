package com.zw.netty.pri.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zw.netty.pri.enumrate.MessageType;
import com.zw.netty.pri.message.Header;
import com.zw.netty.pri.message.NettyMessage;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatReqHandler extends ChannelHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HeartBeatReqHandler.class);
	
	private volatile ScheduledFuture<?> heartBeat;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		NettyMessage nettyMsg = (NettyMessage)msg;
		if(nettyMsg.getHeader() != null && nettyMsg.getHeader().getType() == MessageType.LOGIN_RESP.value()) {
			heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 0, 5000, TimeUnit.MILLISECONDS);
		} else if(nettyMsg.getHeader() != null && nettyMsg.getHeader().getType() == MessageType.HEARTBEAT_RESP.value()) {
			logger.info("recieved server's heartbeat message");
		} else {
			ctx.fireChannelRead(msg);
		}
	}
	
	private class HeartBeatTask implements Runnable {

		private final ChannelHandlerContext ctx;
		
		HeartBeatTask(final ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}
		
		@Override
		public void run() {
			ctx.writeAndFlush(buildHeartBeat());
			logger.info("client send heartbeat req to server ");
		}
		
		private NettyMessage buildHeartBeat() {
			NettyMessage msg = new NettyMessage();
			Header header = new Header();
			header.setType(MessageType.HEARTBEAT_REQ.value());
			msg.setHeader(header);
			return msg;
		}
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if(heartBeat != null) {
			heartBeat.cancel(true);
			heartBeat = null;
		}
		ctx.fireExceptionCaught(cause);
	}
	
	
}

