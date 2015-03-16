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
	
	private THashSet<String> published = new THashSet<String>();
	
	/**
	 * Fetch all advertisements from local cache corresponding the specified class
	 * @param adClass
	 * @return 
	 */
	public ArrayList<Advertisement> fetchLocal(Class<?> adClass){
		return JJnet.getDatabase().getAdvertisements(adClass.getName(), 0);
	}
	
	/**
	 * Fetch a specified number of advertisements from local cache corresponding the specified class
	 * @param adClass
	 * @return 
	 */
	public ArrayList<Advertisement> fetchLocal(Class<?> adClass, int limit){
		return JJnet.getDatabase().getAdvertisements(adClass.getName(), limit);
	}
	
	
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
	
	/**
	 * Request a specified number of advertisements from neighbors corresponding the specified class
	 * Advertisements will be stored to local cache when they arrive
	 * @param adClass
	 * @param adsPerPeer
	 */
	public void fetchRemote(Class<?> adClass, int adsPerPeer){
		String className = adClass.getName();
		byte[] msg = new byte[className.length()+9];
		try {
			msg[0]=Protocol.ARP.value();
			System.arraycopy(JJNetUtils.intToByteArray(className.length()), 0, msg, 1, 4);
			System.arraycopy(JJNetUtils.intToByteArray(adsPerPeer), 0, msg, 5, 4);
			System.arraycopy(className.getBytes("ISO-8859-1"), 0, msg, 9, className.length());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		for(Pipe pipe : JJnet.getPipes()){
			if(pipe.isConnected()){
				pipe.send(msg);
			}
		}
	}
	
	/**
	 * Request a specified number of advertisements from neighbors corresponding the specified className.
	 * Advertisements will be stored to local cache when they arrive
	 * @param className
	 * @param adsPerPeer
	 */
	public void fetchRemote(String className, int adsPerPeer){
		byte[] msg = new byte[className.length()+9];
		try {
			msg[0]=Protocol.ARP.value();
			System.arraycopy(JJNetUtils.intToByteArray(className.length()), 0, msg, 1, 4);
			System.arraycopy(JJNetUtils.intToByteArray(adsPerPeer), 0, msg, 5, 4);
			System.arraycopy(className.getBytes("ISO-8859-1"), 0, msg, 9, className.length());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		for(Pipe pipe : JJnet.getPipes()){
			if(pipe.isConnected()){
				pipe.send(msg);
			}
		}
	}
	
	public void publish(Advertisement ad){
		String hash = Advertisement.generateHash(ad);
		if(published.add(hash)){
			JJnet.getDatabase().insertAdvertisement(ad);
		}
	}
	public void add(Advertisement ad) {
		JJnet.getDatabase().insertAdvertisement(ad);
	}

}
