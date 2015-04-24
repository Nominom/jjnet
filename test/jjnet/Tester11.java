package jjnet;

import gnu.trove.set.hash.TIntHashSet;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.jjneko.jjnet.utils.JJNetUtils;

public class Tester11 {
	
	public static void main(String[] args) throws Exception {
		final ReliableDatagramSocket2 so1 = new ReliableDatagramSocket2(new DatagramSocket(2222), new InetSocketAddress("127.0.0.1",2223));
		final ReliableDatagramSocket2 so2 = new ReliableDatagramSocket2(new DatagramSocket(2223), new InetSocketAddress("127.0.0.1",2222));
		
		final TIntHashSet received=new TIntHashSet();
		
		
		new Thread(new Runnable() {
			
			

			@Override
			public void run() {
				
				long beforeTime=System.nanoTime();
				for(int i=0;i<100000;i++){
					try{
						so1.send(JJNetUtils.intToByteArray(i), true);
//						System.out.println(so1.sendAcked);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
				System.out.println("send took "+(double)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");
				
				try{
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				
				for(int i=0;i<10000;i++){
					if(!received.contains(i)){
						System.out.println("Packet "+ i + " was not received");
					}
				}
				System.out.println("received!");
				
				System.exit(0);
				
			}
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				ReliableDatagramPacket packet = new ReliableDatagramPacket();
				while(true){
					try {
						so1.receive(packet);
//						System.out.println(packet);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(true){
					try{
						so2.send(new byte[]{0,0,0,0}, true);
						Thread.sleep(100);
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
						so2.receive(packet);
//						System.out.println(packet.toString());
						received.add(JJNetUtils.byteArrayToInt(packet.getData()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	

}
