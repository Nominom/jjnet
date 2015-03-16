package com.jjneko.jjnet.networking.discovery;

import com.jjneko.jjnet.networking.EndPoint;
import com.jjneko.jjnet.networking.security.SecurityService;

public class WorldGroupAdvertisement extends GroupAdvertisement{
	
	public static final String NAME="私の名前はおおおおおおおおおおおおおおおおおおおおおおおおあああああああああわわわわわおおおおおおおおおおおおおおおおおおおおおおおおおおおお";
	public static final long VALIDUNTIL=Long.MAX_VALUE;

	public WorldGroupAdvertisement(EndPoint owner,boolean anonymous) {
		super(VALIDUNTIL, NAME, SecurityService.hashAsBase64(NAME), owner, anonymous);
	}

}
