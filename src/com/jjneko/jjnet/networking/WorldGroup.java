package com.jjneko.jjnet.networking;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;

import com.jjneko.jjnet.messaging.BroadcastMessage;
import com.jjneko.jjnet.networking.discovery.WorldGroupAdvertisement;
import com.jjneko.jjnet.networking.security.SecurityService;
import com.jjneko.jjnet.utils.EndPointHashSet;
import com.jjneko.jjnet.utils.JJNetUtils;

public class WorldGroup extends PeerGroup{
	
	/*TODO Move member list to database? */
	public EndPointHashSet<EndPoint> members = new EndPointHashSet<EndPoint>();
	
	public WorldGroup(EndPoint owner) {
		super("world", owner, true);
		addMember(owner);
		addBroadCastListener(new WorldGroupBcastListener());
	}

	public WorldGroup(WorldGroupAdvertisement wgad) {
		super(wgad.name,wgad.owner,wgad.anonymous);
		addMember(owner);
		addBroadCastListener(new WorldGroupBcastListener());
	}

	@Override
	public Collection<EndPoint> getMemberList() {
		return members;
	}

	@Override
	public boolean containsMember(EndPoint e) {
		return members.contains(e);
	}

	@Override
	public void addMember(EndPoint e) {
		members.add(e);
	}

	public int getMemberCount() {
		return members.size();
	}
	
	@Override
	public void broadcast(byte[] msg){
		try{
		byte[] bcastmsg = new byte[1+								//Protocol
		                           SecurityService.PACKET_ID_LENGTH+//Packet id
		                           EndPoint.ENDPOINT_ADDRESS_LENGTH+//Source addr
		                           Long.BYTES+						//timestamp
		                           Integer.BYTES+					//Message length as int
		                           msg.length+						//Message
		                           SecurityService.CIPHER_LENGTH	//Signed hash of everything else
		                           ];
		bcastmsg[0]=Protocol.PWGMP.value();
		SecurityService.getRandomPacketID(bcastmsg, 1);
		System.arraycopy(JJnet.localEndPointAddress.getAddress().getBytes("ISO-8859-1"),
				0,
				bcastmsg,
				1+SecurityService.PACKET_ID_LENGTH,
				EndPoint.ENDPOINT_ADDRESS_LENGTH);
		JJNetUtils.longToBytes(JJnet.currentTimeMillis(),
				bcastmsg, 
				1+SecurityService.PACKET_ID_LENGTH+EndPoint.ENDPOINT_ADDRESS_LENGTH);
		JJNetUtils.intToByteArray(msg.length,
				bcastmsg, 
				1+SecurityService.PACKET_ID_LENGTH+EndPoint.ENDPOINT_ADDRESS_LENGTH+Long.BYTES);
		System.arraycopy(msg, 0, bcastmsg, 
				1+SecurityService.PACKET_ID_LENGTH+EndPoint.ENDPOINT_ADDRESS_LENGTH+Long.BYTES+Integer.BYTES,
				msg.length);
		byte[] sign = SecurityService.rsaEncrypt(SecurityService.hash(bcastmsg, 0,
				1+SecurityService.PACKET_ID_LENGTH+EndPoint.ENDPOINT_ADDRESS_LENGTH+Long.BYTES+Integer.BYTES+msg.length,
				SecurityService.HASH_MAX_LENGTH), JJnet.privateKey);
		System.arraycopy(sign, 0, bcastmsg,
				1+SecurityService.PACKET_ID_LENGTH+EndPoint.ENDPOINT_ADDRESS_LENGTH+Long.BYTES+Integer.BYTES+msg.length,
				SecurityService.CIPHER_LENGTH);
		
		Routing.Route(null, JJnet.localEndPointAddress.getAddress(), null, Protocol.PWGMP, JJnet.currentTimeMillis(), JJNetUtils.byteArrayToInt(bcastmsg,1), bcastmsg);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public EndPoint getMemberByAddr(String addr) {
		return (EndPoint) members.get(addr);
	}
	
	static class WorldGroupBcastListener implements BroadcastListener{
		@Override
		public void processBroadcast(BroadcastMessage message) {
			try{
			byte[] msg = message.getMessage();
			if(msg[0]==Protocol.NPP.value()){
				String keystring = new String(Arrays.copyOfRange(msg, 1, msg.length), "ISO-8859-1");
				PublicKey key = SecurityService.parsePublicKey(keystring);
				EndPoint newPoint= new EndPoint(key);
				JJnet.worldGroup.addMember(newPoint);
				System.out.println("New peer joined! "+newPoint.getAddress());
			}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
	}
	
	

}
