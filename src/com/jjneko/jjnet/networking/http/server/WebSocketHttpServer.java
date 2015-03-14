package com.jjneko.jjnet.networking.http.server;

import java.net.InetSocketAddress;

import com.jjneko.jjnet.utils.JJNetUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * A HTTP server which serves Web Socket requests at the hosts ip address
 */
public final class WebSocketHttpServer{
	
	private int port;
	private final boolean SSL;
	private boolean started=false;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel channel;
	
	public WebSocketHttpServer(int port){
		this.port=port;
		this.SSL=Boolean.parseBoolean(System.getProperty("jjneko.jjnet.http.useSSL", "false"));
        WebSocketHttpServerHandler.setSSL(SSL);
	}
	
	/**
	 * Starts the http server
	 * 
	 * @throws Exception when something goes wrong, throw exceptions!
	 */
	public void start() throws Exception{
		if(started){
			throw new Exception("Http server already running!");
		}
		
		final SslContext ssl;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            ssl = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } else {
            ssl = null;
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(Integer.parseInt(System.getProperty("jjneko.jjnet.http.server.WorkerCount", "1")));
        
        ServerBootstrap boostrap = new ServerBootstrap();
        boostrap.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new WebSocketHttpServerInitializer(ssl));
        
        
        LogLevel logLevel=LogLevel.ERROR;
        String logLevelString = System.getProperty("jjneko.jjnet.http.server.logLevel", "ERROR");
        if(logLevelString.equals("INFO"))
        	logLevel=LogLevel.INFO;
        else if(logLevelString.equals("WARN"))
        	logLevel=LogLevel.WARN;
        else if(logLevelString.equals("DEBUG"))
        	logLevel=LogLevel.DEBUG;
        else if(logLevelString.equals("ERROR"))
        	logLevel=LogLevel.ERROR;
        else if(logLevelString.equals("TRACE"))
        	logLevel=LogLevel.TRACE;
        
        boostrap.handler(new LoggingHandler(logLevel));

        System.err.println("HTTPSERVER - creating channel");
        if(port==-1)
        	channel = boostrap.bind(JJNetUtils.getRandomAvailablePort()).sync().channel();
        else
        	channel = boostrap.bind(port).sync().channel();
        this.port=((InetSocketAddress)channel.localAddress()).getPort();
        System.err.println("HTTPSERVER - channel created");
        started=true;
	}
	
	
	/**
	 * Stops the http server
	 * 
	 * @throws Exception when something goes wrong, throw exceptions!
	 */
	public void stop() throws Exception{
		channel.close();
		channel.closeFuture().sync();
		bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
	}
	
	
	/**
	 * @return returns the port in which the http server is running
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return returns whether the http server is using SSL or not
	 */
	public boolean isSSL() {
		return SSL;
	}
	
	/**
	 * @return returns whether the http server is already running or not
	 */
	public boolean isStarted() {
		return started;
	}
	

}