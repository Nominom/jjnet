package com.jjneko.jjnet.networking.upnp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;


public class UPnPService {
	
	private Logger logger = java.util.logging.Logger.getLogger(this.getClass().getName());
	private InetAddress localAddress;
	private String externalIPAddress;
	private HashMap<Integer, String> mappedPorts = new HashMap<Integer, String>();
	private GatewayDevice device;
	private boolean started=false;
	
	public synchronized void start() throws NoGatewayDeviceFoundException, SocketException, UnknownHostException, IOException, SAXException, ParserConfigurationException{
		logger.info("Starting weupnp");
		GatewayDiscover discover = new GatewayDiscover();
		logger.info("Looking for Gateway Devices");
		discover.discover();
		device = discover.getValidGateway();
		device=new GatewayDevice();
        

		if (null != device) {
		    logger.log(Level.INFO, "Gateway device found.\n{0} ({1})", new Object[]{device.getModelName(), device.getModelDescription()});
		} else {
		    logger.info("No valid gateway device found.");
		    throw new NoGatewayDeviceFoundException("No valid gateway device found! Try activating UPnP in your router and computer settings!");
		}

		localAddress = device.getLocalAddress();
		logger.log(Level.INFO, "Using local address: {0}", localAddress);
		externalIPAddress = device.getExternalIPAddress();
		logger.log(Level.INFO, "External address: {0}", externalIPAddress);
		started=true;
	}
	
	public synchronized int mapPort(int internalPort, String protocol, String desc) throws Exception{
		if(started){
			int extPort=internalPort;
			
			PortMappingEntry portMapping = new PortMappingEntry();
	
			logger.log(Level.INFO, "Attempting to map port {0}", internalPort);
			logger.log(Level.INFO, "Querying device to see if mapping for port {0} already exists", internalPort);
			int retries = Integer.parseInt(System.getProperty("jjneko.jjnet.upnp.upnpretries", "10"));
			
			for(extPort=internalPort;extPort<internalPort+retries;extPort++){
				if (!device.getSpecificPortMappingEntry(extPort,protocol,portMapping)) {
					logger.info("Sending port mapping request for internal:"+internalPort+", external: "+extPort);
					if (device.addPortMapping(extPort,internalPort,localAddress.getHostAddress(),protocol,desc)) {
				        logger.info("Mapping succesful!");
				        mappedPorts.put(extPort, protocol);
				        break;
					}
				}else {
				    logger.log(Level.INFO, "Port was "+extPort+" already mapped. Trying again with {0}",extPort+1);
				}
			}
			return extPort;
		}else{
			throw new Exception("You must start the UPnP service first!");
		}
	}
	
	
	public synchronized void clearPortMappings() throws IOException, SAXException{
		logger.info("Stopping weupnp");
		logger.info("Removing port mappings");
		for(Entry<Integer, String> port : mappedPorts.entrySet()){
			logger.info("Port mapping removed "+port.getKey()+" "+port.getValue());
			device.deletePortMapping(port.getKey(),port.getValue());
		}
		mappedPorts.clear();
		
		logger.info("Removal SUCCESSFUL");
		logger.info("Stopping weupnp");
	}
}
