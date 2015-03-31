package jjnet;

import com.jjneko.jjnet.networking.EndPoint;
import com.jjneko.jjnet.networking.security.SecurityService;
import com.jjneko.jjnet.utils.EndPointHashSet;

public class Tester9 {
	
	public static void main(String[] args){
		EndPoint ep = new EndPoint(SecurityService.generateRSAKeyPair().getPublic());
		
		EndPointHashSet<EndPoint> set = new EndPointHashSet<EndPoint>();
		set.add(ep);
		
		EndPoint ep2 = set.get(ep.getAddress());
		
		System.out.println(ep);
		System.out.println(ep2);
	}

}
