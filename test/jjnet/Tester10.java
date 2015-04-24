package jjnet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import com.jjneko.jjnet.networking.stun.StunClient;
import com.jjneko.jjnet.utils.JJNetUtils;

public class Tester10 {
	
	public static void main(String[] args){
		try {
			final DatagramSocket socket1 = new DatagramSocket();			
			
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
			
			
			final ReliableDatagramSocket2 rsocket = new ReliableDatagramSocket2(socket1,receiver);
			
			

			new Thread(new Runnable() {

				@Override
				public void run() {
					while(true){
						try{
							rsocket.send("Keepalive".getBytes(), true);
							Thread.sleep(30000);
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(0);
						}
					}
				}
			}).start();
			
			new Thread(new Runnable() {

				@Override
				public void run() {
					ReliableDatagramPacket packet = new ReliableDatagramPacket();
					while(true){
						try {
							rsocket.receive(packet);
							System.out.println(packet.toString());
							System.out.println(new String(packet.getData()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();

			
			
			String msg = "";
			msg=s.nextLine();
			
			while(!msg.equals("exit")){
				if(msg.equals("start")){
					new Thread(new Runnable() {
						@Override
						public void run() {
							
							long beforeTime=System.nanoTime();
							for(int i=0;i<100000;i++){
								try{
									rsocket.send(JJNetUtils.intToByteArray(i), true);
								} catch (Exception e) {
									e.printStackTrace();
									System.exit(0);
								}
							}
							System.out.println("send took "+(double)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");
						}
					}).start();
				}
				rsocket.send(msg.getBytes());
				msg=s.nextLine();
			}
			
			s.close();
			socket1.close();
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
