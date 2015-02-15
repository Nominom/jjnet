package com.jjneko.jjnet.networking.pipes;

import java.util.LinkedList;
import java.util.Queue;

import com.jjneko.jjnet.networking.EndPoint;

public abstract class Pipe {
	
	protected final LinkedList<String> queue = new LinkedList<String>();
	public boolean sendKeepAlive=false;
	protected boolean connected=false;
	protected EndPoint endPoint;
	protected Runnable handshake;
	
	public Pipe(EndPoint endpoint, Runnable handshake){
		this.endPoint=endpoint;
		this.handshake=handshake;
	}
	
	public abstract void send(String message);
	
	public abstract String receive();
	
	public abstract void sendKeepAlive();
	
	public void connect(){
		new Thread(handshake).start();
	}
	
	public abstract void close();
	
	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(EndPoint endPoint) {
		this.endPoint = endPoint;
	}
	
	public int getQueueLength(){
		return queue.size();
	}
	
	public boolean isEmpty(){
		return queue.isEmpty();
	}
	public void queuePacket(String packet){
		queue.add(packet);
	}
	
	/**
	 * Example 122.45.33.45
	 * @return returns the pipe's ip address as a string
	 */
	public abstract String getIPAddress();
	

}
