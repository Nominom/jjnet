package com.jjneko.jjnet.networking.udp;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Logger;

public class UDPService {
	
	Logger logger = Logger.getLogger(UDPService.class.getName());
	private final int serverport;
	private DatagramSocket socket;
	private UDPServer server;
	private Thread packetHandler;
	
	public UDPService(int serverPort){
		this.serverport=serverPort;
	}
	
	public void start() throws SocketException{
		logger.info("Starting UDP Listening port in port "+serverport);
		socket=new DatagramSocket(serverport);
		server=new UDPServer(socket);
		packetHandler = new Thread(server);
		packetHandler.start();
		logger.info("Now listening UDP requests on "+serverport);
	}

	
	/**
	 * @return the port this UDP server is running on
	 */
	public int getServerPort() {
		return serverport;
	}
}
