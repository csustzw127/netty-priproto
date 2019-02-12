package com.zw.netty.pri.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zw.netty.pri.enumrate.MessageType;
import com.zw.netty.pri.message.Header;
import com.zw.netty.pri.message.NettyMessage;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 当channel状态为inactive时发送连接请求
 * 并判断请求连接结果
 * @author zhouwei
 *
 */
public class LoginAuthRespHandler extends ChannelHandlerAdapter{

	private static Logger logger = LoggerFactory.getLogger(LoginAuthRespHandler.class);
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(buildAuthReq());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		NettyMessage nettyMsg = (NettyMessage)msg;
		if(nettyMsg.getHeader().getType() == MessageType.LOGIN_RESP.value()) {
			byte loginRes = (byte)nettyMsg.getBody();
			if(loginRes != (byte)0) {
				// 登录失败
				ctx.close();
				logger.error("登录失败，关闭连接。");
			} else {
				logger.info("登录成功 ： " + nettyMsg);
				// 这里为什么还要把消息往下扔不是处理完了登录返回信息的
				ctx.fireChannelRead(msg);
			}
		} else {
			ctx.fireChannelRead(msg);
		}
	}


	private NettyMessage buildAuthReq() {
		NettyMessage msg = new  NettyMessage();
		Header h = new Header();
		h.setType(MessageType.LOGIN_REQ.value());
		msg.setHeader(h);
		return msg;
	}



	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}

}
