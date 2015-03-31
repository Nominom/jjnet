package com.jjneko.jjnet.networking;

import java.security.PublicKey;

import com.jjneko.jjnet.networking.security.SecurityService;

public class EndPoint {
	
	
	public static final int ENDPOINT_ADDRESS_LENGTH = 8;
	
	private String address;
	private PublicKey key;
	
	public EndPoint(String address, PublicKey key) {
		this.setAddress(address);
		this.setKey(key);
	}
	
	public EndPoint(PublicKey key){
		this(generateAddress(key),key);
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the key
	 */
	public PublicKey getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(PublicKey key) {
		this.key = key;
	}
	
	/**
	 * Returns the hash code for this endpoint <br/>
	 * The hash code is the same as address's hash code
	 */
	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EndPoint other = (EndPoint) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}
	
	public static String generateAddress(PublicKey key){
		return SecurityService.hashAsBase64(
				SecurityService.publicKeytoString(key),
				EndPoint.ENDPOINT_ADDRESS_LENGTH);
	}

	@Override
	public String toString() {
		return address;
	}

	

}
