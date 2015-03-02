package jjnet;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.discovery.NodeAdvertisement;
import com.jjneko.jjnet.networking.pipes.http.SimpleHttpClientPipe;

public class Tester2 {
	
	public static void main(String[] args){
		try {
			JJnet.init();
			InetAddress iadd = InetAddress.getLocalHost();
			int port = 7555;
			
			SimpleHttpClientPipe pipe = new SimpleHttpClientPipe(null, iadd, port);
			pipe.connect();
			
			JJnet.start();
			
			Thread.sleep(6000);
			
			JJnet.getAdvertisementService().fetchRemote(NodeAdvertisement.class.getName(),2);
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
