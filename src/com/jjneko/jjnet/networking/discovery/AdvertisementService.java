package com.jjneko.jjnet.networking.discovery;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.Protocol;
import com.jjneko.jjnet.networking.pipes.Pipe;
import com.jjneko.jjnet.utils.JJNetUtils;

public class AdvertisementService {
	
	private THashSet<Advertisement> published = new THashSet<Advertisement>();
	
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
		// TODO check if already published first
		
		published.add(ad);
		JJnet.getDatabase().insertAdvertisement(ad);
	}
	public void add(Advertisement ad) {
		JJnet.getDatabase().insertAdvertisement(ad);
	}

}
