package jjnet;

import java.net.InetAddress;

import com.jjneko.jjnet.networking.ConnectionType;
import com.jjneko.jjnet.networking.JJnet;

public class Tester2_2 {
	
	public static void main(String[] args){
		try {
			InetAddress iadd = InetAddress.getByName("91.152.90.11");
			int port = 7555;
			
			JJnet.init(iadd.getHostAddress(), port, ConnectionType.HTTP, true, false, false, false, false,7555,0);

			JJnet.start();
			
//			for(int i=0;i<200;i++){
//				Thread.sleep(20000);
//				JJnet.getAdvertisementService().fetchRemote(NodeAdvertisement.class,2);
//			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
