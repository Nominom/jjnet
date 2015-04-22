package com.jjneko.jjnet.networking;

import java.util.ArrayList;
import java.util.Collection;

import com.jjneko.jjnet.messaging.BroadcastMessage;
import com.jjneko.jjnet.networking.security.SecurityService;

public abstract class PeerGroup {
	
	public static final int ID_LENGTH=8;
	protected final String name;
	protected final String id;
	protected EndPoint owner;
	protected boolean anonymous;
	private ArrayList<BroadcastListener> bcastlisteners = new ArrayList<BroadcastListener>();
	
	public PeerGroup(String name, EndPoint owner, boolean anonymous){
		this.id=SecurityService.hashAsBase64(name+owner.getAddress(), ID_LENGTH);
		this.name=name;
		this.owner=owner;
		this.anonymous=anonymous;
	}
	
	public abstract Collection<EndPoint> getMemberList();
	
	public abstract boolean containsMember(EndPoint e);
	
	public abstract EndPoint getMemberByAddr(String addr);
	
	public abstract void addMember(EndPoint e);

	public EndPoint getOwner() {
		return owner;
	}

	public void setOwner(EndPoint owner) {
		this.owner = owner;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isAnonymous() {
		return anonymous;
	}
	
	public void broadcast(byte[] msg){
		//TODO Broadcasting in normal peergroups
//		try{
//		byte[] bcastbody = XML.toUnsignedXML(msg).getBytes("ISO-8859-1");
//		byte[] bcastmsg = new byte[1+								//Protocol
//		                           SecurityService.PACKET_ID_LENGTH+//Packet id
//		                           EndPoint.ENDPOINT_ADDRESS_LENGTH+//Source addr
//		                           Long.BYTES+						//timestamp
//		                           4+								//Message _length as int
//		                           bcastbody.length+				//Message
//		                           SecurityService.CIPHER_LENGTH	//Signed hash of everything else
//		                           ];
//		bcastmsg[0]=Protocol.PWGMP.value();
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
	}
	
	public void addBroadCastListener(BroadcastListener listener){
		bcastlisteners.add(listener);
	}
	
	void receiveBroadcast(BroadcastMessage message){
		for(BroadcastListener list : bcastlisteners){
			list.processBroadcast(message);
		}
	}

}
