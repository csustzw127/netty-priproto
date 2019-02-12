package com.zw.netty.pri.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zw.netty.pri.codec.NettyMessageDecoder;
import com.zw.netty.pri.codec.NettyMessageEncoder;
import com.zw.netty.pri.handler.HeartBeatRespHandler;
import com.zw.netty.pri.handler.LoginAuthReqHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
	
	private static Logger logger = LoggerFactory.getLogger(NettyServer.class);
	// accept group
	private EventLoopGroup bossGroup;
	// 与client交互io事件的group
	private EventLoopGroup workGroup;
	
	private ServerBootstrap server;
	
	public static void main(String[] args) {
		new NettyServer().start();
	}
	public void start() {
		
		try {
			server = new ServerBootstrap();
			bossGroup = new NioEventLoopGroup();
			workGroup = new NioEventLoopGroup();
			server.group(bossGroup,workGroup)
				  .channel(NioServerSocketChannel.class)
				  .childHandler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(new NettyMessageDecoder(2048,4,4))
									 .addLast(new NettyMessageEncoder())
									 .addLast(new LoginAuthReqHandler())
									 .addLast(new HeartBeatRespHandler());
//									 .addLast(new SimpleChannelHandler());
					}
					  
				  });
			ChannelFuture f = server.bind(8081).sync();
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
		
	}
}
