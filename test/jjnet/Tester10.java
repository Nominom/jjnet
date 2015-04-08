package jjnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import org.ice4j.pseudotcp.PseudoTcpSocket;
import org.ice4j.pseudotcp.PseudoTcpSocketFactory;

import com.jjneko.jjnet.networking.stun.StunClient;

public class Tester10 {
	
	public static void main(String[] args){
		try {
			final DatagramSocket socket1 = new DatagramSocket(2888);
			DatagramSocket socket2 = new DatagramSocket(2889);
			
			
			InetSocketAddress inet1 = new InetSocketAddress("stun.l.google.com", 19302);
			InetSocketAddress inet2 = new InetSocketAddress("stun1.l.google.com", 19302);
			
			StunClient stcl1 = new StunClient(inet1, socket1);
			InetSocketAddress out1 = stcl1.getMappedAddress();
			StunClient stcl2 = new StunClient(inet2, socket1);
			InetSocketAddress out2 = stcl2.getMappedAddress();
			
			System.out.println(out1);
			System.out.println(out2);
			
			System.out.println("Input ip: ");
			
			Scanner s = new Scanner(System.in);
			
			String ip = s.nextLine();
			
			System.out.println("Input port: ");
			
			int port = Integer.parseInt(s.nextLine());
			
			boolean connected=false;
			DatagramPacket pack = new DatagramPacket(new byte[1024], 1024);
			InetSocketAddress receiver = new InetSocketAddress(ip,port);
			socket1.setSoTimeout(1000);
			boolean accept=true;
			
			while(!connected){
				try{
					pack = new DatagramPacket(new byte[1024], 1024);
					socket1.receive(pack);
					System.out.println("received packet! :"+ new String(pack.getData()));
					receiver=(InetSocketAddress) pack.getSocketAddress();
					connected=true;
					System.out.println("connected!");
					
					pack.setData("Hello".getBytes());
					pack.setLength(5);
					pack.setSocketAddress(receiver);
					socket1.send(pack);
					System.out.println("sent hello!");
				}catch(SocketTimeoutException ex){
					pack.setData("Hello".getBytes());
					pack.setLength(5);
					pack.setSocketAddress(receiver);
					socket1.send(pack);
					System.out.println("sent hello!");
					accept=false;
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			
			socket1.setSoTimeout(0);
			PseudoTcpSocket psocket = new PseudoTcpSocketFactory().createSocket(socket1);
			if(!accept)
				psocket.connect(receiver);
			else
				psocket.accept(10000);
			
			new Thread(new Runnable(){

				@Override
				public void run() {
					DatagramPacket packs = new DatagramPacket(new byte[1024], 1024);
					while(true){
						try {
							socket1.receive(packs);
							System.out.println("received packet! :"+ new String(packs.getData()));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}}).start();
			
			for(int i=0;i<200000;i++){
				pack.setData("keepalive".getBytes());
				pack.setLength("keepalive".getBytes().length);
				pack.setSocketAddress(receiver);
				socket1.send(pack);
				try{
					Thread.sleep(20000);
				}catch(Exception ex){};
			}
			
			socket1.close();
			socket2.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
