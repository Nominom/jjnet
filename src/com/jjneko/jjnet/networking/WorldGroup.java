package com.jjneko.jjnet.networking;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;

import com.jjneko.jjnet.networking.discovery.WorldGroupAdvertisement;

public class WorldGroup extends PeerGroup{
	
	/*TODO Move member list to database? */
	public THashSet<EndPoint> members = new THashSet<EndPoint>();

	
	public WorldGroup(EndPoint owner) {
		super("world", owner, true);
	}

	public WorldGroup(WorldGroupAdvertisement wgad) {
		super(wgad.name,wgad.owner,wgad.anonymous);
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

	public int getMemberCount() {
		return members.size();
	}

}
