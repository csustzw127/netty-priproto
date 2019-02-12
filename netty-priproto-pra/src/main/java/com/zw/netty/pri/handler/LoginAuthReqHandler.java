package com.zw.netty.pri.handler;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zw.netty.pri.enumrate.MessageType;
import com.zw.netty.pri.message.Header;
import com.zw.netty.pri.message.NettyMessage;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 校验ip白名单和是否重复登录
 * @author zhouwei
 *
 */
public class LoginAuthReqHandler extends ChannelHandlerAdapter{

	private static Logger logger = LoggerFactory.getLogger(LoginAuthReqHandler.class);
	
	// 保存已登录的ip地址
	private Map<String,Boolean> nodeCheck = new ConcurrentHashMap<String,Boolean>();
	// ip白名单
	private String[] whiteList = {"127.0.0.1"};
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		NettyMessage nettyMsg = (NettyMessage)msg;
		if(nettyMsg.getHeader().getType() == MessageType.LOGIN_REQ.value()) {
			String ip = ((InetSocketAddress)ctx.channel().remoteAddress()).getHostString();
			NettyMessage resp;
			if(nodeCheck.containsKey(ip)) {
				// 重复登录，拒绝
				resp = buildResp((byte) -1);
				ctx.writeAndFlush(resp);
			} else {
				// 白名单校验
				boolean inWhite = false;
				for(String whiteip : whiteList) {
					
					if(whiteip.equals(ip)) {
						inWhite = true;
						break;
					}
						
				}
				
				resp = inWhite ? buildResp((byte) 0) : buildResp((byte) -1);
				
				if(inWhite) {
					nodeCheck.put(ip,true);
				}
				
				ctx.writeAndFlush(resp);
				
				logger.info("校验ip："+ip+"请求登录结果："+inWhite);
			}
			
		} else {
			ctx.fireChannelRead(msg);
		}
	}


	private NettyMessage buildResp(byte b) {
		NettyMessage msg = new NettyMessage();
		Header h = new Header();
		h.setType(MessageType.LOGIN_RESP.value());
		msg.setHeader(h);
		msg.setBody(b);
		return msg;
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String ip = ((InetSocketAddress)ctx.channel().remoteAddress()).getHostString();
		// 删除登录节点
		nodeCheck.remove(ip);
		
		logger.info("移除节点："+ip);
		
		super.exceptionCaught(ctx, cause);
		
	}

}
