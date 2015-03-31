package com.jjneko.jjnet.networking.pipes;

import java.util.LinkedList;
import java.util.Queue;

import com.jjneko.jjnet.networking.EndPoint;

public abstract class Pipe {
	
	protected final LinkedList<byte[]> queue = new LinkedList<byte[]>();
	public boolean sendKeepAlive=false;
	protected boolean connected=false;
	protected Runnable handshake;
	
	public Pipe(Runnable handshake){
		this.handshake=handshake;
	}

	public abstract void send(byte[] message);
	
	public abstract byte[] receive();
	
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
	
	public int getQueueLength(){
		return queue.size();
	}
	
	public boolean isEmpty(){
		return queue.isEmpty();
	}
	public void queuePacket(byte[] packet){
		queue.add(packet);
	}
	
	/**
	 * Example 122.45.33.45
	 * @return returns the pipe's ip address as a string
	 */
	public abstract String getIPAddress();
		

}
