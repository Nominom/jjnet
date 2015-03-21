package com.jjneko.jjnet.networking.security;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;






import org.bouncycastle.jce.provider.BouncyCastleProvider;

import ove.crypto.digest.Blake2b;

public class SecurityService {

	// private static MessageDigest Blakehasher;
	public static final int HASH_MAX_LENGTH = 64;
	public static int BLOCK_SIZE = 128;
	public static int CIPHER_LENGTH = 128;
	private static SecureRandom rand;
	private static int generatedCount=0;
	private static Blake2b.Digest Blakehasher;
	private static KeyFactory rsaFact;
	private static Cipher RSAcipher, AEScipher;
	private static KeyPairGenerator rsaKpg;
	private static KeyGenerator AESgenerator;
	private static int RESEED_THERSHOLD=0;

	static {
		try {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());        
			try {
		        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
		        field.setAccessible(true);
		        field.set(null, java.lang.Boolean.FALSE);
		    } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
		        ex.printStackTrace(System.err);
		    }
			
			rand = SecureRandom.getInstance("SHA1PRNG");
			rand.nextBytes(new byte[16]);
			RESEED_THERSHOLD=Integer.parseInt(System.getProperty(
					"jjneko.jjnet.security.reseedthreshold", (Integer.MAX_VALUE/2)+""));
			
			AEScipher = Cipher.getInstance("AES/CTR/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
			AESgenerator = KeyGenerator.getInstance("AES","BC");
			
			AESgenerator.init(Integer.parseInt(System.getProperty(
					"jjneko.jjnet.security.aeskeylength", "128")));
			
			// Blakehasher = MessageDigest.getInstance("BLAKE2");
			Blakehasher = Blake2b.Digest.newInstance(HASH_MAX_LENGTH);
			rsaFact = KeyFactory.getInstance("RSA");
			RSAcipher = Cipher.getInstance("RSA");
			rsaKpg = KeyPairGenerator.getInstance("RSA");
			rsaKpg.initialize(Integer.parseInt(System.getProperty(
					"jjneko.jjnet.security.rsakeylength", "2048")));
			KeyPair kp = rsaKpg.genKeyPair();
			CIPHER_LENGTH = rsaEncrypt("", kp.getPrivate()).length();
			
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
	}

	public static KeyPair generateRSAKeyPair() {
		return rsaKpg.genKeyPair();
	}
	
	public static Key generateAESKey(){
		return AESgenerator.generateKey();
	}
	
	public static byte[] AESKeyToBytes(Key key){
		return key.getEncoded();
	}
	
	public static Key AESBytesToKey(byte[] bytes){
		return new SecretKeySpec(bytes, "AES");
	}

	public static String privateKeytoString(PrivateKey privateKey) {
		try {
			RSAPrivateKeySpec key = rsaFact.getKeySpec(privateKey,
					RSAPrivateKeySpec.class);
			String mod = Base64.encodeBase64String(key.getModulus().toByteArray());
			String pri = Base64.encodeBase64String(key.getPrivateExponent().toByteArray());
			return mod + ":" + pri;
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String publicKeytoString(PublicKey publicKey) {
		try {
			RSAPublicKeySpec key = rsaFact.getKeySpec(publicKey,
					RSAPublicKeySpec.class);
			String mod = Base64.encodeBase64String(key.getModulus().toByteArray());
			String pub = Base64.encodeBase64String(key.getPublicExponent().toByteArray());
			return mod + ":" + pub;
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PublicKey parsePublicKey(String s) throws ParseException,
			InvalidKeySpecException {
		String[] sarr = s.split(":");
		if (sarr.length != 2)
			throw new ParseException(
					"Invalid key format! check this key for data corruption!",
					0);
		else if (!Base64.isBase64(sarr[0])|| !Base64.isBase64(sarr[1]))
		throw new ParseException(
				"Invalid key format! check this key for data corruption!",
				0);

		BigInteger mod = new BigInteger(Base64.decodeBase64(sarr[0]));
		BigInteger exp = new BigInteger(Base64.decodeBase64(sarr[1]));
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
		return rsaFact.generatePublic(keySpec);
	}

	public static PrivateKey parsePrivateKey(String s) throws ParseException,
			InvalidKeySpecException {
		String[] sarr = s.split(":");
		if (sarr.length != 2)
			throw new ParseException(
					"Invalid key format! check this key for data corruption!",
					0);
		else if (!Base64.isBase64(sarr[0])|| !Base64.isBase64(sarr[1]))
		throw new ParseException(
				"Invalid key format! check this key for data corruption!",
				0);
	
		BigInteger mod = new BigInteger(Base64.decodeBase64(sarr[0]));
		BigInteger exp = new BigInteger(Base64.decodeBase64(sarr[1]));
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(mod, exp);
		return rsaFact.generatePrivate(keySpec);
	}

	public static String hashAsBase64(String data) {
		Blakehasher.update(data.getBytes());
		byte[] hash = Blakehasher.digest();
		return Base64.encodeBase64String(hash);
	}

	public static String hashAsBase64(String data, int length) {
		Blakehasher.update(data.getBytes());
		byte[] hash = Blakehasher.digest();
		return Base64.encodeBase64String(hash).substring(0, length);
	}
	
	public static String hashAsHex(String data) {
		Blakehasher.update(data.getBytes());
		byte[] hash = Blakehasher.digest();
		StringBuilder sb = new StringBuilder(hash.length * 2);
		for (byte b : hash)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}

	public static String hashAsHex(String data, int length) {
		Blakehasher.update(data.getBytes());
		byte[] hash = Blakehasher.digest();
		StringBuilder sb = new StringBuilder();
		for (byte b : hash)
			sb.append(String.format("%02x", b & 0xff));
		if (length > HASH_MAX_LENGTH)
			return sb.toString();
		return sb.toString().substring(0, length);
	}
	
	public static String hash(String data) {
		Blakehasher.update(data.getBytes());
		byte[] hash = Blakehasher.digest();
		try {
			return new String(hash,"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";
		}
	}

	public static String hash(String data, int length) {
		Blakehasher.update(data.getBytes());
		byte[] hash = Blakehasher.digest();
		try {
			return new String(hash,"ISO-8859-1").substring(0,length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";
		}
	}
	
	public static byte[] hash(byte[] data) {
		Blakehasher.update(data);
		return Blakehasher.digest();
		
	}

	public static byte[] hash(byte[] data, int length) {
		Blakehasher.update(data);
		byte[] hash = new byte[length];
		System.arraycopy(Blakehasher.digest(), 0, hash, 0, length);
		return hash;
	}

	public static String rsaEncrypt(String data, PrivateKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException {
		RSAcipher.init(Cipher.ENCRYPT_MODE, key);
		try {
			byte[] cipherData = RSAcipher.doFinal(data.getBytes("ISO-8859-1"));
			return new String(cipherData, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";
		}
	}

	public static String rsaDecrypt(String data, PrivateKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException, DecoderException {
		RSAcipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherData = null;
		try {
			cipherData = RSAcipher.doFinal(data.getBytes("ISO-8859-1"));
			return new String(cipherData, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";

		}
		
	}

	public static String rsaEncrypt(String data, PublicKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException {
		RSAcipher.init(Cipher.ENCRYPT_MODE, key);
		try {
			byte[] cipherData = RSAcipher.doFinal(data.getBytes("ISO-8859-1"));
			return new String(cipherData, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";
		}
	}

	public static String rsaDecrypt(String data, PublicKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException, DecoderException {
		RSAcipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherData = null;
		try {
			cipherData = RSAcipher.doFinal(data.getBytes("ISO-8859-1"));
			return new String(cipherData, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";

		}
		
	}
	
	public static byte[] rsaEncrypt(byte[] data, PrivateKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException {
		RSAcipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherData = RSAcipher.doFinal(data);
		return cipherData;
	}
	public static byte[] rsaEncrypt(byte[] data, PublicKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException {
		RSAcipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherData = RSAcipher.doFinal(data);
		return cipherData;
	}
	
	public static byte[] rsaDecrypt(byte[] data, PrivateKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException {
		RSAcipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherData = RSAcipher.doFinal(data);
		return cipherData;
	}
	public static byte[] rsaDecrypt(byte[] data, PublicKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException {
		RSAcipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherData = RSAcipher.doFinal(data);
		return cipherData;
	}
	
	
	/**
	 * Encrypts a byte array with a random IV <br/>
	 * Make sure the the dest array has a length of at least src.length+16!
	 * 
	 * @param key
	 * @param src
	 * @param srcOff
	 * @param dest
	 * @param destOff
	 * @throws Exception If something goes wrong o,o
	 */
	public static void aesEncrypt(Key key, byte[] src, int srcOff, byte[] dest, int destOff, int length) throws Exception{
		IvParameterSpec ivSpec = nextRandomIv();
		System.arraycopy(ivSpec.getIV(), 0, dest, destOff, 16);
		
		AEScipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
		AEScipher.doFinal(src, srcOff, length, dest, destOff+16);
	}
	
	public static byte[] aesEncrypt(Key key, byte[] src) throws Exception{
		byte[] dest = new byte[src.length+16];
		
		IvParameterSpec ivSpec = nextRandomIv();
		System.arraycopy(ivSpec.getIV(), 0, dest, 0, 16);
		
		AEScipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
		AEScipher.doFinal(src, 0, src.length, dest, 16);
		return dest;
	}
	
	
	/**
	 * Decrypts a byte array with IV appended to the message first 16 bytes
	 * 
	 * @param key
	 * @param src
	 * @param srcOff
	 * @param dest
	 * @param destOff
	 * @param length
	 * @throws Exception
	 */
	public static void aesDecrypt(Key key, byte[] src, int srcOff, byte[] dest, int destOff, int length) throws Exception{
		IvParameterSpec ivSpec = ivFromBytes(src, srcOff);
		
		AEScipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		AEScipher.doFinal(src, srcOff+16, length-16, dest, destOff);
	}
	
	public static byte[] aesDecrypt(Key key, byte[] src) throws Exception{
		byte[] dest = new byte[src.length-16];
		IvParameterSpec ivSpec = ivFromBytes(src, 0);
		
		AEScipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		AEScipher.doFinal(src, 16, src.length-16, dest, 0);
		return dest;
	}
	
	private static IvParameterSpec nextRandomIv(){
		byte[] bytes = new byte[16];
		rand.nextBytes(bytes);
		IvParameterSpec spec = new IvParameterSpec(bytes);
		generatedCount++;
		if(generatedCount>RESEED_THERSHOLD){
			try {
				rand = SecureRandom.getInstance("SHA1PRNG");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			rand.nextBytes(new byte[16]);
			generatedCount=0;
		}
		return spec;
	}
	
	private static IvParameterSpec ivFromBytes(byte[] bytes, int offset) throws IndexOutOfBoundsException{
		byte[] bytes2 = new byte[16];
		System.arraycopy(bytes, offset, bytes2, 0, 16);
		IvParameterSpec spec = new IvParameterSpec(bytes2);
		return spec;
	}
	
	
	public static Key generateAESXORKey(byte[] key1, byte[] key2)throws SecurityException{
		if(key1.length != key2.length){
			throw new SecurityException("Key1 length does not match Key2");
		}
		
		byte[] keyBytes = new byte[key1.length];
		
		for(int i = 0; i< key1.length;i++){
			keyBytes[i] = (byte) (key1[i] ^ key2[i]);
		}
		
		return new SecretKeySpec(keyBytes, "AES");
	}

	public static SecureRandom getRandom() {
		generatedCount++;
		return rand;
	}

	public static void setRandomGenerator(SecureRandom rand) {
		SecurityService.rand = rand;
		generatedCount=0;
	}
}
