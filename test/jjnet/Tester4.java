package jjnet;


import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.jjneko.jjnet.messaging.XML;
import com.jjneko.jjnet.networking.http.HttpService;
import com.jjneko.jjnet.networking.security.SecurityService;

public class Tester4 {
		
	public static void main(String[] args){
		long startTime = System.currentTimeMillis();
		
		HttpService ad = new HttpService(5050);
		KeyPair kp = SecurityService.generateRSAKeyPair();
		PrivateKey prikey = kp.getPrivate();
		PublicKey pubkey = kp.getPublic();
		
		try {
			String SignedXml = XML.toSignedXML(ad, prikey);
			for(int i=0;i<60;i++){
				String xmlss = XML.toUnsignedXML(ad);
				System.out.println(xmlss);
			}
			System.out.println(SignedXml);
			System.out.println(XML.isValidSignedXML(SignedXml, pubkey));
			HttpService ad2 = (HttpService)XML.parseSignedXML(SignedXml);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		
		
	
	}

}
