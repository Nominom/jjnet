package jjnet;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.jjneko.jjnet.networking.stun.StunClient;

public class Tester10 {
	
	public static void main(String[] args){
		try {
			DatagramSocket socket1 = new DatagramSocket();
			DatagramSocket socket2 = new DatagramSocket();
			
			
			InetSocketAddress inet1 = new InetSocketAddress("stun.stunprotocol.org", 3478);
			InetSocketAddress inet2 = new InetSocketAddress("stun.l.google.com", 19302);
			
			StunClient stcl1 = new StunClient(inet1, socket1);
			StunClient stcl2 = new StunClient(inet2, socket1);
			
			InetSocketAddress out1 = stcl1.getMappedAddress();
			InetSocketAddress out2 = stcl2.getMappedAddress();
			
			System.out.println(out1);
			System.out.println(out2);
			
			socket1.close();
			socket2.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
