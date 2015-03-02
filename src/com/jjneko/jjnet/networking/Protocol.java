package com.jjneko.jjnet.networking;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.EnumUtils;


/**
 * Your relevant enum comments
 * 
 * @author You
 */
public enum Protocol {
	
	/**
	 * KEEP ALIVE PACKET<br/>
	 * Used to keep the connection alive<br/>
	 * 0b0000 0000
	 */
	KAP((byte)0),
	/**
	 * NEW PEER PROTOCOL<br/>
	 * Used when a new peer is joining the network<br/>
	 * 0b0000 0001
	 */
	NPP((byte)1),
	/**
	 * UNSAFE UNRELIABLE DIRECT PACKET PROTOCOL<br/>
	 * Used to send unencrypted packets to a specified peer in a group. Packets may or may not be delivered.<br/>
	 * 0b0000 0010
	 */
	UUDPP((byte)2),
	/**
	 * PIPE REQUEST PROTOCOL<br/>
	 * Used when asking a known node for a new pipe<br/>
	 * 0b0000 0011
	 */
	PRP((byte)3),
	/**
	 * DYNAMIC QUERY PROTOCOL<br/>
	 * Used for issuing and relaying queries<br/>
	 * 0b0000 0100
	 */
	DQP((byte)4),
	/**
	 * DYNAMIC QUERY RESPONSE PROTOCOL<br/>
	 * Used for responding to an issued query<br/>
	 * 0b0000 0101
	 */
	DQRP((byte)5),
	/**
	 * ADVERTISEMENT REQUEST PROTOCOL<br/>
	 * Used for fetching remote advertisements from neighbor nodes<br/>
	 * 0b0000 0110
	 */
	ARP((byte)6),
	/**
	 * PEER MULTICAST PROTOCOL<br/>
	 * Used for multicasting messages to all peers in worldgroup
	 * 0b0000 0111
	 */
	PMP((byte)7),
	/**
	 * ADVERTISEMENT REQUEST RESPONSE PROTOCOL<br/>
	 * Used for responding to advertisement requests<br/>
	 * 0b0000 0110
	 */
	ARRP((byte)8);
	 
	private byte value;
	Protocol(byte value){
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
	
	public static Protocol fromChar(char ch){
		try {
			byte b = new String(new char[]{ch}).getBytes("ISO-8859-1")[0];
			for(Protocol p : Protocol.values()){
				if(b==p.value())
				return p;
			}
			return null;
				
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
};
