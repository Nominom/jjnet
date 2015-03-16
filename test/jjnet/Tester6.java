package jjnet;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import com.jjneko.jjnet.messaging.XML;
import com.jjneko.jjnet.networking.security.SecurityService;
import com.jjneko.jjnet.utils.JJNetUtils;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

public class Tester6 {
	
	public static void main(String[] args) throws Exception{
		LZ4Factory factory = LZ4Factory.fastestInstance();

	
		String bbkeystring = "";
		for(int i=0;i< 20;i++){
			XML.toUnsignedXML(SecurityService.generateRSAKeyPair().getPublic());
			bbkeystring+=XML.toUnsignedXML(SecurityService.generateRSAKeyPair().getPublic());
			bbkeystring+=XML.toUnsignedXML(factory);
		}
		byte[] data = bbkeystring.getBytes();
		
		final int decompressedLength = data.length;
		
		System.out.println(decompressedLength);
		
	    long beforeTime=System.nanoTime();
		byte[] compressed = JJNetUtils.compress(data);
		System.out.println("compression took "+(float)(System.nanoTime()-beforeTime)/1000000f+"ms");
		
		beforeTime=System.nanoTime();
	    byte[] decompressed = JJNetUtils.decompress(compressed);
		System.out.println("decompression took "+(float)(System.nanoTime()-beforeTime)/1000000f+"ms");
	    
	    System.out.println(compressed.length);
	    System.out.println(Arrays.equals(data, decompressed));

	}

}
