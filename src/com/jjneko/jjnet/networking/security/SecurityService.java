package com.jjneko.jjnet.networking.security;

import java.io.UnsupportedEncodingException;
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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;


import ove.crypto.digest.Blake2b;

public class SecurityService {

	// private static MessageDigest hasher;
	public static final int HASH_MAX_LENGTH = 64;
	public static int BLOCK_SIZE = 128;
	public static int CIPHER_LENGTH = 128;
	private static Blake2b.Digest hasher;
	private static KeyFactory rsaFact;
	private static Cipher cipher;
	private static KeyPairGenerator rsaKpg;

	static {
		try {
			// hasher = MessageDigest.getInstance("BLAKE2");
			hasher = Blake2b.Digest.newInstance(HASH_MAX_LENGTH);
			rsaFact = KeyFactory.getInstance("RSA");
			cipher = Cipher.getInstance("RSA");
			rsaKpg = KeyPairGenerator.getInstance("RSA");
			rsaKpg.initialize(Integer.parseInt(System.getProperty(
					"jjneko.jjnet.security.rsakeylength", "1024")));
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
		}
	}

	public static KeyPair generateRSAKeyPair() {
		KeyPair kp = rsaKpg.genKeyPair();
		return kp;
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
		hasher.update(data.getBytes());
		byte[] hash = hasher.digest();
		return Base64.encodeBase64String(hash);
	}

	public static String hashAsBase64(String data, int length) {
		hasher.update(data.getBytes());
		byte[] hash = hasher.digest();
		return Base64.encodeBase64String(hash).substring(0, length);
	}
	
	public static String hashAsHex(String data) {
		hasher.update(data.getBytes());
		byte[] hash = hasher.digest();
		StringBuilder sb = new StringBuilder(hash.length * 2);
		for (byte b : hash)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}

	public static String hashAsHex(String data, int length) {
		hasher.update(data.getBytes());
		byte[] hash = hasher.digest();
		StringBuilder sb = new StringBuilder();
		for (byte b : hash)
			sb.append(String.format("%02x", b & 0xff));
		if (length > HASH_MAX_LENGTH)
			return sb.toString();
		return sb.toString().substring(0, length);
	}
	
	public static String hash(String data) {
		hasher.update(data.getBytes());
		byte[] hash = hasher.digest();
		try {
			return new String(hash,"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";
		}
	}

	public static String hash(String data, int length) {
		hasher.update(data.getBytes());
		byte[] hash = hasher.digest();
		try {
			return new String(hash,"ISO-8859-1").substring(0,length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";
		}
	}

	public static String rsaEncrypt(String data, PrivateKey key)
			throws IllegalBlockSizeException, InvalidKeyException,
			BadPaddingException {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		try {
			byte[] cipherData = cipher.doFinal(data.getBytes("ISO-8859-1"));
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
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherData = null;
		try {
			cipherData = cipher.doFinal(data.getBytes("ISO-8859-1"));
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
		cipher.init(Cipher.ENCRYPT_MODE, key);
		try {
			byte[] cipherData = cipher.doFinal(data.getBytes("ISO-8859-1"));
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
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherData = null;
		try {
			cipherData = cipher.doFinal(data.getBytes("ISO-8859-1"));
			return new String(cipherData, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return "";

		}
		
	}
}
