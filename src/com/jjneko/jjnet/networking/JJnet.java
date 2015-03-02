package com.jjneko.jjnet.networking;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.jjneko.jjnet.database.DatabaseManager;
import com.jjneko.jjnet.networking.discovery.Advertisement;
import com.jjneko.jjnet.networking.discovery.AdvertisementService;
import com.jjneko.jjnet.networking.pipes.Pipe;
import com.jjneko.jjnet.networking.security.SecurityService;

public class JJnet {
	
	private static Logger log = Logger.getLogger(JJnet.class.getName());
	
	/**
	 * Never send your private key to anyone!
	 */
	public static PrivateKey privateKey=null;
	public static PublicKey publicKey=null;
	public static String localEndPointAddress="";
	static DatabaseManager database = null;
	/* TODO Don't know if i want to keep this*/
	static ArrayList<EndPoint> peers = new ArrayList<EndPoint>();
	public static long TIMESTAMP_VALID=10000;
	static int minNeighbours=1;
	static int maxNeighbours=5;
	public static NATType natType = NATType.UNSPECIFIED;
	public static int natDelta = 0;
	static String seedAddress;
	static int seedPort;
	static ConnectionType seedType;
	static boolean useHttp = true, useUdp = true, useUPnP = true, useNATPnP = true;
	static ConcurrentLinkedQueue<Pipe> pipes = new ConcurrentLinkedQueue<Pipe>();
	static Thread msgHandler;
	static boolean running = false;
	static AdvertisementService adService;
	
	public static void init(String seedAddress, int seedPort, ConnectionType seedType){
		JJnet.seedAddress=seedAddress;
		JJnet.seedPort=seedPort;
		JJnet.seedType=seedType;
		init();
	}
	
	public static void init(){
		setDatabase(new DatabaseManager());
		
		KeyPair kp = SecurityService.generateRSAKeyPair();
		privateKey = kp.getPrivate();
		publicKey = kp.getPublic();
		
		localEndPointAddress=SecurityService.hashAsBase64(
				SecurityService.publicKeytoString(publicKey),
				EndPoint.ENDPOINT_ADDRESS_LENGTH);
		
		adService = new AdvertisementService();
		
		log.info("localEndpoint= " + localEndPointAddress);
		log.info("JJnet initialized!");
	}
	
	public synchronized static void start(){
		if(running)
			return;
		msgHandler = new Thread(new MessageHandler());
		msgHandler.start();
		running=true;
	}
	
	public synchronized static void stop(){
		if(!running)
			return;
	}
	
	
	public static void addPipe(Pipe pipe){
		pipes.add(pipe);
	}
	
	public static void removePipe(Pipe pipe){
		pipes.remove(pipe);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static ConcurrentLinkedQueue<Pipe> getPipes() {
		return pipes;
	}

	public static EndPoint newPeerProtocol(String endpoint, String publicKey, long timestamp, String hashedStamp ){
		try{
//			Long currtime = System.currentTimeMillis();
//			if(currtime-timestamp>TIMESTAMP_VALID){
//				throw new Exception("Timestamp too old!");
//			}
//			PublicKey pbk = SecurityService.parsePublicKey(publicKey);
//			Long hashStamp = Long.parseLong(new String(SecurityService.rsaDecrypt(hashedStamp.getBytes(), pbk)));
//			if(hashStamp!=timestamp){
//				log.info("timestamp: " + timestamp + " decrypted timestamp: "+ hashStamp);
//				throw new Exception("Hashed timestamp did not match given timestamp");
//			}
//			if(!endpoint.equals(SecurityService.MD5HashtoHex(publicKey, EndPoint.ENDPOINT_ADDRESS_LENGTH))){
//				log.info("endpoint: " + endpoint + " public key hash: "+ 
//						SecurityService.MD5HashtoHex(publicKey, EndPoint.ENDPOINT_ADDRESS_LENGTH));
//				throw new Exception("Endpoint name does not match public key hash!");
//			}
			
			/* TODO New peer protocol*/
		}catch(Exception ex){
			log.severe("NewPeerProtocol failed: "+ ex.getStackTrace());
			return null;
		}
		
		for(EndPoint ep : peers){
			
		}
		
		
		return null;
	}

	/**
	 * @return the database
	 */
	public static DatabaseManager getDatabase() {
		return database;
	}

	/**
	 * @param database the database to set
	 */
	private static void setDatabase(DatabaseManager database) {
		JJnet.database = database;
	}

	/**
	 * @return the adService
	 */
	public static AdvertisementService getAdvertisementService() {
		return adService;
	}

}
