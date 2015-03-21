package jjnet;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.PBEKeySpec;

import org.bouncycastle.jce.provider.JCEIESCipher;
import org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory;
import org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.IESParameterSpec;

public class Tester8 {
	
	public static void main(String[] args) throws Exception {
	    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());        

		 KeyPairGenerator kpg;
//		 kpg = KeyPairGenerator.getInstance("ECIES","BC");
		 kpg = new KeyPairGenerator.EC();
		 ECGenParameterSpec ecsp;
		 ecsp = new ECGenParameterSpec("secp256r1");
		 kpg.initialize(ecsp);
		
		 KeyPair kp = kpg.genKeyPair();
		 PrivateKey privKey = kp.getPrivate();
		 PublicKey pubKey = kp.getPublic();
		
		 
		 System.out.println(privKey.toString());
		 System.out.println(pubKey.toString());
		 
		 System.out.println(new String(privKey.getEncoded()));
		 System.out.println(new String(pubKey.getEncoded()));
		 
		 EncodedKeySpec spec = new PKCS8EncodedKeySpec(privKey.getEncoded());
		 
		 PublicKey pubKey2 = org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory.createPublicKeyFromDERStream(pubKey.getEncoded());
		 
		 java.security.KeyFactory fac = java.security.KeyFactory.getInstance("EC","BC");
		 PrivateKey privKey2 = fac.generatePrivate(spec);
		 
		 System.out.println(new String(pubKey2.getEncoded()));
		 System.out.println(new String(privKey2.getEncoded()));
		 
		 System.out.println(privKey2.toString());
		 System.out.println(pubKey2.toString());
		 
		 Cipher cipher = Cipher.getInstance("ECIES");
		 
		 String message = "»V¯à±ÒôÂ·ÉñÈúÅ~ÁNÍ?·?X¾?É?yò¤­";
		 System.out.println(message);
		 
//		 IESParameterSpec params = new IESParameterSpec(null, null, 0);
		 
		 byte[]  d = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
		         byte[]  e = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
		 
		         IESParameterSpec param = new IESParameterSpec(d, e, 128);
		 
			cipher.init(Cipher.ENCRYPT_MODE, privKey, param);
		 
		 
//		 cipher.init(Cipher.ENCRYPT_MODE, privKey);
		 byte[] cipherText = cipher.doFinal(message.getBytes());
		 
		 
		 System.out.println("Cipher:"+new String(cipherText));
		 
		 
//		 cipher.init(Cipher.DECRYPT_MODE, pubKey2);
		 byte[] plain = cipher.doFinal(message.getBytes());
		 
		 
		 System.out.println("decrypted:"+new String(cipherText));
	}


}
