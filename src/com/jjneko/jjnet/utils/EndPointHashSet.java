package com.jjneko.jjnet.utils;

import com.jjneko.jjnet.networking.EndPoint;

import gnu.trove.set.hash.THashSet;

public class EndPointHashSet<T> extends THashSet<T>{
	
	static EndPoint ep = new EndPoint("",null);
	@SuppressWarnings("unchecked")
	public synchronized T get(String addr){
		ep.setAddress(addr);
		return (T) _set[index(ep)];
	}

}
