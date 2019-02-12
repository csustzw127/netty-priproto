package com.zw.netty.pri.codec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.Unmarshaller;

import com.zw.netty.pri.marshalling.ChannelBufferByteInput;
import com.zw.netty.pri.marshalling.MarshallingCodecFactory;
import com.zw.netty.pri.message.Header;
import com.zw.netty.pri.message.NettyMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/**
 * 在nettyMessage的编码过后的帧中，代表消息长度的字节在第5到8字节，所以配偏移量0，长度4
 * @author zhouwei
 *
 */
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {

	private final Unmarshaller unmarshaller;
	
	/**
	 * 
	 * @param maxFrameLength
	 * @param lengthFieldOffset  帧长度字节的偏移量
	 * @param lengthFieldLength	 代表帧长度的字节长度
	 */
	public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
		try {
			unmarshaller = MarshallingCodecFactory.buildUnMarshalling();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf buf = (ByteBuf) super.decode(ctx, in);
		// 对于半包 buf == null,那lengthFieldBasedFrameDecoder怎么确定是半包的呢？没有数据包长度
		if(buf == null) return null;
		
		NettyMessage msg = new NettyMessage();
		// 设置header,按写的顺序读取
		Header h = new Header();
		h.setCrcCode(buf.readInt());
		h.setLength(buf.readInt());
		h.setSessionID(buf.readLong());
		h.setType(buf.readByte());
		h.setPriority(buf.readByte());
		  
		int attachSize = buf.readInt();
		if(attachSize > 0) {
			Map<String,Object> map = new HashMap<String,Object>();
			for(int i=0; i<attachSize; i++) {
				int attachLen = buf.readInt();
				byte[] strBytes = new byte[attachLen];
				buf.readBytes(strBytes);
				
				map.put(new String(strBytes,"UTF-8"), decode(buf));
			}
			
			h.setAttachment(map);
		}
		// 读取body
		if(buf.readableBytes() > 4) {
			msg.setBody(decode(buf));
		}
		msg.setHeader(h);
		return msg;
	}

	/**
	 * 解码buf中的attach的obj
	 * 前四个字节是obj字节数组的长度
	 * @param buf
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private Object decode(ByteBuf buf) throws IOException, ClassNotFoundException {
		int objLen = buf.readInt();
		// 拥有相同的字节内容，但是readerIndex和writerIndex是相互独立的
		// 使用copybuf读取字节，再将buf的readerIndex
		ByteBuf copyBuf = buf.slice(buf.readerIndex(), objLen);
		ByteInput input = new ChannelBufferByteInput(copyBuf);
		try {
		    unmarshaller.start(input);
		    Object obj = unmarshaller.readObject();
		    unmarshaller.finish();
		    buf.readerIndex(buf.readerIndex() + objLen);
		    return obj;
		} finally {
		    unmarshaller.close();
		}
	}
}
