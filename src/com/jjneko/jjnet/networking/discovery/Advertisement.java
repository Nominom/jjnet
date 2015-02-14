package com.jjneko.jjnet.networking.discovery;

public abstract class Advertisement {
	
	private final long VALID_UNTIL;
	
	public Advertisement(int validuntil){
		VALID_UNTIL=validuntil;
	}

}
