package com.jjneko.jjnet.networking;

import java.security.PublicKey;

public class EndPoint {
	
	
	public static final int ENDPOINT_ADDRESS_LENGTH = 8;
	
	private String address;
	private PublicKey key;
	
	public EndPoint(String address, PublicKey key) {
		this.address = address;
		this.key = key;
	}

}
