package com.jjneko.jjnet.networking.http;

import com.jjneko.jjnet.networking.http.server.WebSocketHttpServer;

public class HttpService {

	
    private final int SERVERPORT;
    
    private WebSocketHttpServer server;
    
    
    /**
     * 
     * @param serverPort The port in which the http server will be running. set -1 if you don't want to start a http server
     */
	public HttpService(int serverPort){
		SERVERPORT=serverPort;
		
		server=new WebSocketHttpServer(SERVERPORT);
	}
	
	public void start() throws Exception{
		if(!server.isStarted()){
			server.start();
		}
	}

	public int getServerPort() {
		return SERVERPORT;
	}

}
