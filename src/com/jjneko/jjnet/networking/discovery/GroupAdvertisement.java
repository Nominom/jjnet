package com.jjneko.jjnet.networking.discovery;

import com.jjneko.jjnet.networking.EndPoint;

public class GroupAdvertisement {
	
	public String name;
	public String id;
	public EndPoint owner;
	public boolean anonymous;
	public String className;
	
	public GroupAdvertisement(String name, String id, EndPoint owner,
			boolean anonymous, String className) {
		super();
		this.name = name;
		this.id = id;
		this.owner = owner;
		this.anonymous = anonymous;
		this.className = className;
	}
	
	
	@Override
	public String toString() {
		return "GroupAdvertisement [name=" + name + ", id=" + id + ", owner="
				+ owner + ", anonymous=" + anonymous + ", className="
				+ className + "]";
	}

	
}
