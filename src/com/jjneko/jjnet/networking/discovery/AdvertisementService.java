package com.jjneko.jjnet.networking.discovery;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.Protocol;
import com.jjneko.jjnet.networking.pipes.Pipe;
import com.jjneko.jjnet.utils.JJNetUtils;

public class AdvertisementService {
	
	private ArrayList<Advertisement> ads = new ArrayList<Advertisement>();
	private ArrayList<Advertisement> published = new ArrayList<Advertisement>();
	
	/**
	 * Fetch all advertisements from local cache corresponding the specified className
	 * @param className
	 * @return 
	 */
	public ArrayList<Advertisement> fetchLocal(String className){
		return JJnet.getDatabase().getAdvertisements(className, 0);
	}
	/**
	 * Fetch a specified number of advertisements from local cache corresponding the specified className
	 * @param className
	 * @return 
	 */
	public ArrayList<Advertisement> fetchLocal(String className, int limit){
		return JJnet.getDatabase().getAdvertisements(className, limit);
	}
	
	public void fetchRemote(String className, int adsPerPeer){
		for(Pipe pipe : JJnet.getPipes()){
			try {
				if(pipe.isConnected()){
					pipe.send(Protocol.ARP.toChar()+new String(JJNetUtils.intToByteArray(className.length()),"ISO-8859-1")+className+adsPerPeer);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void publish(Advertisement ad){
		published.add(ad);
		JJnet.getDatabase().insertAdvertisement(ad);
	}

}
