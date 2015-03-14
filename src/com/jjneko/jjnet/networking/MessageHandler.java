package com.jjneko.jjnet.networking;

import static com.jjneko.jjnet.networking.JJnet.*;

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
	

	@Override
	public void run() {
		logger.setLevel(Level.FINEST);
		while(true){
			try{
				Thread.sleep(1000);
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
							int classNameLength = JJNetUtils.byteArrayToInt(Arrays.copyOfRange(message, 1, 5));
							String className = new String(Arrays.copyOfRange(message, 5, classNameLength+5), "ISO-8859-1");
							int limit = (int)message[classNameLength+5];
							
							List<Advertisement> ads = adService.fetchLocal(className,limit);
							
							for(Advertisement ad : ads){
								String adString = XML.toUnsignedXML(ad);
								int adlength = adString.length();
								String response = Protocol.ARRP.toChar()+
										new String(JJNetUtils.intToByteArray(adlength),"ISO-8859-1")+adString;
								
								System.out.println("Response:"+response);
								
								p.send(response.getBytes("ISO-8859-1"));
							}
						}else if(protocol==Protocol.ARRP){
							int responseLength = JJNetUtils.byteArrayToInt(Arrays.copyOfRange(message, 1, 5));
							String response = new String(Arrays.copyOfRange(message, 5, responseLength+5), "ISO-8859-1");
							Advertisement ad = (Advertisement) XML.parseUnsignedXML(response);
							adService.add(ad);
						}else if(protocol==Protocol.PMP){
							
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
