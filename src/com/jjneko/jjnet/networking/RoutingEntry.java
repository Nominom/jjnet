package com.jjneko.jjnet.networking;

import com.jjneko.jjnet.networking.pipes.Pipe;

public class RoutingEntry {
	
	public long lastPacketTime=0;
	public int lowestCost=Integer.MAX_VALUE, altCost=Integer.MAX_VALUE;
	public Pipe lowestHop=null, altHop=null;
}
