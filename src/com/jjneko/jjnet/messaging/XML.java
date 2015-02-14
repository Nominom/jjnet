package com.jjneko.jjnet.messaging;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import com.jjneko.jjnet.networking.security.SecurityService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class XML {
	
	private static Logger logger = Logger.getLogger(XML.class.getName());
	
	private static final XStream xstream = new XStream();
	
	/**
	 * Create a signed XML string from any object
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidKeyException 
	 * @throws UnsupportedEncodingException 
	 */
	public static String toSignedXML(Object object, PrivateKey key) throws InvalidKeyException{
		StringWriter sw = new StringWriter();
		CompactWriter cw = new CompactWriter(sw);
		xstream.marshal(object,  cw);
		String xml = sw.toString();
		xml=xml.replace("\n", "");
		String hash = SecurityService.hashAsHex(xml,64);
		String shash = "";
		try{
			String signedHash = SecurityService.rsaEncrypt(hash, key);
			shash=signedHash;
		}catch(IllegalBlockSizeException ex){
			logger.severe(ex.getMessage());
			ex.printStackTrace();
		}catch(BadPaddingException e){
			logger.severe(e.getMessage());
			e.printStackTrace();
		}

		return shash+xml.replace("\n", "");
	}
	
	/**
	 * Checks if a signed XML string is valid
	 * @param signedxml
	 * @param key
	 * @return
	 * @throws InvalidKeyException
	 */
	public static boolean isValidSignedXML(String signedxml, PublicKey key) throws InvalidKeyException{
		try{
			String encrypteHash = signedxml.substring(0, SecurityService.CIPHER_LENGTH);
			String xml = signedxml.substring(SecurityService.CIPHER_LENGTH);
			String hash = SecurityService.hashAsHex(xml,64);
			String decryptedHash = SecurityService.rsaDecrypt(encrypteHash, key);			
			return decryptedHash.equals(hash);
		}catch(Exception ex){
			logger.severe(ex.getMessage());
			return false;
		}
	}
	
	/**
	 * Converts a signed XML string to an object.</br>
	 * Please note, that this method does not check the validity of the signature.</br>
	 * Use isValidSignedXML if you want to validate it first
	 * @param signedxml
	 * @return
	 * @throws InvalidKeyException
	 */
	public static Object parseSignedXML(String signedxml) throws InvalidKeyException{
		try{
			String xml = signedxml.substring(SecurityService.CIPHER_LENGTH);
			return xstream.fromXML(xml);
		}catch(Exception ex){
			logger.severe(ex.getMessage());
			return null;
		}
	}
	
	public static String toUnsignedXML(Object object){
		StringWriter sw = new StringWriter();
		CompactWriter cw = new CompactWriter(sw);
		xstream.marshal(object,  cw);
		return sw.toString();
	}
	
	public static Object parseUnsignedXML(String xml){
		return xstream.fromXML(xml);
	}

}
