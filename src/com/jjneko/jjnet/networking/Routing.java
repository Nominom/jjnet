package com.jjneko.jjnet.networking;

import java.util.HashMap;

import com.jjneko.jjnet.networking.pipes.Pipe;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import static com.jjneko.jjnet.networking.JJnet.*;

class Routing {
	
	public static final int PACKET_TTL=10000;
	
	static private TLongObjectHashMap<PacketInfo> routed = new TLongObjectHashMap<PacketInfo>();
	static private THashMap<String, RoutingEntry> routingtable = new THashMap<String, RoutingEntry>();
	
	
//	public void receive(Peer lasthop, Packet p){
//		if(dead)
//			return;
//		long timeTook = System.currentTimeMillis()-p.sentAt;
//		if(!routed.containsKey(p.id)){
//			if(!routingtbl.containsKey(p.orig)){
//				routingtbl.put(p.orig, lasthop);
//				routingtblttl.put(p.orig, timeTook);
//			}else if(timeTook < routingtblttl.get(p.orig)){
//				routingtbl.put(p.orig, lasthop);
//				routingtblttl.put(p.orig, timeTook);
//			}
//			routingtblaliveuntil.put(p.orig, System.currentTimeMillis());
//		}
//		
//			if(p.dest.equals(addr) && !routed.containsKey(p.id)){
//	//			System.out.println(addr+" received packet! "+p.id);
//				if(!p.ack && p.tcp){
//					Packet p2=new Packet(p.orig, addr, 200,true,false,false);
//					p2.ackid=p.id;
//					forwardImpl(p2,lasthop);
//				}else{
//					
//				}
//			}if(p.dest.equals(addr)){
//				return;
//			}else{
//				
//				forward(lasthop,p);
//			}
//		
//	}
	
	/**
	 * 
	 * @param lastHop
	 * @param source
	 * @param dest
	 * @param protocol
	 * @param timestamp
	 * @param packetId
	 * @param msg
	 * @return
	 */
	
	public static boolean Route(Pipe lastHop, String source, String dest, Protocol protocol, Long timestamp, int packetId, byte[] msg){
		long timeTook = JJnet.currentTimeMillis()-timestamp;
		if(timeTook>PACKET_TTL){
			MessageHandler.logger.finest("Message timed out");
			return false;
		}
		
		if(!routed.containsKey(packetId)){
			if(!routingtable.containsKey(source)){
				RoutingEntry rentr = new RoutingEntry();
				rentr.lowestCost=(int)timeTook;
				rentr.lowestHop=lastHop;
				rentr.lastPacketTime=JJnet.currentTimeMillis();
				routingtable.put(source, rentr);
			}else{
				RoutingEntry rentr = routingtable.get(source);
				rentr.lastPacketTime=JJnet.currentTimeMillis();
				if(timeTook < rentr.altCost){
					if(timeTook < rentr.lowestCost){
						rentr.lowestCost=(int)timeTook;
						rentr.lowestHop=lastHop;
					}else{
						rentr.altCost=(int)timeTook;
						rentr.altHop=lastHop;
					}
				}
			}
		}
		
		if(protocol==Protocol.PWGMP){
			if(routed.containsKey(packetId)){
				return false;
			}else{
				for(Pipe p : pipes){
					if(p.isConnected() && p!=lastHop){
						forward(p,msg);
					}
				}
				PacketInfo info = new PacketInfo();
				info.timeStamp=JJnet.currentTimeMillis();
				info.lastHop=lastHop;
				routed.putIfAbsent(packetId, info);
				return true;
			}
		}
		
//		else if(p.ack){
//			Peer nexthop = null;
//			nexthop = routed.get(p.ackid)[0];
//			if(nexthop==null){
//				nexthop=routingtbl.get(p.dest);
//			}
//			if(nexthop==null || nexthop==lasthop || nexthop.dead)
//		    	nexthop=neighbors.get(rand.nextInt(neighbors.size()));
//			
//			forwardImpl(p,nexthop);
//		}else{
//			Peer nexthop=null;
//			
//			if(routingtblaliveuntil.containsKey(p.dest)){
//				if((System.currentTimeMillis())>routingtblaliveuntil.get(p.dest)+Peer.timetolive){
//					routingtblaliveuntil.remove(p.dest);
//					routingtbl.remove(p.dest);
//					routingtblttl.remove(p.dest);
//					System.out.println("Route expired!");
//				}
//			}
//			
//			nexthop=routingtbl.get(p.dest);
//			
//		    if(nexthop==null || nexthop==lasthop || rand.nextInt(100)==0)
//		    	nexthop=neighbors.get(rand.nextInt(neighbors.size()));
//		    
//		    if(routed.containsKey(p.id)){
//		    	int index = rand.nextInt(neighbors.size());
//		    	if(index == neighbors.indexOf(lasthop))
//		    		index++;
//		    	if(index==neighbors.size())
//		    		index=0;
//		    	nexthop=neighbors.get(index);
//		    	if(routedpasscount.get(p.id)>neighbors.size() && routingtbl.containsKey(p.dest)){
//		    		return;
//		    	}
//		    }
//		    
//		    
//		    
//		    forwardImpl(p,nexthop);
//			if(p.tcp){
//				routed.putIfAbsent(p.id, new Peer[]{lasthop,nexthop});
//				Integer i = routedpasscount.get(p.id);
//				if(i==null)i=0;
//				routedpasscount.put(p.id, i+1);
//			}
//		}
		
		return false;
		
	}
	
	public static void forward(Pipe nextHop, byte[] msg){
		nextHop.send(msg);
	}
}
