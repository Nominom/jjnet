package com.jjneko.jjnet.networking.pipes.http;

import java.util.LinkedList;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.jjneko.jjnet.networking.EndPoint;
import com.jjneko.jjnet.networking.http.server.WebSocketHttpServerHandler;
import com.jjneko.jjnet.networking.pipes.Pipe;

public class SimpleHttpServerPipe extends Pipe{
	

	private final LinkedList<String> queue = new LinkedList<String>();
	public ChannelHandlerContext channel;
	
	public SimpleHttpServerPipe(EndPoint endpoint, ChannelHandlerContext channel) {
		super(endpoint, new Runnable(){
			@Override
			public void run() {
				
			}
		});
		
		sendKeepAlive=true;
	}

	@Override
	public void send(String message) {
		if(!channel.channel().isActive()){
			//TODO Disconnect pipe
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
	
	public void queuePacket(String packet){
		queue.add(packet);
	}

	@Override
	public void sendKeepAlive() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		channel.close();
		WebSocketHttpServerHandler.pipes.remove(channel.channel().id().asShortText());
	}

}
