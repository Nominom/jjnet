package jjnet;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jjneko.jjnet.networking.ConnectionType;
import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.discovery.NodeAdvertisement;
import com.jjneko.jjnet.networking.pipes.http.SimpleHttpClientPipe;

public class Tester2 {
	
	public static void main(String[] args){
		try {
			JJnet.init(false, false, false, false);
			InetAddress iadd = InetAddress.getLocalHost();
			int port = 7555;
			
			JJnet.setSeed(iadd.getHostAddress(), port, ConnectionType.HTTP);
			JJnet.start();
			
			for(int i=0;i<200;i++){
				Thread.sleep(20000);
				JJnet.getAdvertisementService().fetchRemote(NodeAdvertisement.class,2);
			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
