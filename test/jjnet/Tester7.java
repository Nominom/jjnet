package jjnet;
import java.lang.reflect.Field;
import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.jjneko.jjnet.networking.security.SecurityService;

/**
 * Basic IO example with CTR using AES
 */
public class Tester7 {
	static{
	    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());        
		try {
	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
	        field.setAccessible(true);
	        field.set(null, java.lang.Boolean.FALSE);
	    } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
	        ex.printStackTrace(System.err);
	    }
	}
	
	  public static void main(String[] args) throws Exception {


		  long beforeTime=System.nanoTime();
		  SecurityService.class.newInstance();
		  System.out.println("initialization took "+(float)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");

		  
		  byte[] plain = "meow".getBytes();
		  
		  Key key = SecurityService.AESBytesToKey("abcdefghijklmnop".getBytes());
		  Key keya = SecurityService.AESBytesToKey("abcdefghijklmnop".getBytes());
		  
		  beforeTime=System.nanoTime();
		  for(int i=0;i<200000;i++){
//			  byte[] keyBytes = SecurityService.AESKeyToBytes(key);
//			  key = SecurityService.AESBytesToKey(keyBytes);
			  byte[] encrypted = SecurityService.aesEncrypt(key, plain);
			  byte[] decrypted = SecurityService.aesDecrypt(key, encrypted);
		  }
		  System.out.println("normal AES took "+(float)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");
		  
		  byte[] encrypted = new byte[plain.length+16];
		  byte[] decrypted = new byte[plain.length];
		  
		  beforeTime=System.nanoTime();
		  for(int i=0;i<200000;i++){
//			  byte[] keyBytes = SecurityService.AESKeyToBytes(key);
//			  key = SecurityService.AESBytesToKey(keyBytes);
			  SecurityService.aesEncrypt(key, plain,0,encrypted,0,plain.length);
			  SecurityService.aesDecrypt(key, encrypted,0, decrypted,0, encrypted.length);
		  }
		  System.out.println("fast AES took "+(float)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");
		  
		  beforeTime=System.nanoTime();
		  for(int i=0;i<200000;i++){
			  SecurityService.hash(plain);
			  SecurityService.hash(plain);
		  }
		  System.out.println("blake2b took "+(float)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");
		  
		  beforeTime=System.nanoTime();
		  for(int i=0;i<200000;i++){
			  SecurityService.getRandom().nextBytes(new byte[64]);;
		  }
		  System.out.println("rand took "+(float)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");

//		  System.out.println(new String(SecurityService.aesEncrypt(key, "meow".getBytes())));
		  
		  Key key2 = SecurityService.generateAESKey();
		  
		  byte[] kissa = "Jade".getBytes();
		  
		  System.out.println(new String(kissa));
		  
		  byte[] salattu = SecurityService.aesEncrypt(key2, kissa);
		  
		  System.out.println(new String(salattu));
		  
		  byte[] toinenKissa = SecurityService.aesDecrypt(key2, salattu);
		  
		  System.out.println(new String(toinenKissa));
	  }
}
