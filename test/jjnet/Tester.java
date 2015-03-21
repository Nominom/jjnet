package jjnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.xml.sax.SAXException;

import com.jjneko.jjnet.messaging.XML;
import com.jjneko.jjnet.networking.EndPoint;
import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.discovery.Advertisement;
import com.jjneko.jjnet.networking.discovery.AdvertisementService;
import com.jjneko.jjnet.networking.discovery.NodeAdvertisement;
import com.jjneko.jjnet.networking.discovery.WorldGroupAdvertisement;
import com.jjneko.jjnet.networking.http.HttpService;
import com.jjneko.jjnet.networking.security.SecurityService;
import com.jjneko.jjnet.networking.stun.StunServer;
import com.jjneko.jjnet.networking.udp.UDPService;
import com.jjneko.jjnet.networking.upnp.UPnPService;

public class Tester {
	
	public static void main(String[] args) throws UnknownHostException{
		JJnet.initAsSeed(true,true,false,false,7555,7556);
//		
//		try {
//			KeyPair kp = SecurityService.generateRSAKeyPair();
//			PublicKey pub = kp.getPublic();
//			PrivateKey priv = kp.getPrivate();
//			
//			System.out.println(" public= " + SecurityService.publicKeytoString(pub)
//					+"\n   hash= " + SecurityService.hashAsBase64(SecurityService.publicKeytoString(pub))
//					+"\n  again= " + SecurityService.publicKeytoString(SecurityService.parsePublicKey(SecurityService.publicKeytoString(pub))));
//			System.out.println("private= " + SecurityService.privateKeytoString(priv)
//					 +"\n   hash= " + SecurityService.hashAsBase64(SecurityService.privateKeytoString(priv))
//					 +"\n  again= " + SecurityService.privateKeytoString(SecurityService.parsePrivateKey(SecurityService.privateKeytoString(priv))));
//			
//			String message = "Hashu";
//			System.out.println(message);
//			String encrypted = SecurityService.rsaEncrypt(message, pub);
//			System.out.println(new String(encrypted));
//			String decrypted = SecurityService.rsaDecrypt(encrypted, priv);
//			System.out.println(new String(decrypted));
//			
//		} catch (InvalidKeySpecException e) {
//			e.printStackTrace();
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println(UUID.randomUUID().hashCode());
//		try {
//			InetAddress[] lanaddresses = InetAddress.getAllByName(null);
//			System.out.println(Arrays.toString(lanaddresses));
//		} catch (UnknownHostException e1) {
//			e1.printStackTrace();
//		}
//		System.setProperty("jjneko.jjnet.http.server.logLevel", "INFO");
//		
//		
//		
//		HttpService https = new HttpService(7555);
//		UDPService udp = new UDPService(7656);
//		final UPnPService upnp = new UPnPService();	
//		StunServer stun = new StunServer();
//
//		
//		try {
//			https.start();
//			udp.start();
//			stun.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		NodeAdvertisement ad = new NodeAdvertisement(InetAddress.getLocalHost().getHostAddress(), true, true, true,
//				https.getServerPort(), udp.getServerPort(), stun.stunServerPort);
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
//		NodeAdvertisement add = new NodeAdvertisement(InetAddress.getLocalHost().getHostAddress(), true, true, true,
//				https.getServerPort(), udp.getServerPort(), stun.stunServerPort);
//		
//		System.out.println(ad);
//		
//		for(String s : ad.getSuperClasses())
//			System.out.println(s);
//		System.out.println(Advertisement.generateHash(ad));
//		
//		
//		System.out.println(add);
//		for(String s : add.getSuperClasses())
//			System.out.println(s);
//		System.out.println(Advertisement.generateHash(add));
//		
//		String adxml = XML.toUnsignedXML(ad);
//		
//		System.out.println(adxml);
//		
//		NodeAdvertisement ad2 = (NodeAdvertisement) XML.parseUnsignedXML(adxml);
//		
//		AdvertisementService ads = new AdvertisementService();
//		ads.publish(ad2);
//		
//		System.out.println(ad2);
//		System.out.println(ad2.ipAddress);
//		
//		ads.fetchLocal(Advertisement.class.getName());
		
		try{
//			upnp.start();
//			int extHttpPort = upnp.mapPort(https.getServerPort(), "TCP", "http server");
//			int extSTUNPort = upnp.mapPort(stun.stunServerPort, "UDP", "STUN server");
//			int extUDPPort = upnp.mapPort(udp.getServerPort(), "UDP", "UDP server");
//			System.out.println("External HTTP port: "+ extHttpPort);
//			System.out.println("External STUN port: "+ extSTUNPort);
//			System.out.println("External UDP port: "+ extUDPPort);
//
//			
//			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			    public void run() {
//			    	try {
//			    		System.out.println("Meow!");
//						upnp.clearPortMappings();
//					} catch (IOException e) {
//						
//					} catch (SAXException e) {
//						
//					}
//			    }
//			}));
			
			Thread.sleep(4000);
		} catch(Exception e){
			System.out.println("Could not start UPnP: "+ e.getMessage());
		}
		
		JJnet.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ArrayList<Advertisement> ads = JJnet.getAdvertisementService().fetchLocal(WorldGroupAdvertisement.class);
		
		NodeAdvertisement ad = new NodeAdvertisement(InetAddress.getLocalHost().getHostAddress(), true, true, true,
		0, 0, 0);
		NodeAdvertisement ad2 = new NodeAdvertisement(InetAddress.getLocalHost().getHostAddress(), true, true, true,
				0, 0, 354);
		NodeAdvertisement ad3 = new NodeAdvertisement(InetAddress.getLocalHost().getHostAddress(), true, true, true,
				0, 354, 0);
		NodeAdvertisement ad4 = new NodeAdvertisement(InetAddress.getLocalHost().getHostAddress(), true, true, true,
				354, 0, 0);
		
		JJnet.getAdvertisementService().publish(ad);
		JJnet.getAdvertisementService().publish(ad2);
		JJnet.getAdvertisementService().publish(ad3);
		JJnet.getAdvertisementService().publish(ad4);
		
		long beforeTime= System.nanoTime();
		for(int i=0;i<10;i++){
			beforeTime= System.nanoTime();
			JJnet.getWorldGroup().addMember(new EndPoint(SecurityService.generateRSAKeyPair().getPublic()));
			System.out.println("keygen took "+(float)(System.nanoTime()-beforeTime)/1000000f+"ms");
		}
		
		for(int i=0;i<200;i++){
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			JJnet.getAdvertisementService().fetchRemote(NodeAdvertisement.class,2);
		}
	}

}
