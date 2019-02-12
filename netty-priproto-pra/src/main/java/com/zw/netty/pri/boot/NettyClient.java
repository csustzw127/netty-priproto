package com.zw.netty.pri.boot;

import java.net.InetSocketAddress;

import com.zw.netty.pri.codec.NettyMessageDecoder;
import com.zw.netty.pri.codec.NettyMessageEncoder;
import com.zw.netty.pri.handler.HeartBeatReqHandler;
import com.zw.netty.pri.handler.LoginAuthRespHandler;
import com.zw.netty.pri.handler.SimpleChannelHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

	private EventLoopGroup group;
	private Bootstrap clientStrap;
	
	public static void main(String[] args) {
		new NettyClient().start();
	}
	
	public void start() {
		clientStrap = new Bootstrap();
		group = new NioEventLoopGroup();
		clientStrap.group(group)
				   .channel(NioSocketChannel.class)
				   .handler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(new NettyMessageEncoder())
						 .addLast(new NettyMessageDecoder(2048,4,4))
						 .addLast(new LoginAuthRespHandler())
//						 .addLast(new SimpleChannelHandler())
						 .addLast(new HeartBeatReqHandler());
					}
					   
				});
		ChannelFuture f = clientStrap.connect(new InetSocketAddress("127.0.0.1",8081)).syncUninterruptibly();
		f.channel().closeFuture().syncUninterruptibly();
		group.shutdownGracefully();
	}
}
