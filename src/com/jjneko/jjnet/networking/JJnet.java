package com.jjneko.jjnet.networking;

import java.net.InetAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.jjneko.jjnet.database.DatabaseManager;
import com.jjneko.jjnet.networking.discovery.Advertisement;
import com.jjneko.jjnet.networking.discovery.AdvertisementService;
import com.jjneko.jjnet.networking.discovery.WorldGroupAdvertisement;
import com.jjneko.jjnet.networking.http.HttpService;
import com.jjneko.jjnet.networking.pipes.Pipe;
import com.jjneko.jjnet.networking.pipes.http.SimpleHttpClientPipe;
import com.jjneko.jjnet.networking.security.SecurityService;
import com.jjneko.jjnet.networking.stun.StunServer;
import com.jjneko.jjnet.networking.udp.UDPService;
import com.jjneko.jjnet.networking.upnp.UPnPService;

public class JJnet {
	
	public static final int BUFFER_LENGTH=16384;
	
	private static Logger log = Logger.getLogger(JJnet.class.getName());
	
	/**
	 * Never send your private key to anyone!
	 */
	public static PrivateKey privateKey=null;
	public static PublicKey publicKey=null;
	public static EndPoint localEndPointAddress=null;
	static DatabaseManager database = null;
	static int minNeighbours=1;
	static int maxNeighbours=5;
	public static NATType natType = NATType.UNSPECIFIED;
	public static int natDelta = 0;
	static String seedAddress;
	static int seedPort;
	static ConnectionType seedType;
	static boolean useHttp = true, useUdp = true, useUPnP = true, useNATPmP = true;
	static int HttpPort=-1, UDPPort=-1;
	static ConcurrentLinkedQueue<Pipe> pipes = new ConcurrentLinkedQueue<Pipe>();
	static Thread msgHandler;
	static boolean running = false;
	static AdvertisementService adService;
	static WorldGroup worldGroup;
	static HttpService https;
	static UDPService udps;
	static UPnPService uPnPs;
	static StunServer stuns;
	
	public static void init(String seedAddress, int seedPort, ConnectionType seedType, boolean useHttp, boolean useUdp, boolean useUPnP, boolean useNATPnP){
		JJnet.seedAddress=seedAddress;
		JJnet.seedPort=seedPort;
		JJnet.seedType=seedType;
		init(useHttp, useUdp, useUPnP, useNATPnP);
	}
	
	public static void initAsSeed(boolean useHttp, boolean useUdp, boolean useUPnP, boolean useNATPnP, int HttpPort, int UdpPort){
		JJnet.HttpPort=HttpPort;
		JJnet.UDPPort=UdpPort;
		init(useHttp, useUdp, useUPnP, useNATPnP);
		worldGroup = new WorldGroup(localEndPointAddress);
		WorldGroupAdvertisement wgad = new WorldGroupAdvertisement(worldGroup.owner,true);
		adService.publish(wgad);
	}
	
	public static void setSeed(String seedAddress, int seedPort, ConnectionType seedType){
		JJnet.seedAddress=seedAddress;
		JJnet.seedPort=seedPort;
		JJnet.seedType=seedType;
	}
	
	public static void init(boolean useHttp, boolean useUdp, boolean useUPnP, boolean useNATPmP){
		JJnet.useHttp=useHttp;
		JJnet.useUdp=useUdp;
		JJnet.useUPnP=useUPnP;
		JJnet.useNATPmP=useNATPmP;
		setDatabase(new DatabaseManager());
		
		KeyPair kp = SecurityService.generateRSAKeyPair();
		privateKey = kp.getPrivate();
		publicKey = kp.getPublic();
		
		localEndPointAddress=new EndPoint(publicKey);
		
		adService = new AdvertisementService();
		
		
		log.info("localEndpoint= " + localEndPointAddress.getAddress());
		log.info("JJnet initialized!");
	}
	
	
	public synchronized static void start(){
		if(running)
			return;
		msgHandler = new Thread(new MessageHandler());
		msgHandler.start();
		new Thread(new NetworkInitializer()).start();
		running=true;
	}
	
	public synchronized static void stop(){
		if(!running)
			return;
		/*TODO stop*/
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
		
		
		return null;
	}

	/*TODO Better comments ^^;*/
	
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
	
	private static class NetworkInitializer implements Runnable{
		
		@Override
		public void run() {
			if(useHttp){
				try {
					https = new HttpService(HttpPort);
					https.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(useUdp){
				try {
					udps = new UDPService(UDPPort);
					stuns = new StunServer();
					udps.start();
					stuns.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(useUPnP){
				try {
					uPnPs = new UPnPService();
					uPnPs.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(seedAddress!=null){
				Pipe pipe = null;

				try {
					if(seedType==ConnectionType.HTTP){
						pipe = new SimpleHttpClientPipe(InetAddress.getByName(seedAddress), seedPort);
						pipe.connect();
					}else if(seedType==ConnectionType.UDP){
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				int tries=0;
				while(!pipe.isConnected() && tries < 200){
					try{
						Thread.sleep(100);
						tries++;
					}catch(Exception ex){}
				}
				while(true){
					adService.fetchRemote(WorldGroupAdvertisement.class, 1);
					try{
						Thread.sleep(1000);
						WorldGroupAdvertisement wgad = (WorldGroupAdvertisement) adService.fetchLocal(WorldGroupAdvertisement.class).get(0);
						worldGroup=new WorldGroup(wgad);
						break;
					}catch(Exception ex){}
				}
			}
			System.out.println("initialized");
		}
	}

	

}
