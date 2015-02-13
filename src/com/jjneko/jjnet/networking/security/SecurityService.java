package com.jjneko.jjnet.networking.security;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;

import ove.crypto.digest.Blake2b;

public class SecurityService {
	
//	private static MessageDigest hasher;
	private static Blake2b.Digest hasher;
	private static KeyFactory rsaFact;
	private static Cipher cipher;
	private static KeyPairGenerator rsaKpg;

	static{
		try {
//			hasher = MessageDigest.getInstance("BLAKE2");
			hasher = Blake2b.Digest.newInstance();
			rsaFact = KeyFactory.getInstance("RSA");
			cipher = Cipher.getInstance("RSA");
			rsaKpg = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	
	public static KeyPair generateRSAKeyPair(){
		rsaKpg.initialize(Integer.parseInt(System.getProperty("jjneko.jjnet.security.rsakeylength", "1024")));
		KeyPair kp = rsaKpg.genKeyPair();
		return kp;
	}
	
	public static String privateKeytoString(PrivateKey privateKey){
		try {
			RSAPrivateKeySpec key = rsaFact.getKeySpec(privateKey,
					  RSAPrivateKeySpec.class);
			return key.getModulus()+":"+key.getPrivateExponent();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String publicKeytoString(PublicKey publicKey){
		try {
			RSAPublicKeySpec key = rsaFact.getKeySpec(publicKey,
					RSAPublicKeySpec.class);
			return key.getModulus()+":"+key.getPublicExponent();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PublicKey parsePublicKey(String s) throws ParseException, InvalidKeySpecException{
		String[] sarr = s.split(":");
		if(sarr.length!=2)
			throw new ParseException("Invalid key format! check this key for data corruption!", 0);
		else if(!sarr[0].matches("[0-9]+")||!sarr[1].matches("[0-9]+"))
			throw new ParseException("Invalid key format! check this key for data corruption!", 0);
		
		BigInteger mod = new BigInteger(sarr[0]);
		BigInteger exp = new BigInteger(sarr[1]);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
	    return rsaFact.generatePublic(keySpec);
	}
	
	public static PrivateKey parsePrivateKey(String s) throws ParseException, InvalidKeySpecException{
		String[] sarr = s.split(":");
		if(sarr.length!=2)
			throw new ParseException("Invalid key format! check this key for data corruption!", 0);
		else if(!sarr[0].matches("[0-9]+")||!sarr[1].matches("[0-9]+"))
			throw new ParseException("Invalid key format! check this key for data corruption!", 0);
		
		BigInteger mod = new BigInteger(sarr[0]);
		BigInteger exp = new BigInteger(sarr[1]);
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(mod, exp);
		return rsaFact.generatePrivate(keySpec);
	}
	
	
	public static String HashtoHex(String data){
		hasher.update(data.getBytes());
		byte[] hash = hasher.digest();
		StringBuilder sb = new StringBuilder(hash.length * 2);
		   for(byte b: hash)
		      sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}
	
	public static String HashtoHex(String data, int length){
		hasher.update(data.getBytes());
		byte[] hash = hasher.digest();
		StringBuilder sb = new StringBuilder();
		for(byte b: hash)
			sb.append(String.format("%02x", b & 0xff));
		/* TODO fix indexOutOfBoundsException */
		return sb.toString().substring(0, length);
	}
	
	public static byte[] rsaEncrypt(byte[] data, PrivateKey key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException{
		  cipher.init(Cipher.ENCRYPT_MODE, key);
		  byte[] cipherData = cipher.doFinal(data);
		  return cipherData;
	}
	
	public static byte[] rsaDecrypt(byte[] data, PrivateKey key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException{
		  cipher.init(Cipher.DECRYPT_MODE, key);
		  byte[] cipherData = cipher.doFinal(data);
		  return cipherData;
	}
	
	public static byte[] rsaEncrypt(byte[] data, PublicKey key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException{
		  cipher.init(Cipher.ENCRYPT_MODE, key);
		  byte[] cipherData = cipher.doFinal(data);
		  return cipherData;
	}
	
	public static byte[] rsaDecrypt(byte[] data, PublicKey key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException{
		  cipher.init(Cipher.DECRYPT_MODE, key);
		  byte[] cipherData = cipher.doFinal(data);
		  return cipherData;
	}
}
