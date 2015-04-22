package jjnet;

import static jjnet.ReliableDatagramPacketHeader.*;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Random;

import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.utils.BitBuffer;
import com.jjneko.jjnet.utils.JJNetUtils;

public class ReliableDatagramSocket {
	
	TIntObjectHashMap<DataTimePair> sentData = new TIntObjectHashMap<DataTimePair>();
	
	private static final int maxbit = 2048;
	BitBuffer sendAcked = new BitBuffer(maxbit);
	BitBuffer shouldResend = new BitBuffer(maxbit);
	BitBuffer received = new BitBuffer(maxbit);

	DatagramSocket socket;
	InetSocketAddress address;

	// Current send and receive sequence numbers
	private int sendSeq = 0;
	private int recSeq = MAX_SEQ;
	private int recTimes=0;
	private int sendTimes=0;

	static final long DEFAULT_RESEND_TIMEOUT=300;
	long resendTimeout=DEFAULT_RESEND_TIMEOUT;
	/** For synchronized keyword */
	Object sendLock = new Object();
	Object receiveLock = new Object();
	
	//testing TODO remove!
	Random r = new Random();
	
	
	long last_window=0;
	int packets_this_window=0;
	long window_length=100;
	float window_size=5;
	int window_min_size=5;
	float window_increase_rate=0.1f;
	float window_decrease_rate=0.5f;
	int window_small_decrease_rate=3;
	
	public void send(byte[] message, boolean resendIfLost) throws IOException {
		if(address==null)return;
		
		boolean flags[] = {false,false,false,false,false,false,false,false};
		BitSet ack = new BitSet(ACK_BYTES*8);
		byte[] msg = new byte[HEADER_LENGTH+message.length];
		DatagramPacket packet = new DatagramPacket(msg,msg.length);
		
		if(packets_this_window>=window_size){
			long sleepTime=last_window+window_length-JJnet.currentTimeMillis();
			if(!(sleepTime<=0)){
				try{
					System.out.println("Sleeping for "+sleepTime+"ms");
					Thread.sleep(sleepTime);
				}catch(Exception ex){}
			}else{
				decreaseWindow();
			}
			last_window=JJnet.currentTimeMillis();
			packets_this_window=0;
			System.out.println("window reset");
		}
		packets_this_window++;
		
		System.out.println("window_size= "+window_size);

		if(shouldResend.get(maxbit-1) && !sendAcked.get(maxbit-1)){
			int seq = JJNetUtils.floorMod(sendSeq-(maxbit-1),MAX_SEQ+1);
			System.out.println(seq);
			synchronized(resendList){
				synchronized(sentData){
					try{
					byte[] dat = sentData.get(seq).data;
					resendList.add(dat);
					sentData.remove(seq);
					}catch(NullPointerException ex){}
				}
			}
			shouldResend.set(maxbit-1, false);
			sendAcked.set(maxbit-1, true);
			System.out.println("overflow! decreasing window");
			decreaseWindow();
		}
		
		synchronized (sendLock) {
			for(int i=0;i<ACK_BYTES*8;i++){
				ack.set(i, received.get(i));;
			}
			ReliableDatagramPacketHeader head = new ReliableDatagramPacketHeader(sendSeq, flags, recSeq, ack);
			head.insertBytes(msg);
			System.arraycopy(message, 0, msg, HEADER_LENGTH, message.length);
			packet.setSocketAddress(address);
			socket.send(packet);
			if(resendIfLost){insertDataPair(message, sendSeq);}
			sendSeq++;
			sendAcked.set(0, false);
			sendAcked.shiftRight(1);
			shouldResend.set(0, resendIfLost);
			shouldResend.shiftRight(1);
			if(sendSeq>MAX_SEQ){sendSeq=0;sendTimes++;}
			
		}
		
		//reset rwsack to zero
		rwsack=0;
		lastAckSent=JJnet.currentTimeMillis();
	}
	
	void resend(byte[] message, Iterator it) throws IOException {
		if(address==null)return;
		
		boolean flags[] = {false,false,false,false,false,false,false,false};
		flags[FLAG_RETRANSMIT]=true;
		BitSet ack = new BitSet(ACK_BYTES*8);
		byte[] msg = new byte[HEADER_LENGTH+message.length];
		DatagramPacket packet = new DatagramPacket(msg,msg.length);

		packets_this_window++;
		
		synchronized (sendLock) {
			if(shouldResend.get(maxbit-1) && !sendAcked.get(maxbit-1)){
				return;
			}
			
			for(int i=0;i<ACK_BYTES*8;i++){
				ack.set(i, received.get(i));;
			}
			ReliableDatagramPacketHeader head = new ReliableDatagramPacketHeader(sendSeq, flags, recSeq, ack);
			head.insertBytes(msg);
			System.arraycopy(message, 0, msg, HEADER_LENGTH, message.length);
			packet.setSocketAddress(address);
			socket.send(packet);
			insertDataPair(message, sendSeq);
			
			sendSeq++;
			sendAcked.set(0, false);
			sendAcked.shiftRight(1);
			shouldResend.set(0, true);
			shouldResend.shiftRight(1);
			if(sendSeq>MAX_SEQ){sendSeq=0;sendTimes++;}
			
		}
		
		//reset rwsack to zero
		rwsack=0;
		lastAckSent=JJnet.currentTimeMillis();
		it.remove();
	}
	
	public void sendAck() throws IOException{
		boolean flags[] = {false,false,false,false,false,false,false,false};
		flags[FLAG_ACK]=true;
		BitSet ack = new BitSet(ACK_BYTES*8);
		byte[] msg = new byte[HEADER_LENGTH];
		DatagramPacket packet = new DatagramPacket(msg,msg.length);

		packets_this_window++;
		
		synchronized (sendLock) {
			if(shouldResend.get(maxbit-1) && !sendAcked.get(maxbit-1)){
				return;
			}
			
			for(int i=0;i<ACK_BYTES*8;i++){
				ack.set(i, received.get(i));;
			}
			ReliableDatagramPacketHeader head = new ReliableDatagramPacketHeader(sendSeq, flags, recSeq, ack);
			head.insertBytes(msg);
			packet.setSocketAddress(address);
			socket.send(packet);

			
			sendSeq++;
			sendAcked.set(0, false);
			sendAcked.shiftRight(1);
			shouldResend.set(0, false);
			shouldResend.shiftRight(1);
			if(sendSeq>MAX_SEQ){sendSeq=0;sendTimes++;}
			
		}
		
		//reset rwsack to zero
		rwsack=0;
		lastAckSent=JJnet.currentTimeMillis();
	}
	
	public void send(byte[] message) throws IOException {
		send(message, true);
	}
	
	
	/**received without sending ack*/
	int rwsack=0;
	long lastAckSent=0;
	
	DatagramPacket dpack = new DatagramPacket(new byte[2048], 2048);

	
	public void receive(ReliableDatagramPacket packet) throws IOException {
		while(true){
			socket.receive(dpack);
			synchronized (receiveLock) {
				if(address==null){
					address = (InetSocketAddress) dpack.getSocketAddress();
				}
				if ((!dpack.getSocketAddress().equals(address))||(dpack.getLength() < HEADER_LENGTH)) {
					continue;
				}
				
				packet.header = ReliableDatagramPacketHeader.fromBytes(dpack
						.getData());
				int dataLength = dpack.getLength() - HEADER_LENGTH;
				packet.data = new byte[dataLength];
				System.arraycopy(dpack.getData(),HEADER_LENGTH, packet.data, 0,
						dataLength);
				packet.addr = address;
				int seq = packet.header.getSeq();
				int hop = Math.abs(seq-recSeq);
				boolean wrapAround = hop>(MAX_SEQ/2);
				
				if(wrapAround){
					hop = seq < recSeq ? MAX_SEQ-recSeq+seq+1 : MAX_SEQ-seq+recSeq+1;
				}

				int ackSeq = packet.header.ackSeq;
				int ackHop = Math.abs(sendSeq-ackSeq);
				if(ackHop>(MAX_SEQ/2)){
					ackHop = (MAX_SEQ)-ackSeq+sendSeq+1;
				}
				
				
				if(seq<recSeq){
					if(wrapAround){
						recSeq=seq;
						received.shiftRight(hop);
						received.set(0, true);
						recTimes++;
					}
					if(hop<maxbit){
						if(received.get(hop))
							continue;
						received.set(hop, true);
						
					}
					
				}else if(seq>recSeq){
					if(!wrapAround){
						recSeq=seq;
					}
					if(hop<maxbit && !wrapAround){
						received.shiftRight(hop);
						received.set(0, true);
					}else if(wrapAround){
						if(received.get(hop))
							continue;
						received.set(hop, true);
					}
				}else{
					continue;
				}
				
				for(int i=0;i<ACK_BYTES*8;i++){
					boolean acked = packet.header.ack.get(i);
					if(acked & ackHop+i<maxbit){
						if(!sendAcked.get(ackHop+i)){
							increaseWindow();
							sendAcked.set(ackHop+i, true);
							shouldResend.set(ackHop+i, false);
							clearDataPair(JJNetUtils.floorMod(ackSeq-i, MAX_SEQ+1));
						}
					}else if( !acked & i == (ACK_BYTES*8)-2){
						sDecreaseWindow();
					}
				}
				if(packet.header.isAck())
					continue;
				
				
				rwsack++;
				if(rwsack>ACK_BYTES*2 | JJnet.currentTimeMillis()-lastAckSent>DEFAULT_RESEND_TIMEOUT/2){
					sendAck();
				}
				break;
			}
		}//while end
	}

	public ReliableDatagramSocket(DatagramSocket socket, InetSocketAddress address) {
		this.socket = socket;
		this.address = address;
		sendAcked.setAll(true);
		received.setAll(true);
		received.set(0, false);
	}
	
	public ReliableDatagramSocket(DatagramSocket socket) {
		this(socket,null);
	}
	
	public void setResendTimeout(long timeout){
		if(timeout<=0)
			resendTimeout=DEFAULT_RESEND_TIMEOUT;
		else
			resendTimeout=timeout;
	}
	
	public void reset(){
		//TODO Send packet with reset flag
		_doReset();
	}
	
	void _doReset(){
		//TODO connection reset
	}

	public static void main(String[] args) throws Exception {
		final ReliableDatagramSocket so1 = new ReliableDatagramSocket(new DatagramSocket(2222), new InetSocketAddress("127.0.0.1",2223));
		final ReliableDatagramSocket so2 = new ReliableDatagramSocket(new DatagramSocket(2223));
		
		final TIntHashSet received=new TIntHashSet();
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				long beforeTime=System.nanoTime();
				for(int i=0;i<1000000;i++){
					try{
						so1.send(JJNetUtils.intToByteArray(i), true);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
				System.out.println("send took "+(double)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");
				
				try{
					Thread.sleep(10000);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				
				for(int i=0;i<1000000;i++){
					if(!received.contains(i)){
//						System.out.println("Packet "+ i + " was not received");
					}
				}
				System.out.println("received!");
				
				System.exit(0);
				
			}
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				ReliableDatagramPacket packet = new ReliableDatagramPacket();
				while(true){
					try {
						so1.receive(packet);
//						System.out.println(packet);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					try {
						so1.checkResend();
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				while(true){
					try{
						so2.send(new byte[]{0,0,0,0}, true);
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				ReliableDatagramPacket packet = new ReliableDatagramPacket();
				while(true){
					try {
						so2.receive(packet);
//						System.out.println(packet.toString());
						received.add(JJNetUtils.byteArrayToInt(packet.getData()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					try {
						so2.checkResend();
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	void insertDataPair(byte[] data, int seq){
		synchronized(sentData){
			sentData.put(seq, new DataTimePair(JJnet.currentTimeMillis(),data));
		}
	}
	
	void markAsDelivered(int seq){
		int hop = Math.abs(sendSeq-seq);
		if(hop>(MAX_SEQ/2)){
			hop = (MAX_SEQ)-seq+sendSeq+1;
			
		}

		if(hop<maxbit){
			shouldResend.set(hop,false);
			sendAcked.set(hop, true);
		}

	}
	
	void clearDataPair(int seq){
		synchronized(sentData){
			sentData.remove(seq);
		}
	}
	
	ArrayList<Integer> removeList = new ArrayList<Integer>();
	ArrayList<byte[]> resendList = new ArrayList<byte[]>();
	public void checkResend(){
		
		
		synchronized(sentData){
			TIntObjectIterator<DataTimePair> it = sentData.iterator();
			long currTime = JJnet.currentTimeMillis();
			while(it.hasNext()){
				it.advance();
				int seq = it.key();
				DataTimePair pair = it.value();
				
				if(currTime-pair.timestamp>resendTimeout){
					resendList.add(pair.data);
					removeList.add(seq);
					markAsDelivered(seq);
				}
			}
			for(int i : removeList){
				sentData.remove(i);
			}
			removeList.clear();
		}
		
		
		//resend
		synchronized(resendList){
			if(!resendList.isEmpty()){
				decreaseWindow();
				System.out.println("Packet loss detected! reducing window");
				Iterator<byte[]> it = resendList.iterator();
				while(it.hasNext()){
					byte[] ba = it.next();
					try {
						resend(ba,it);
						Thread.sleep(10);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	
	//congestion control
	void decreaseWindow(){
		window_size=(int)(window_size*window_decrease_rate);
		if(window_size<window_min_size){
			window_size=window_min_size;
		}
	}
	
	void sDecreaseWindow(){
		window_size=window_size-window_small_decrease_rate;
		if(window_size<window_min_size){
			window_size=window_min_size;
		}
	}
	
	void increaseWindow(){
		window_size+=window_increase_rate;
	}
	
	

	
	private class DataTimePair{
		Long timestamp;
		byte[] data;
		
		public DataTimePair(Long timestamp, byte[] data) {
			super();
			this.timestamp = timestamp;
			this.data = data;
			
		}
	}
	
}
