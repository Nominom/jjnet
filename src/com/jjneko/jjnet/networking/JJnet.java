package com.jjneko.jjnet.networking;

import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.jjneko.jjnet.database.DatabaseManager;
import com.jjneko.jjnet.networking.discovery.Advertisement;
import com.jjneko.jjnet.networking.discovery.AdvertisementService;
import com.jjneko.jjnet.networking.discovery.NodeAdvertisement;
import com.jjneko.jjnet.networking.discovery.WorldGroupAdvertisement;
import com.jjneko.jjnet.networking.http.HttpService;
import com.jjneko.jjnet.networking.pipes.Pipe;
import com.jjneko.jjnet.networking.pipes.http.SimpleHttpClientPipe;
import com.jjneko.jjnet.networking.security.SecurityService;
import com.jjneko.jjnet.networking.stun.StunServer;
import com.jjneko.jjnet.networking.udp.UDPService;
import com.jjneko.jjnet.networking.upnp.UPnPService;
import com.jjneko.jjnet.utils.JJNetUtils;

public class JJnet {
	
	public static final int BUFFER_LENGTH=262144; //256 kilobytes
	
	private static Logger log = Logger.getLogger(JJnet.class.getName());
	
	/**
	 * Never send your private key to anyone!
	 */
	public static PrivateKey privateKey=null;
	public static PublicKey publicKey=null;
	public static EndPoint localEndPointAddress=null;
	static DatabaseManager database = null;
	static int minNeighbours=2;
	static int maxNeighbours=5;
	public static NATType natType = NATType.UNSPECIFIED;
	public static int natDelta = 0;
	static String seedAddress;
	static int seedPort;
	static ConnectionType seedType;
	static boolean useHttp = true, useTcp = true, useStun=false, useUPnP = true, useNATPmP = true;
	static int HttpPort=-1, TcpPort=-1;
	static ConcurrentLinkedQueue<Pipe> pipes = new ConcurrentLinkedQueue<Pipe>();
	static Thread msgHandler;
	static boolean running = false;
	static AdvertisementService adService;
	static WorldGroup worldGroup;
	static HttpService https;
	static UDPService udps;
	static UPnPService uPnPs;
	static StunServer stuns;
	static boolean receivingPeerList=false;
	static PeerListBuilder plb = null;
	static PeerListSender pls = null;
	static Thread maintT;
	
	public static void init(String seedAddress, int seedPort, ConnectionType seedType, boolean useHttp, boolean useUdp,boolean useStun, boolean useUPnP, boolean useNATPnP){
		JJnet.seedAddress=seedAddress;
		JJnet.seedPort=seedPort;
		JJnet.seedType=seedType;
		init(useHttp, useUdp, useStun, useUPnP, useNATPnP);
	}
	
	public static void initAsSeed(boolean useHttp, boolean useTcp, boolean useStun, boolean useUPnP, boolean useNATPnP, int HttpPort, int UdpPort){
		JJnet.HttpPort=HttpPort;
		JJnet.TcpPort=UdpPort;
		init(useHttp, useTcp, useStun, useUPnP, useNATPnP);
		worldGroup = new WorldGroup(localEndPointAddress);
		worldGroup.addMember(localEndPointAddress);
		WorldGroupAdvertisement wgad = new WorldGroupAdvertisement(worldGroup.owner,true,worldGroup.getMemberCount());
		adService.publish(wgad);
	}
	
	public static void setSeed(String seedAddress, int seedPort, ConnectionType seedType){
		JJnet.seedAddress=seedAddress;
		JJnet.seedPort=seedPort;
		JJnet.seedType=seedType;
	}
	
	public static void init(boolean useHttp, boolean useTcp, boolean useStun, boolean useUPnP, boolean useNATPmP){
		JJnet.useHttp=useHttp;
		JJnet.useTcp=useTcp;
		JJnet.useUPnP=useUPnP;
		JJnet.useNATPmP=useNATPmP;
		JJnet.useStun=useStun;
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
		running=true;
		msgHandler = new Thread(new MessageHandler());
		msgHandler.start();
		msgHandler.setPriority(Thread.NORM_PRIORITY-2);
		
		new Thread(new NetworkInitializer()).start();
		maintT = new Thread(new maintenanceThread());
		maintT.setPriority(Thread.MIN_PRIORITY);
		maintT.start();
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
	
	public static Long currentTimeMillis(){
		//TODO Time sync protocol
		return System.currentTimeMillis();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static ConcurrentLinkedQueue<Pipe> getPipes() {
		return pipes;
	}

	static void newPeerProtocol(Pipe p, byte[] msg){
		try{
			if(worldGroup.owner.equals(localEndPointAddress)){
				String keystring = new String(Arrays.copyOfRange(msg, 1, msg.length), "ISO-8859-1");
				PublicKey key = SecurityService.parsePublicKey(keystring);
				EndPoint newPoint= new EndPoint(key);
				if(!worldGroup.containsMember(newPoint)){
					worldGroup.addMember(newPoint);
					worldGroup.broadcast(msg);
				}
			}
		}catch(Exception ex){
			log.severe("NewPeerProtocol failed: "+ ex.getStackTrace());
		}
	}
	
	
	private static void fetchWorldPeerList(){
		/*TODO Better peer list fetching implementation*/
		byte[] message = new byte[2];
		message[0]=Protocol.PLRP.value();
		message[1]=PeerListBuilder.PLB_REQUEST;
		
		JJnet.plb= new PeerListBuilder(pipes.peek());
		receivingPeerList=true;
		pipes.peek().send(message);
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
	
	public static WorldGroup getWorldGroup() {
		return worldGroup;
	}
	
	public static String getLocalIp(){
		//TODO internet facing ip
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "";
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
			if(useTcp){
				//TODO TCP server
			}
			if(useStun){
				try {
					stuns = new StunServer();
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
					}else if(seedType==ConnectionType.TCP){
						
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
				
				fetchWorldPeerList();
				
				byte[] pubkey=null;
				try {
					pubkey = SecurityService.publicKeytoString(localEndPointAddress.getKey()).getBytes("ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				byte[] peerProto = new byte[pubkey.length+1];
				peerProto[0]=Protocol.NPP.value();
				System.arraycopy(pubkey, 0, peerProto, 1, pubkey.length);
				pipes.peek().send(peerProto);
			}
			
			pls = new PeerListSender();
			
			
			try {
				//TODO change to actual internet facing ip and real tcp port
				NodeAdvertisement ad = new NodeAdvertisement(InetAddress.getLocalHost().getHostAddress(), useHttp, useTcp, useStun, useHttp? https.getServerPort() : 0, useTcp? 0 : 0 , useStun? stuns.stunServerPort : 0);
				getAdvertisementService().publish(ad);
			}catch(Exception ex){
				ex.printStackTrace();
			}


			System.out.println("initialized");
		}
	}
	
	static class PeerListBuilder{
		int listLength=0,receivedCount=0;;
		long startTime=0;
		Pipe p;
		public static final long PLB_TIMEOUT=5*60*1000;
		public static final byte
			PLB_REQUEST=0,
			PLB_REQUEST_APPROVED=1,
			PLB_REQUEST_DENIED=2,
			PLB_REQUEST_CONFIRM=3,
			PLB_LIST_LENGTH=4,
			PLB_START=5,
			PLB_KEY=6,
			PLB_END=7,
			PLB_STOP=8;
		
		public PeerListBuilder(Pipe p){
			startTime=JJnet.currentTimeMillis();
			this.p=p;
		}
		
		private boolean approved=false;
		private boolean receiving=false;
		public void processMessage(byte[] msg) throws JJNetException, InvalidKeySpecException, UnsupportedEncodingException, ParseException{
			if(JJnet.currentTimeMillis()>startTime+PLB_TIMEOUT){
				throw new JJNetException("Peerlist request timed out");
			}
			if(!approved){
				if(msg[1]==PLB_REQUEST_APPROVED){
					approved=true;
					byte[] response = new byte[18];
					response[0]=Protocol.PLRP.value();
					response[1]=PLB_REQUEST_CONFIRM;
					System.arraycopy(msg, 2, response, 2, 16);
					p.send(response);
				}else if(msg[1]==PLB_REQUEST_DENIED){
					throw new JJNetException("Peerlist request was denied");
				}
			}else if(!receiving){
				if(msg[1]==PLB_LIST_LENGTH){
					listLength=JJNetUtils.byteArrayToInt(msg, 2);
					byte[] response = new byte[2];
					response[0]=Protocol.PLRP.value();
					response[1]=PLB_START;
					p.send(response);
					receiving=true;
				}
			}else{
				if(msg[1]==PLB_END){
					finalizePLB();
				}else if(msg[1]==PLB_KEY){
					byte[] subArray= Arrays.copyOfRange(msg, 2, msg.length);
					PublicKey key = SecurityService.parsePublicKey(new String(subArray,"ISO-8859-1"));
					worldGroup.addMember(new EndPoint(key));
					receivedCount++;
					if(bytesInKey==0){
						sendStart=JJnet.currentTimeMillis();
						bytesInKey=subArray.length;
					}
					if(receivedCount>listLength){
						finalizePLB();
					}
				}
			}
		}
		
		long sendStart=0;
		int bytesInKey=0;
		private void finalizePLB() {
			receiving=false;
			receivingPeerList=false;
			plb=null;
			System.out.println("Peerlist receiving done!");
			float tookTime= (float)(JJnet.currentTimeMillis()-sendStart)/1000.0f;
			long amountOfBytes=bytesInKey*receivedCount;
			
			//TODO Can use this as speed limiter
			int bytesPerSecond = (int)(amountOfBytes/tookTime);
			System.out.println("BytesPerSecond was "+ bytesPerSecond);
		}
	}
	
	static class PeerListSender{
		EndPoint[] list;
		int listLength=0;
		TObjectByteHashMap<Pipe> recepients = new TObjectByteHashMap<Pipe>();
		THashMap<Pipe, byte[]> recepientsTokens = new THashMap<Pipe, byte[]>();
		
		public static final byte
		PLB_REQUEST=0,
		PLB_REQUEST_APPROVED=1,
		PLB_REQUEST_DENIED=2,
		PLB_REQUEST_CONFIRM=3,
		PLB_LIST_LENGTH=4,
		PLB_START=5,
		PLB_KEY=6,
		PLB_END=7,
		PLB_STOP=8;

		public void processMessage(Pipe p, byte[] msg) throws JJNetException, InvalidKeySpecException, UnsupportedEncodingException, ParseException{
			if(recepients.contains(p)){
				byte recep = recepients.get(p);
				byte msgp = msg[1];
				
				if(recep==PLB_END || recep==PLB_STOP){
					return;
				}
				
				if(recep==PLB_REQUEST_APPROVED && msgp==PLB_REQUEST_CONFIRM){
					byte[] confirm = Arrays.copyOfRange(msg, 2, 18);
					byte[] token = recepientsTokens.get(p);
					if(Arrays.equals(token, confirm)){
						recepientsTokens.remove(p);
						byte[] response = new byte[6];
						response[0] = Protocol.PLRRP.value();
						response[1] = PLB_LIST_LENGTH;
						JJNetUtils.intToByteArray(listLength, response, 2);
						
						recepients.put(p, PLB_REQUEST_CONFIRM);
						p.send(response);
					}else{
						byte[] response = new byte[6];
						response[0] = Protocol.PLRRP.value();
						response[1] = PLB_REQUEST_DENIED;
						p.send(response);
						recepients.put(p, PLB_END);
					}
				}else if(recep==PLB_REQUEST_CONFIRM && msgp==PLB_START){
					recepients.put(p, PLB_START);
					new Thread(new ListSender(p)).start();
				}else if(msgp==PLB_STOP){
					recepients.put(p, PLB_STOP);
				}
			}else{
				if(msg[1]==PLB_REQUEST){
					Object[] elist = worldGroup.getMemberList().toArray();
					list=new EndPoint[elist.length];
					for(int i=0;i<elist.length;i++){
						list[i]=(EndPoint)elist[i];
					}
					listLength=list.length;
					recepients.put(p, PLB_REQUEST_APPROVED);
					byte[] response = new byte[18];
					response[0] = Protocol.PLRRP.value();
					response[1] = PLB_REQUEST_APPROVED;
					byte[] token = new byte[16];
					SecurityService.getRandom().nextBytes(token);
					recepientsTokens.put(p, token);
	
					System.arraycopy(token, 0, response, 2, 16);
					p.send(response);
				}
			}
		}
		
		/*TODO call this at some point*/
		void cleanPLS(){
			TObjectByteIterator<Pipe> i = recepients.iterator();
			while(i.hasNext()){
				i.advance();
				Pipe p = i.key();
				if(!pipes.contains(p)){
					recepients.remove(p);
					recepientsTokens.remove(p);
				}
			}
		}
		
		private class ListSender implements Runnable{
			public Pipe p=null;
			
			public ListSender(Pipe p){
				this.p=p;
			}
			
			@Override
			public void run() {
				for(int i=0;i<listLength;i++){
					if(recepients.get(p)==PLB_STOP)
						break;
					try{
						byte[] key = (SecurityService.publicKeytoString(list[i].getKey())).getBytes("ISO-8859-1");
						byte[] msg = new byte[key.length+2];
						msg[0] = Protocol.PLRRP.value();
						msg[1] = PLB_KEY;
						System.arraycopy(key, 0, msg, 2, key.length);
						p.send(msg);
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
				
				byte[] msg = new byte[2];
				msg[0] = Protocol.PLRRP.value();
				msg[1] = PLB_END;
				p.send(msg);
				
				System.out.println("Peerlist sending done!");
				recepients.put(p,PLB_END);
			}
		}
	}
	
	
	static class maintenanceThread implements Runnable{

		@Override
		public void run() {
			try{
				Thread.sleep(20*1000);
			}catch(InterruptedException ex){}
			while(running){
				try{
					System.err.println("Running maintenace");
					pls.cleanPLS();
					JJnet.getAdvertisementService().fetchRemote(NodeAdvertisement.class,1);
					
					if(pipes.size()<minNeighbours){
						for(Advertisement ad : JJnet.getAdvertisementService().fetchLocal(NodeAdvertisement.class, minNeighbours-pipes.size())){
							NodeAdvertisement nad = (NodeAdvertisement) ad;
							if(nad.http){
								boolean alreadyConnected=false;
								if(nad.ipAddress.equals(getLocalIp())){
									alreadyConnected=true;
									continue;
								}
								for(Pipe p : pipes){
									if(p.getIPAddress().equals(nad.ipAddress)){
										alreadyConnected=true;
									}
								}
								
								if(!alreadyConnected){
									Pipe pipe = new SimpleHttpClientPipe(InetAddress.getByName(nad.ipAddress), nad.httpPort);
									pipe.connect();
								}
							}
						}
					}
					System.err.println("Maintenance complete");
				}catch(Exception ex)
				{ex.printStackTrace();}
				finally{
					try{
						Thread.sleep(60*1000);
					}catch(InterruptedException ex){}
				}
			}
		}
		
	}


}
