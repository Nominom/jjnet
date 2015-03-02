package com.jjneko.jjnet.networking.discovery;

public class NodeAdvertisement extends Advertisement{
	
	public static final int AD_LIFETIME=1000*60*10;
	public final String ipAddress;
	public final boolean http, udp, stun;
	public final int httpPort, udpPort, stunPort;
	

	public NodeAdvertisement(String ipAddress, boolean http,
			boolean udp, boolean stun,int httpPort, int udpPort,int stunPort) {
		super(System.currentTimeMillis()+AD_LIFETIME);
		this.ipAddress = ipAddress;
		this.http = http;
		this.udp = udp;
		this.stun=stun;
		this.httpPort = httpPort;
		this.udpPort = udpPort;
		this.stunPort=stunPort;
	}
	
	@Override
	public String toString() {
		return "NodeAdvertisement [ipAddress=" + ipAddress + ", http=" + http
				+ ", udp=" + udp + ", stun=" + stun + ", httpPort=" + httpPort
				+ ", udpPort=" + udpPort + ", stunPort=" + stunPort
				+ ", valid_until=" + valid_until + "]";
	}

}
