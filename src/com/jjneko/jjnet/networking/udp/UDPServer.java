package com.jjneko.jjnet.networking.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer implements Runnable{

	public static int PACKET_BUFFER_SIZE = 2048;
	DatagramSocket socket;
	
	public UDPServer(DatagramSocket socket){
		this.socket=socket;
	}
	
	@Override
	public void run() {
		DatagramPacket packet = new DatagramPacket(new byte[PACKET_BUFFER_SIZE], PACKET_BUFFER_SIZE);
		try {
			socket.receive(packet);
			System.out.println(new String(packet.getData()).trim());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
