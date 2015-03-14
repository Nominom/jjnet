package com.jjneko.jjnet.networking.pipes.http;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.networking.Protocol;

public class SimpleHttpServerPipeInitializer implements Runnable{

	private static final long HANDSHAKE_TIMEOUT=5000;
	SimpleHttpServerPipe pipe;
	private static byte[] requestMsg, responseMsg;
	static{
		try {
			requestMsg=(Protocol.PRP.toChar()+"?").getBytes("ISO-8859-1");
			responseMsg=("!").getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public SimpleHttpServerPipeInitializer(SimpleHttpServerPipe pipe) {
		this.pipe = pipe;
	}

	@Override
	public void run() {
		/* TODO better handshake '-' */
		long waitTime=0;
        while(pipe.isEmpty()){
        	try{
        		Thread.sleep(100);
        		waitTime+=100;
        		if(waitTime>HANDSHAKE_TIMEOUT){
        			pipe.setConnected(false);
        			pipe.close();
        			System.out.println("No response received in time");
        			return;
        		}
        	}catch(Exception ex){}
        }
        if(Arrays.equals(pipe.receiveHandshake(), requestMsg)){
        	pipe.send(responseMsg);
        	pipe.setConnected(true);
    		JJnet.addPipe(pipe);
    		System.out.println("Pipe connected!");
        }
        else
        	pipe.close();
	}
	
	
}
