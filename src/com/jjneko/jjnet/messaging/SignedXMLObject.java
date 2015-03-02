package com.jjneko.jjnet.messaging;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.jjneko.jjnet.networking.discovery.Advertisement;

public class SignedXMLObject {
	
	public final String signedBy;
	public final String signedObject;
	
	public SignedXMLObject(String endPointName, Object object, PrivateKey key) throws InvalidKeyException {
		super();
		this.signedBy=endPointName;
		this.signedObject = XML.toSignedXML(object, key);
	}
	
	public Object getObject() throws InvalidKeyException{
		return XML.parseSignedXML(signedObject);
	}
	
	public boolean isValid(PublicKey key) throws InvalidKeyException{
		return XML.isValidSignedXML(signedObject, key);
	}
	

}
