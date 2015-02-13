package jjnet;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jjneko.jjnet.networking.pipes.http.SimpleHttpClientPipe;

public class Tester2 {
	
	public static void main(String[] args){
		try {
			InetAddress iadd = InetAddress.getLocalHost();
			int port = 7555;
			
			SimpleHttpClientPipe pipe = new SimpleHttpClientPipe(null, iadd, port);
			pipe.connect();
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
