package com.jjneko.jjnet.networking;

import static com.jjneko.jjnet.networking.JJnet.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jjneko.jjnet.messaging.XML;
import com.jjneko.jjnet.networking.discovery.Advertisement;
import com.jjneko.jjnet.networking.pipes.Pipe;
import com.jjneko.jjnet.utils.JJNetUtils;

class MessageHandler implements Runnable {
	
	Logger logger = Logger.getLogger(MessageHandler.class.getName());
	
	/*TODO A LOT of optimizing*/
	
	byte[] buffer = new byte[BUFFER_LENGTH];
	@Override
	public void run() {
		logger.setLevel(Level.FINEST);
		while(true){
			try{
				Thread.sleep(100);
			}catch(Exception ex){}
			/* TODO Add data transfer cap or something maybe idk */
			
			// Loop pipes
			for(Pipe p : pipes){
				if(p.isConnected() && !p.isEmpty()){
					try{
						byte[] message = p.receive();
						if(logger.getLevel().equals(Level.FINEST)){
							logger.finest("msg:"+p.getIPAddress()+":"+message);
							System.err.println("msg:"+p.getIPAddress()+":"+new String(message,"ISO-8859-1"));
						}
						byte proto = message[0];
						Protocol protocol = Protocol.fromByte(proto);
						
						
						
						if(protocol==Protocol.ARP){
							int classNameLength = JJNetUtils.byteArrayToInt(message,1);
							int limit = JJNetUtils.byteArrayToInt(message, 5);
							String className = new String(Arrays.copyOfRange(message, 9, classNameLength+9), "ISO-8859-1");
							
							List<Advertisement> ads = adService.fetchLocal(className,limit);
							
							int rlength=0;
							for(Advertisement ad : ads){
								byte[] adString = XML.toUnsignedXML(ad).getBytes("UTF-8");
								int adlength = adString.length;
								System.arraycopy(JJNetUtils.intToByteArray(adlength), 0, buffer, rlength, 4);
								System.arraycopy(adString, 0, buffer, rlength+4, adlength);
								rlength+=4+adlength;
							}
							
							byte [] responseComp = JJNetUtils.compress(buffer,0,rlength);
							byte [] responseComp2 = new byte[responseComp.length+5];
							System.arraycopy(JJNetUtils.intToByteArray(ads.size()), 0, responseComp2, 1, 4);
							System.arraycopy(responseComp, 0, responseComp2, 5, responseComp.length);
							responseComp2[0]=Protocol.ARRP.value();
							p.send(responseComp2);
						}else if(protocol==Protocol.ARRP){
							int offset=0;
							int adslength = JJNetUtils.byteArrayToInt(message,1);
							byte [] responseBytes = JJNetUtils.decompress(message,5);
							int times=0;
							for(int i=0;i<adslength;i++){
								times++;
								int responseLength = JJNetUtils.byteArrayToInt(responseBytes,offset);
								offset+=4;
								String response = new String(Arrays.copyOfRange(responseBytes, offset, responseLength+offset), "UTF-8");
								Advertisement ad = (Advertisement) XML.parseUnsignedXML(response);
								offset+=responseLength;
								adService.add(ad);
							}
						}else if(protocol==Protocol.PMP){
							/*TODO Peer multicast protocol*/
						}else if(protocol==Protocol.PLRP){
							pls.processMessage(p, message);
						}else if(protocol==Protocol.PLRRP){
							if(plb!=null && receivingPeerList && plb.p==p){
								plb.processMessage(message);
							}else{
								byte[] response = new byte[2];
								response[0] = Protocol.PLRP.value();
								response[1] = PeerListBuilder.PLB_STOP;
								p.send(response);
							}
						}
						
					}catch(Exception ex){
						logger.severe(ex.getMessage());
						ex.printStackTrace();
					}
				}
			}
			//Loop pipes end
			
			
		}
	}

}
