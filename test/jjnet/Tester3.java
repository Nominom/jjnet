package jjnet;

import java.net.Inet4Address;

import com.hoodcomputing.natpmp.ExternalAddressRequestMessage;
import com.hoodcomputing.natpmp.MapRequestMessage;
import com.hoodcomputing.natpmp.NatPmpDevice;
import com.hoodcomputing.natpmp.NatPmpException;

public class Tester3 {
	
	public static void main(String[] args) {
		NatPmpDevice pmpDevice = null;

        try {
            // To find the device, simply construct the class. An exception is
            // thrown if the device cannot be located or if the network is not
            // RFC1918.
            // When the device is constructed, you have to tell it whether you
            // want it to automatically shutdown with the JVM or if you'll take
            // the responsibility of shutting it down yourself. Refer to the
            // constructor documentation for the details. In this case, we'll
            // let it shut down with the JVM.
        	System.out.println("Getting NAT PMP device...");
            pmpDevice = new NatPmpDevice(true);
            System.out.println("PMP device found");
            // The next step is always to determine the external address of
            // the device. This is done by constructing the request message
            // and enqueueing it.
            ExternalAddressRequestMessage extAddr = new ExternalAddressRequestMessage(null);
            System.out.println("Queuing message");
            pmpDevice.enqueueMessage(extAddr);

            // In this example, we want to purposefully wait until the queue is
            // empty. It is possible to receive notification when the operation
            // is complete. Refer to the documentation for the
            // ExternalAddressRequestMessage constructor.
            pmpDevice.waitUntilQueueEmpty();
            System.out.println("Message delivered");

            // We can try and get the external address to determine if the
            // gateway is functional.
            // This may throw an exception if there was an error receiving the
            // response. The method getResponseException() would also return an
            // exception object in this case, if you prefer avoiding using
            // try/catch for logic.
            Inet4Address extIP = extAddr.getExternalAddress();
            
            System.out.println("external: " + extIP.toString());

            // Now, we can set up a port mapping. Refer to the javadoc for
            // the parameter values. This message sets up a TCP redirect from
            // a gateway-selected available external port to the local port
            // 5000. The lifetime is 120 seconds. In implementation, you would
            // want to consider having a longer lifetime and periodicly sending
            // a MapRequestMessage to prevent it from expiring.
            System.out.println("Attempting port mapping...");
            MapRequestMessage map = new MapRequestMessage(false, 5000, 0, 120, null);
            pmpDevice.enqueueMessage(map);
            pmpDevice.waitUntilQueueEmpty();

            // Let's find out what the external port is.
            int extPort = map.getExternalPort();
            System.out.println("Port mapping successful! external: " + extPort);


            // All set!
            
            // Please refer to the javadoc if you run into trouble. As always,
            // contact a developer on the SourceForge project or post in the
            // forums if you have questions.
        } catch (NatPmpException ex) {
        }
	}

}
