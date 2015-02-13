package com.jjneko.jjnet.networking;

public enum NATType {
	
	
	/**
	 * When NAT type has not been specified yet
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

}
