package com.jjneko.jjnet.networking.udp;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Logger;

public class UDPService {
	
	Logger logger = Logger.getLogger(UDPService.class.getName());
	private final int SERVERPORT;
	private DatagramSocket socket;
	private UDPServer server;
	private Thread packetHandler;
	
	public UDPService(int serverPort){
		this.SERVERPORT=serverPort;
	}
	
	public void start() throws SocketException{
		logger.info("Starting UDP Listening port in port "+SERVERPORT);
		socket=new DatagramSocket(SERVERPORT);
		server=new UDPServer(socket);
		packetHandler = new Thread(server);
		packetHandler.start();
		logger.info("Now listening UDP requests on "+SERVERPORT);
	}

	
	/**
	 * @return the sERVERPORT
	 */
	public int getSERVERPORT() {
		return SERVERPORT;
	}
}
