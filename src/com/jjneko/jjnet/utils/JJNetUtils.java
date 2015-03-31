package com.jjneko.jjnet.utils;

import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import com.jjneko.jjnet.networking.JJnet;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

public class JJNetUtils {
	
	static LZ4Factory factory = LZ4Factory.fastestInstance();
	static LZ4Compressor compressor = factory.fastCompressor();
	static LZ4FastDecompressor decompressor = factory.fastDecompressor();
	
	private static ByteBuffer longbuffer = ByteBuffer.allocate(Long.BYTES);
	private static ReentrantLock bufferLock = new ReentrantLock();

    public static void longToBytes(long x, byte[] dest, int offset) {
    	bufferLock.lock();
    	try{
    		longbuffer.clear();
	        longbuffer.putLong(0, x);
	        System.arraycopy(longbuffer.array(), 0, dest, offset, Long.BYTES);
	        
    	}finally{
    		bufferLock.unlock();
    	}
    }

    public static long bytesToLong(byte[] src, int offset) {
    	bufferLock.lock();
    	try{
    		longbuffer.clear();
	        longbuffer.put(src, offset, Long.BYTES);
	        longbuffer.flip();
	        return longbuffer.getLong();
    	}finally{
    		bufferLock.unlock();
    	}
    }
	
	
	public static int byteArrayToInt(byte[] b) 
	throws IndexOutOfBoundsException{
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	
	public static int byteArrayToInt(byte[] b, int offset) 
			throws IndexOutOfBoundsException{
			    return   b[3+offset] & 0xFF |
			            (b[2+offset] & 0xFF) << 8 |
			            (b[1+offset] & 0xFF) << 16 |
			            (b[0+offset] & 0xFF) << 24;
			}

	public static byte[] intToByteArray(int a)
	{
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	
	public static void intToByteArray(int a, byte[] dest, int offset)
	{
	       	dest[offset]=(byte) ((a >> 24) & 0xFF);
	       	dest[offset+1]=(byte) ((a >> 16) & 0xFF);   
	       	dest[offset+2]=(byte) ((a >> 8) & 0xFF);  
	       	dest[offset+3]=(byte) (a & 0xFF);
	    
	}
	
	public static int getRandomAvailablePort(){
		int port=2048;
		boolean br=false;
		while(port<49151 && !br){
			try{
				new ServerSocket(port).close();
				new DatagramSocket(port).close();
				br=true;
			}catch(Exception ex){
				port++;
			}
		}
		return port;
	}
	
	
	public static byte[] compress(byte[] data) throws Exception{
		int maxCompressedLength = compressor.maxCompressedLength(data.length);
		byte[] compressed = new byte[maxCompressedLength+4];
		int compressedLength = compressor.compress(data, 0, data.length, compressed, 0, maxCompressedLength);
		compressed = Arrays.copyOf(compressed, compressedLength+4);
		byte[] maxlenbytes = intToByteArray(data.length);
		System.arraycopy(compressed, 0, compressed, 4, compressed.length-4);
		compressed[0]=maxlenbytes[0];
		compressed[1]=maxlenbytes[1];
		compressed[2]=maxlenbytes[2];
		compressed[3]=maxlenbytes[3];
		return compressed;
	}
	
	public static byte[] compress(byte[] src, int offset, int length) throws Exception{
		int maxCompressedLength = compressor.maxCompressedLength(length);
		byte[] compressed = new byte[maxCompressedLength+4];
		int compressedLength = compressor.compress(src, offset, length, compressed, 0, maxCompressedLength);
		compressed = Arrays.copyOf(compressed, compressedLength+4);
		byte[] maxlenbytes = intToByteArray(length);
		System.arraycopy(compressed, 0, compressed, 4, compressed.length-4);
		compressed[0]=maxlenbytes[0];
		compressed[1]=maxlenbytes[1];
		compressed[2]=maxlenbytes[2];
		compressed[3]=maxlenbytes[3];
		return compressed;
	}
	
	public static byte[] decompress(byte[] compressed) throws Exception{
		int decompressedLength = compressed[3] & 0xFF |
	            (compressed[2] & 0xFF) << 8 |
	            (compressed[1] & 0xFF) << 16 |
	            (compressed[0] & 0xFF) << 24;
		if(decompressedLength>JJnet.BUFFER_LENGTH){
			throw new BufferOverflowException();
		}
		byte[] restored = new byte[decompressedLength];
		decompressor.decompress(compressed, 4, restored, 0, decompressedLength);
		return restored;	
	}
	
	public static byte[] decompress(byte[] compressed, int offset) throws Exception{
		int decompressedLength = compressed[3+offset] & 0xFF |
	            (compressed[2+offset] & 0xFF) << 8 |
	            (compressed[1+offset] & 0xFF) << 16 |
	            (compressed[0+offset] & 0xFF) << 24;
		
		if(decompressedLength>JJnet.BUFFER_LENGTH){
			throw new BufferOverflowException();
		}
		
		byte[] restored = new byte[decompressedLength];
		decompressor.decompress(compressed, 4+offset, restored, 0, decompressedLength);
		return restored;	
	}
}
