package com.jjneko.jjnet.networking.pipes.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.net.InetAddress;
import java.net.URI;

import com.jjneko.jjnet.messaging.XML;
import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.Protocol;
import com.jjneko.jjnet.networking.http.client.WebSocketClientHandler;

public class SimpleHttpClientPipeInitializer implements Runnable{
	
	private static final long HANDSHAKE_TIMEOUT=5000;

	InetAddress address;
	int serverPort;
	SimpleHttpClientPipe pipe;
	
	public SimpleHttpClientPipeInitializer(SimpleHttpClientPipe pipe, InetAddress address, int serverPort) {
		this.pipe = pipe;
		this.address=address;
		this.serverPort=serverPort;
	}

	@Override
	public void run() {
		try{
			final int port;
			
			URI uri = new URI("ws://"+address.getHostAddress()+":"+serverPort+"/websocket");
			System.out.println(uri.toString());
			
	        String scheme = uri.getScheme() == null? "http" : uri.getScheme();
	        final String host = uri.getHost() == null? address.getHostAddress(): uri.getHost();
	        if (uri.getPort() == -1) {
	            if ("http".equalsIgnoreCase(scheme)) {
	                port = 80;
	            } else if ("https".equalsIgnoreCase(scheme)) {
	                port = 443;
	            } else {
	                port = -1;
	            }
	        } else {
	            port = uri.getPort();
	        }
	
	        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
	            System.err.println("Only WS(S) is supported.");
	            return;
	        }
	
	        final boolean ssl = "wss".equalsIgnoreCase(scheme);
	        final SslContext sslCtx;
	        if (ssl) {
	            sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
	        } else {
	            sslCtx = null;
	        }
	
	        pipe.group = new NioEventLoopGroup();
        
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            final WebSocketClientHandler handler =
                    new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()),pipe);

            pipe.clientBootstrap = new Bootstrap();
            pipe.clientBootstrap.group(pipe.group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                     }
                     p.addLast(
                             new HttpClientCodec(),
                             new HttpObjectAggregator(8192),
                             handler);
                 }
             });

            /* TODO better handshake '-' */
            pipe.ch = pipe.clientBootstrap.connect(uri.getHost(), port).sync().channel();
            handler.handshakeFuture().sync();
            pipe.send(Protocol.PRP.toChar()+"ping");
            long waitTime=0;
            while(pipe.isEmpty()){
            	try{
            		Thread.sleep(100);
            		waitTime+=100;
            		if(waitTime>HANDSHAKE_TIMEOUT){
            			pipe.setConnected(false);
            			pipe.close();
            			System.out.println("No response received in time");
            			return;
            		}
            	}catch(Exception ex){}
            }
            if(pipe.receiveHandshake().equals("pong"))
            	pipe.setConnected(true);
            else
            	pipe.close();
            
            System.out.println("connected: "+ pipe.isConnected());
    		JJnet.addPipe(pipe);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	
		
	}

}
