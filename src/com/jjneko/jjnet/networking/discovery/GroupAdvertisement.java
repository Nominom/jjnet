package com.jjneko.jjnet.networking.discovery;

import com.jjneko.jjnet.networking.EndPoint;

public class GroupAdvertisement extends Advertisement{
	
	public String name;
	public String id;
	public EndPoint owner;
	public boolean anonymous;
	public int peerCount;
	
	public GroupAdvertisement(long validUntil, String name, String id, EndPoint owner,
			boolean anonymous, int peerCount) {
		super(validUntil);
		this.name = name;
		this.id = id;
		this.owner = owner;
		this.anonymous = anonymous;
		this.peerCount = peerCount;
	}
	
	
	@Override
	public String toString() {
		return "GroupAdvertisement [name=" + name + ", id=" + id + ", owner="
				+ owner + ", anonymous=" + anonymous +  ", peerCount =" + peerCount + "]";
	}

	
}
