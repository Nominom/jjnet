package com.jjneko.jjnet.networking;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.jjneko.jjnet.database.DatabaseManager;
import com.jjneko.jjnet.networking.security.SecurityService;

public class NetworkManager {
	
	private static Logger log = Logger.getLogger(NetworkManager.class.getName());
	
	/**
	 * Never send your private key to anyone!
	 */
	public static PrivateKey privateKey=null;
	public static PublicKey publicKey=null;
	public static String localEndPointAddress="";
	private static DatabaseManager database = null;
	private static ArrayList<EndPoint> peers = new ArrayList<EndPoint>();
	public static long TIMESTAMP_VALID=10000;
	private static int minNeighbours=1;
	private static int maxNeighbours=5;
	public static NATType natType = NATType.UNSPECIFIED;
	public static int natDelta = 0;
	
	public static void init(){
		database = new DatabaseManager();
		
		KeyPair kp = SecurityService.generateRSAKeyPair();
		privateKey = kp.getPrivate();
		publicKey = kp.getPublic();
		
		localEndPointAddress=SecurityService.HashtoHex(
				SecurityService.publicKeytoString(publicKey),
				EndPoint.ENDPOINT_ADDRESS_LENGTH);
		
		log.info("localEndpoint= " + localEndPointAddress);
		log.info("NetworkManager initialized!");
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

}
