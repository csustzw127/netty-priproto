package com.zw.netty.pri.codec;

import java.io.IOException;
import java.util.Map.Entry;

import org.jboss.marshalling.Marshaller;

import com.zw.netty.pri.marshalling.ChannelBufferByteOutput;
import com.zw.netty.pri.marshalling.MarshallingCodecFactory;
import com.zw.netty.pri.message.NettyMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
/**
 * 编码需要把NettyMessage中的属性写入ByteBuf中
 * 对于基本属性可以直接写入，但是对象需要使用marshalling进行序列化
 * @author zhouwei
 *
 */
public final class NettyMessageEncoder extends MessageToByteEncoder<NettyMessage> {

	private MarshallingEncoder marshallEncoder;
	private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
	MarshallerProvider provider = MarshallingCodecFactory.getMarshallerProvider();
	
	public NettyMessageEncoder() {
		marshallEncoder = MarshallingCodecFactory.buildMarshallingEncoder();
	}
	
	public NettyMessageEncoder(MarshallingEncoder marshallEncoder) {
		super();
		this.marshallEncoder = marshallEncoder;
	}


	@Override
	protected void encode(ChannelHandlerContext ctx, NettyMessage msg,ByteBuf buf) throws Exception {
		
		if(msg == null || msg.getHeader() == null) 
			throw new RuntimeException("");
		
		// 写头部
//		ByteBuf buf = ctx.alloc().buffer();
		buf.writeInt(msg.getHeader().getCrcCode());
		buf.writeInt(msg.getHeader().getLength());
		buf.writeLong(msg.getHeader().getSessionID());
		buf.writeByte(msg.getHeader().getType());
		buf.writeByte(msg.getHeader().getPriority());
		if(msg.getHeader().getAttachment() == null)
			buf.writeInt(0);
		else 
			buf.writeInt(msg.getHeader().getAttachment().size());
		// 头部中的attachment
		for (Entry<String,Object> entry : msg.getHeader().getAttachment().entrySet()) {
			byte[] keyByte = entry.getKey().getBytes("UTF-8");
			// 写key的长度和byte数组
			buf.writeInt(keyByte.length);
			buf.writeBytes(entry.getKey().getBytes());
			
//			marshallEncoder.encode(ctx,entry.getValue(),buf);
			/**
			 * 不知道为什么书上是这么调用，按自己理解的写的话
			 * 是要将entry.getValue()序列化的字节数组写到buf中
			 * 写入buf中包含obj的字节长度和obj序列化的字节数组
			 */
			encode0(ctx, entry.getValue(), buf);
		}
		
		//写msg的body
		if(msg.getBody() != null) {
			encode0(ctx,msg.getBody(),buf);
		} else {
			buf.writeInt(0);
		}
		// crcCode = 4byte
		// length = 4byte 所以length=readbaleBytes-8?
		buf.setInt(4, buf.readableBytes() - 8);
		ctx.writeAndFlush(buf);
	}

	/**
	 * 将对象序列化并写入bytebuf，并在bytebuf的writeIndex写入4个字节的占位符，后面用序列化的对象字节长度代替
	 * @param ctx
	 * @param msg
	 * @param buf
	 * @throws Exception
	 * @throws IOException
	 */
	private void encode0(ChannelHandlerContext ctx, Object msg, ByteBuf buf) throws Exception, IOException {
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
