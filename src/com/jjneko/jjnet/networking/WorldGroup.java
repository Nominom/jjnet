package com.jjneko.jjnet.networking;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;

public class WorldGroup extends PeerGroup{
	
	/*TODO Move member list to database? */
	public THashSet<EndPoint> members = new THashSet<EndPoint>();

	
	public WorldGroup(EndPoint owner) {
		super("world", owner, true);
	}

	@Override
	public Collection<EndPoint> getMemberList() {
		return members;
	}

	@Override
	public boolean containsMember(EndPoint e) {
		return members.contains(e);
	}

	@Override
	public void addMember(EndPoint e) {
		members.add(e);
	}

}
