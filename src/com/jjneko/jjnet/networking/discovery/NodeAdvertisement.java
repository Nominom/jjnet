package com.jjneko.jjnet.networking.discovery;

import com.jjneko.jjnet.networking.JJnet;

public class NodeAdvertisement extends Advertisement{
	
	public static final int AD_LIFETIME=1000*60*10;
	public final String ipAddress;
	public final boolean http, tcp, stun;
	public final int httpPort, tcpPort, stunPort;
	

	public NodeAdvertisement(String ipAddress, boolean http,
			boolean tcp, boolean stun,int httpPort, int tcpPort,int stunPort) {
		super(JJnet.currentTimeMillis()+AD_LIFETIME);
		this.ipAddress = ipAddress;
		this.http = http;
		this.tcp = tcp;
		this.stun=stun;
		this.httpPort = httpPort;
		this.tcpPort = tcpPort;
		this.stunPort=stunPort;
	}
	
	@Override
	public String toString() {
		return "NodeAdvertisement [ipAddress=" + ipAddress + ", http=" + http
				+ ", tcp=" + tcp + ", stun=" + stun + ", httpPort=" + httpPort
				+ ", tcpPort=" + tcpPort + ", stunPort=" + stunPort
				+ ", valid_until=" + valid_until + "]";
	}

}
