package jjnet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.UUID;

import org.xml.sax.SAXException;

import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.http.HttpService;
import com.jjneko.jjnet.networking.security.SecurityService;
import com.jjneko.jjnet.networking.stun.StunServer;
import com.jjneko.jjnet.networking.udp.UDPService;
import com.jjneko.jjnet.networking.upnp.UPnPService;

public class Tester {
	
	public static void main(String[] args){
		
		JJnet nm = new JJnet();
		nm.init();
		
		try {
			KeyPair kp = SecurityService.generateRSAKeyPair();
			PublicKey pub = kp.getPublic();
			PrivateKey priv = kp.getPrivate();
			
			System.out.println(" public= " + SecurityService.publicKeytoString(pub)
					+"\n   hash= " + SecurityService.hash(SecurityService.publicKeytoString(pub))
					+"\n  again= " + SecurityService.publicKeytoString(SecurityService.parsePublicKey(SecurityService.publicKeytoString(pub))));
			System.out.println("private= " + SecurityService.privateKeytoString(priv)
					 +"\n   hash= " + SecurityService.hash(SecurityService.privateKeytoString(priv))
					 +"\n  again= " + SecurityService.privateKeytoString(SecurityService.parsePrivateKey(SecurityService.privateKeytoString(priv))));
			
			String message = "Hashu";
			System.out.println(message);
			String encrypted = SecurityService.rsaEncrypt(message, pub);
			System.out.println(new String(encrypted));
			String decrypted = SecurityService.rsaDecrypt(encrypted, priv);
			System.out.println(new String(decrypted));
			
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(UUID.randomUUID().hashCode());
		try {
			InetAddress[] lanaddresses = InetAddress.getAllByName(null);
			System.out.println(Arrays.toString(lanaddresses));
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		System.setProperty("jjneko.jjnet.http.server.logLevel", "INFO");
		
		
		
		HttpService https = new HttpService(7555);
		UDPService udp = new UDPService(7656);
		final UPnPService upnp = new UPnPService();	
		StunServer stun = new StunServer();

		
		try {
			https.start();
			udp.start();
			stun.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try{
//			upnp.start();
//			int extHttpPort = upnp.mapPort(https.getServerPort(), "TCP", "http server");
//			int extSTUNPort = upnp.mapPort(StunServer.stunServerPort, "UDP", "STUN server");
//			System.out.println("External HTTP port: "+ extHttpPort);
//			System.out.println("External STUN port: "+ extSTUNPort);
//			
//			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			    public void run() {
//			    	try {
//						upnp.clearPortMappings();
//					} catch (IOException e) {
//						
//					} catch (SAXException e) {
//						
//					}
//			    }
//			}));
//			
		} catch(Exception e){
			System.out.println("Could not start UPnP: "+ e.getMessage());
		}
		
		
	}

}
