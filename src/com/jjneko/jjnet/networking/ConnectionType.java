package com.jjneko.jjnet.networking;

import java.io.UnsupportedEncodingException;

public enum ConnectionType {
	
	
	/**
	 * Connection using HTTP
	 * 0b0000 0000
	 */
	HTTP((byte)0),
	/**
	 * Connection using TCP
	 * 0b0000 0001
	 */
	TCP((byte)1);
	 
	private byte value;
	
	ConnectionType(byte value){
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
	
	public static ConnectionType fromChar(char ch){
		try {
			byte b = new String(new char[]{ch}).getBytes("ISO-8859-1")[0];
			for(ConnectionType p : ConnectionType.values()){
				if(b==p.value())
				return p;
			}
			return null;
				
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static ConnectionType fromByte(byte b){
		try {
			for(ConnectionType p : ConnectionType.values()){
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
