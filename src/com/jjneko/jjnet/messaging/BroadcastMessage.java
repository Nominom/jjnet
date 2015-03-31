package com.jjneko.jjnet.messaging;

import com.jjneko.jjnet.networking.EndPoint;

public class BroadcastMessage {
	long timestamp;
	EndPoint sentBy;
	byte[] message;
	public BroadcastMessage(long timestamp, EndPoint sentBy, byte[] message) {
		super();
		this.timestamp = timestamp;
		this.sentBy = sentBy;
		this.message = message;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public EndPoint getSentBy() {
		return sentBy;
	}
	public byte[] getMessage() {
		return message;
	}
}
