package com.jjneko.jjnet.networking;

import java.io.UnsupportedEncodingException;

public enum NATType {
	
	
	/**
	 * When NAT type can not be specified for some reason
	 * 0b0000 0000
	 */
	UNSPECIFIED((byte)0),
	/**
	 * Endpoint independent NAT
	 * External source ports are preserved on new connections.
	 * 0b0000 0001
	 */
	ENDPOINT_INDEPENDENT((byte)1),
	/**
	 * Endpoint dependent NAT
	 * External source ports are not preserved on new connections, but the next port can be predicted
	 * 0b0000 0010
	 */
	ENDPOINT_DEPENDENT_PREDICTABLE((byte)2),
	/**
	 * Endpoint dependent NAT
	 * External source ports are not preserved on new connections and the next port used can not be easily predicted
	 * 0b0000 0011
	 */
	ENDPOINT_DEPENDENT_UNPREDICTABLE((byte)3);
	 
	private byte value;
	
	NATType(byte value){
		 this.value=value;
	}
	
	public byte value(){
		return value;
	}
	
	public char toChar(){
		try {
			return new String(new byte[]{value}, "ISO-8859-1").charAt(0);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
			return '0';
		}
	}
	
	public static NATType fromChar(char ch){
		try {
			byte b = new String(new char[]{ch}).getBytes("ISO-8859-1")[0];
			for(NATType p : NATType.values()){
				if(b==p.value())
				return p;
			}
			return null;
				
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static NATType fromByte(byte b){
		try {
			for(NATType p : NATType.values()){
				if(b==p.value())
				return p;
			}
			return null;
				
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
