package com.jjneko.jjnet.networking;

import com.jjneko.jjnet.messaging.BroadcastMessage;

public interface BroadcastListener {
	
	public abstract void processBroadcast(BroadcastMessage message);

}
