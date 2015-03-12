package com.jjneko.jjnet.networking;

import java.util.Collection;

import com.jjneko.jjnet.networking.security.SecurityService;

public abstract class PeerGroup {
	
	public static final int ID_LENGTH=16;
	protected final String name;
	protected final String id;
	protected EndPoint owner;
	protected boolean anonymous;
	
	public PeerGroup(String name, EndPoint owner, boolean anonymous){
		this.id=SecurityService.hashAsBase64(name+owner.getAddress(), ID_LENGTH);
		this.name=name;
		this.owner=owner;
		this.anonymous=anonymous;
	}
	
	public abstract Collection<EndPoint> getMemberList();
	
	public abstract boolean containsMember(EndPoint e);
	
	public abstract void addMember(EndPoint e);

	public EndPoint getOwner() {
		return owner;
	}

	public void setOwner(EndPoint owner) {
		this.owner = owner;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

}
