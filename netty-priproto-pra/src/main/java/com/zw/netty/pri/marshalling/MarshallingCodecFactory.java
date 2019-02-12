package com.zw.netty.pri.marshalling;

import java.io.IOException;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;

public class MarshallingCodecFactory {
	
	
	
	public static MarshallingDecoder buildMarshallingDecoder() {
		final MarshallerFactory marshallingFactory = 
				Marshalling.getProvidedMarshallerFactory("serial");
		final MarshallingConfiguration c = new MarshallingConfiguration();
		c.setVersion(5);
		UnmarshallerProvider p = new DefaultUnmarshallerProvider(marshallingFactory, c);
		MarshallingDecoder decoder = new MarshallingDecoder(p,1024);
		return decoder;
	}
	
	public static MarshallingEncoder buildMarshallingEncoder() {
		final MarshallerFactory marshallingFactory = 
				Marshalling.getProvidedMarshallerFactory("serial");
		final MarshallingConfiguration c = new MarshallingConfiguration();
		c.setVersion(5);
		MarshallerProvider p = new DefaultMarshallerProvider(marshallingFactory, c);
		MarshallingEncoder encoder = new MarshallingEncoder(p);
		return encoder;
	}
	
	public static MarshallerProvider getMarshallerProvider() {
		final MarshallerFactory marshallingFactory = 
				Marshalling.getProvidedMarshallerFactory("serial");
		final MarshallingConfiguration c = new MarshallingConfiguration();
		c.setVersion(5);
		MarshallerProvider p = new DefaultMarshallerProvider(marshallingFactory, c);
		
		return p;
	}
	
	public static Unmarshaller buildUnMarshalling() throws IOException {
		final MarshallerFactory marshallerFactory = Marshalling
			.getProvidedMarshallerFactory("serial");
		final MarshallingConfiguration configuration = new MarshallingConfiguration();
		configuration.setVersion(5);
		final Unmarshaller unmarshaller = marshallerFactory
			.createUnmarshaller(configuration);
		return unmarshaller;
	    }
}
