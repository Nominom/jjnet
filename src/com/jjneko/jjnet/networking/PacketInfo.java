package com.jjneko.jjnet.networking;

import com.jjneko.jjnet.networking.pipes.Pipe;

public class PacketInfo {
	
	public long timeStamp;
	public Pipe lastHop;
	public Pipe nextHop;
	public int passCount;

}
