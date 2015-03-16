package jjnet;

import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.jjneko.jjnet.networking.security.SecurityService;

public class Tester5 {
	
	static int count = 20000000;
	
	public static void main(String[] args){
		Runtime runtime = Runtime.getRuntime();
	    long maxMemory = runtime.maxMemory();
	    long allocatedMemory = runtime.totalMemory();
	    long freeMemory = runtime.freeMemory();
	    
	    System.out.println("Java max memory: "+ maxMemory/1024);

//	    sb.append("free memory: " + format.format(freeMemory / 1024) + "<br/>");
//	    sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "<br/>");
//	    sb.append("max memory: " + format.format(maxMemory / 1024) + "<br/>");
//	    sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "<br/>");
	    
	    System.gc();
	    long initialMemory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Initial used memory: "+ initialMemory/1024);
	    
	    test1();
	    
	    System.gc();
	    try{
	    	Thread.sleep(2000);
	    }catch(Exception e){}
	    System.gc();
	    
	    initialMemory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Initial used memory: "+ initialMemory/1024);
	    
	    test2();
	    
	    System.gc();
	    try{
	    	Thread.sleep(2000);
	    }catch(Exception e){}
	    System.gc();
	    
	    initialMemory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Initial used memory: "+ initialMemory/1024);
	    
	    test3();
	    
	    System.gc();
	    try{
	    	Thread.sleep(2000);
	    }catch(Exception e){}
	    System.gc();
	    
	    initialMemory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Initial used memory: "+ initialMemory/1024);
		
	}
	
	public static void test1(){
		Runtime runtime = Runtime.getRuntime();
		System.out.println("java.util hashSet");
	    HashSet<Integer> util = new HashSet<Integer>(2);
	    
	    long beforeTime=System.currentTimeMillis();
	    for(int i=0;i<count;i++){
	    	util.add(i);
	    }
	    System.out.println("add took "+(System.currentTimeMillis()-beforeTime)+"ms");

	    System.gc();
	    long utilMemory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Used memory after util hashSet: "+ utilMemory/1024);

	    beforeTime=System.currentTimeMillis();
	    for(int i=0;i<count;i++){
	    	util.contains(i);
	    }
	    System.out.println("contains took "+(System.currentTimeMillis()-beforeTime)+"ms");
	    
	    util=null;
	}
	
	
	public static void test2(){
		Runtime runtime = Runtime.getRuntime();
		System.out.println("gnu.trove hashSet");
	    TIntHashSet util = new TIntHashSet(2);
	    
	    long beforeTime=System.currentTimeMillis();
	    for(int i=0;i<count;i++){
	    	util.add(i);
	    }
	    System.out.println("add took "+(System.currentTimeMillis()-beforeTime)+"ms");
	    
	    System.gc();
	    long utilMemory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Used memory after trove hashSet: "+ utilMemory/1024);

	     beforeTime=System.currentTimeMillis();
	    for(int i=0;i<count;i++){
	    	util.contains(i);
	    }
	    System.out.println("contains took "+(System.currentTimeMillis()-beforeTime)+"ms");
	    
	    util=null;
	}
	
	public static void test3(){
		Runtime runtime = Runtime.getRuntime();
		System.out.println("Java.util ArrayList");
	    ArrayList<Integer> util = new ArrayList<Integer>(count);
	   
	    long beforeTime=System.currentTimeMillis();
	    for(int i=0;i<count;i++){
	    	util.add(i);
	    }
	    System.out.println("add took "+(System.currentTimeMillis()-beforeTime)+"ms");
	    
	    System.gc();
	    long utilMemory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Used memory after util ArrayList: "+ utilMemory/1024);

	    beforeTime=System.currentTimeMillis();
	    for(int i=0;i<100;i++){
	    	util.contains(i);
	    }
	    System.out.println("contains took "+(System.currentTimeMillis()-beforeTime)+"ms");
	    
	    util=null;
	}

}
