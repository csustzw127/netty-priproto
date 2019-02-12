package com.zw.pri.coder.unittest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

import org.jboss.marshalling.Marshaller;
import org.junit.Test;

import com.zw.netty.pri.codec.NettyMessageDecoder;
import com.zw.netty.pri.codec.NettyMessageEncoder;
import com.zw.netty.pri.marshalling.ChannelBufferByteOutput;
import com.zw.netty.pri.marshalling.MarshallingCodecFactory;
import com.zw.netty.pri.message.Header;
import com.zw.netty.pri.message.NettyMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.marshalling.MarshallerProvider;

public class CoderTest {
	private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
	MarshallerProvider provider = MarshallingCodecFactory.getMarshallerProvider();
	@Test
	public void test() throws IOException, Exception {
		
		EmbeddedChannel channel = new EmbeddedChannel(new NettyMessageDecoder(2048,1024,1024));
		
		NettyMessage msg = new NettyMessage();
		Header header = new Header();
		header.setCrcCode(12);
		header.setPriority(new Byte("1"));
		header.setSessionID(12L);
		header.setType(new Byte("2"));
		Object b = new Object();
		msg.setHeader(header);
//		msg.setBody(b);
		// 写头部
		ByteBuf buf = Unpooled.buffer();
		buf.writeInt(msg.getHeader().getCrcCode());
		buf.writeInt(msg.getHeader().getLength());
		buf.writeLong(msg.getHeader().getSessionID());
		buf.writeByte(msg.getHeader().getType());
		buf.writeByte(msg.getHeader().getPriority());
//		buf.writeInt(msg.getHeader().getAttachment().size());
		buf.writeInt(0);
		// 头部中的attachment
		for (Entry<String, Object> entry : msg.getHeader().getAttachment().entrySet()) {
			byte[] keyByte = entry.getKey().getBytes("UTF-8");
			// 写key的长度和byte数组
			buf.writeInt(keyByte.length);
			buf.writeBytes(entry.getKey().getBytes());

			// marshallEncoder.encode(ctx,entry.getValue(),buf);
			/**
			 * 不知道为什么书上是这么调用，按自己理解的写的话 是要将entry.getValue()序列化的字节数组写到buf中
			 * 写入buf中包含obj的字节长度和obj序列化的字节数组
			 */
//			encode(ctx, entry.getValue(), buf);
		}

		// 写msg的body
		if (msg.getBody() != null) {
			 encode(null,msg.getBody(),buf);
		} else {
			buf.writeInt(0);
		}
		// crcCode = 4byte
		// length = 4byte 所以length=readbaleBytes-8?
		buf.setInt(4, buf.readableBytes() - 8);
		
		channel.writeInbound(msg);
		channel.finish();
		NettyMessage _msg = channel.readInbound();
		System.out.println(_msg);
	}
	
	@Test
	public void testCodec() {
		EmbeddedChannel channel = 
				new EmbeddedChannel(new NettyMessageEncoder()
								   ,new NettyMessageDecoder(2048,1024,1024));
		
		NettyMessage msg = new NettyMessage();
		Header header = new Header();
		header.setCrcCode(12);
		header.setPriority(new Byte("1"));
		header.setSessionID(12L);
		header.setType(new Byte("2"));
		Object b = new Object();
		msg.setHeader(header);
//		msg.setBody(b);
		// 写头部
		channel.writeInbound(msg);
		channel.finish();
		NettyMessage _msg = channel.readInbound();
		System.out.println(_msg);
	}
	
	private void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buf) throws Exception, IOException {
		Marshaller marshaller = provider.getMarshaller(ctx);
		try {
			int lengthPos = buf.writerIndex();
			buf.writeBytes(LENGTH_PLACEHOLDER);
			ChannelBufferByteOutput output = new ChannelBufferByteOutput(buf);
			marshaller.start(output);
			marshaller.writeObject(msg);
			marshaller.finish();
			buf.setInt(lengthPos, buf.writerIndex() - lengthPos - 4);
		} finally {
			marshaller.close();
		}
	}
}
