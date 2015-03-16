package com.jjneko.jjnet.utils;

import java.net.DatagramSocket;
import java.net.ServerSocket;

public class JJNetUtils {
	public static int byteArrayToInt(byte[] b) 
	throws IndexOutOfBoundsException{
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
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
}
