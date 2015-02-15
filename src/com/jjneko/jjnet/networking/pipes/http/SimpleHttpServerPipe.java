package com.jjneko.jjnet.networking.pipes.http;

import java.net.InetSocketAddress;
import java.util.LinkedList;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.jjneko.jjnet.networking.EndPoint;
import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.http.server.WebSocketHttpServerHandler;
import com.jjneko.jjnet.networking.pipes.Pipe;

public class SimpleHttpServerPipe extends Pipe{

	public ChannelHandlerContext channel;
	
	public SimpleHttpServerPipe(EndPoint endpoint, ChannelHandlerContext channel) {
		super(endpoint, null);
		this.channel=channel;
		handshake = new SimpleHttpServerPipeInitializer(this);
		sendKeepAlive=false;
	}

	@Override
	public void send(String message) {
		if(!channel.channel().isActive()){
			close();
			return;
		}
		WebSocketFrame frame = new TextWebSocketFrame(message);
		channel.channel().writeAndFlush(frame);
	}

	@Override
	public String receive(){
		if(connected && !queue.isEmpty())
			return queue.remove();
		else return null;
	}
	
	public String receiveHandshake(){
		if(!queue.isEmpty())
			return queue.remove();
		return null;
	}

	@Override
	public void sendKeepAlive() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		channel.close();
		WebSocketHttpServerHandler.pipes.remove(channel.channel().id().asShortText());
		JJnet.removePipe(this);
	}

	@Override
	public String getIPAddress() {
		return ((InetSocketAddress)channel.channel().remoteAddress()).getAddress().getHostAddress();
	}

}
