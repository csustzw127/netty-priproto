package com.zw.netty.pri.handler;

import com.zw.netty.pri.message.Header;
import com.zw.netty.pri.message.NettyMessage;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class SimpleClientChannelHandler extends ChannelHandlerAdapter{

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		NettyMessage msg = new NettyMessage();
		Header header = new Header();
		header.setCrcCode(12);
		header.setPriority(new Byte("1"));
		header.setSessionID(12L);
		header.setType(new Byte("2"));
		Object b = new Object();
		msg.setHeader(header);
		ctx.writeAndFlush(msg);
		
		msg.getHeader().setSessionID(12345L);
		ctx.writeAndFlush(msg);
	}


}
