package com.jjneko.jjnet.networking.discovery;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;

import com.jjneko.jjnet.networking.security.SecurityService;


public abstract class Advertisement{

	public final long valid_until;
	
	public Advertisement(long validuntil){
		valid_until=validuntil;
	}
	
	public static String generateHash(Advertisement ad){
		String fields = "";
		Object someObject = ad;
		for (Field field : someObject.getClass().getFields()) {
			
			    field.setAccessible(true);
			    Object value = null;
				try {
					value = field.get(someObject);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} 
			    if (value != null) {
			        System.out.println(field.getName() + "=" + value);
			        if(!field.getName().equals("valid_until")){
			        	fields+=value;
			        }
			    }
			
		}
		return SecurityService.hash(fields);
	}
	
	public List<String> getSuperClasses() {
		  List<String> classList = new ArrayList<String>();
		  Class<?> classs= this.getClass();
		  Class<?> superclass = classs.getSuperclass();
		  classList.add(classs.getName());
		  classList.add(superclass.getName());
		  while (superclass != null) {   
		    classs = superclass;
		    superclass = classs.getSuperclass();
		    if(superclass!=null)
		    	classList.add(superclass.getName());
		  }
		  return classList;
	}

}
