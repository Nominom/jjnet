package jjnet;

import static jjnet.ReliableDatagramPacketHeader.*;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.concurrent.LinkedTransferQueue;





import java.util.concurrent.TimeUnit;

import org.apache.derby.tools.sysinfo;

import com.jjneko.jjnet.networking.JJnet;
import com.jjneko.jjnet.utils.BitBuffer;
import com.jjneko.jjnet.utils.JJNetUtils;

public class ReliableDatagramSocket2 {
	
	public static final long DEFAULT_SEND_TIMEOUT=10000;
	public long sendTimeout=10000;
	
	private static final int maxbit = 1024;
	BitBuffer sendAcked = new BitBuffer(maxbit);
	BitBuffer shouldResend = new BitBuffer(maxbit);
	BitBuffer received = new BitBuffer(maxbit);
	
	private int sendSeq = 0;
	private int recSeq = MAX_SEQ;
	private int recTimes=0;
	private int sendTimes=0;
	
	LinkedTransferQueue<ReliableDatagramPacket> sendQueue = new LinkedTransferQueue<ReliableDatagramPacket>();
	TIntObjectHashMap<DataTimePair> sentData = new TIntObjectHashMap<DataTimePair>();

	DatagramSocket socket;
	InetSocketAddress address;
	
	boolean active=false;
	
	static final long DEFAULT_RESEND_TIMEOUT=300;
	long resendTimeout=DEFAULT_RESEND_TIMEOUT;
	
	
	long lastCheckResend=0;
	ArrayList<Integer> removeList = new ArrayList<Integer>();
	
	static final long NS_IN_MS = 1000000;
	
	long last_window=0;
	int packets_this_window=0;
	long window_length=100;
	float window_size=1;
	int window_min_size=1;
	float window_increase_rate=0.2f;
	float window_decrease_rate=0.5f;
	int window_small_decrease_rate=3;
	
	Thread sendThread;
	int sendThreadSendRepeatCount=1;

	
	Object receiveLock = new Object();
	
	ReliableDatagramPacket ackPack;
	
	public ReliableDatagramSocket2(DatagramSocket socket, InetSocketAddress address) {
		this.socket = socket;
		this.address = address;
		sendAcked.setAll(true);
		received.setAll(true);
		received.set(0, false);
		
		ackPack = new ReliableDatagramPacket(address, new byte[0], false, false, true, false, false);
		
		start();
	}
	
	void start(){
		if(active)
			return;
		
		
		active=true;
		sendThread = new Thread(new SendThread());
		sendThread.start();
	}
	
	void stop(){
		if(!active)
			return;
		
		active=false;
		try {
			sendThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public void close(){
		stop();
		socket.close();
	}
	
	
	
	public void reset(){
		//TODO Send packet with reset flag
		_doReset();
	}
	
	void _doReset(){
		//TODO connection reset
	}

	
	
	/**
	 * 
	 * @author J00nzu
	 *
	 */
	class SendThread implements Runnable{
		@Override
		public void run() {
			long sleepTime=(long) ((window_length*NS_IN_MS)/window_size); //Time to sleep in nanoseconds
			long currTime=0;//current time in mills
			long lastWindow=0;
			int sentThisWindow=0;
			
			while(active){
				try {
					currTime=JJnet.currentTimeMillis();
					
					/** check if queue has something that needs to be sent and send it */
					if(sentThisWindow<window_size)
						for(int i=0;i<sendThreadSendRepeatCount;i++){
							ReliableDatagramPacket msg = sendQueue.poll();
							if(msg!=null){
								_doSend(msg);
								sentThisWindow++;
							}
						}
					
					/**
					 * Adjust sleep time and number of repeats
					 */
					if(currTime-lastWindow>window_length){
						double windowsizelength = (window_size/(double)window_length);
						if(windowsizelength>1){
							sendThreadSendRepeatCount=(int)windowsizelength;
							sleepTime=1;
						}else{
							sendThreadSendRepeatCount=1;
							sleepTime=(long) (window_length/window_size);
							if(sleepTime==0)
								sleepTime=1;
						}
						if(sentThisWindow==0){
							sleepTime=window_length;
							_decreaseWindow();
						}						
						sentThisWindow=0;
						lastWindow=currTime;
					}
					
					if(currTime-lastCheckResend>resendTimeout){
						//Check resend and update sleepTime
						_checkResend(currTime);
						System.out.println("windowsize= "+window_size);
					}
					
					//Sleep zZzZzZzZzZ
					Thread.sleep(sleepTime);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
		
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
				packet.updateFlagsFromHeader();
				
				
				
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
				
				if(!packet.isAck){
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
				}
				
				for(int i=0;i<ACK_BYTES*8;i++){
					boolean acked = packet.header.ack.get(i);
					if(acked & ackHop+i<maxbit){
						if(!sendAcked.get(ackHop+i)){
							_increaseWindow();
						}
						
						sendAcked.set(ackHop+i, true);
						shouldResend.set(ackHop+i, false);
						_clearDataPair(JJNetUtils.floorMod(ackSeq-i, MAX_SEQ+1));
						
					}else if( !acked & i == (ACK_BYTES*8)-2){
						_sDecreaseWindow();
					}
				}
				
				if(packet.header.isAck())
					continue;
				
				rwsack++;
				if(rwsack>ACK_BYTES*2 | JJnet.currentTimeMillis()-lastAckSent>DEFAULT_RESEND_TIMEOUT/4){
					_sendAck();
				}
				break;
			}
		}//while end
	}
	
	
	void _sendAck() throws IOException{
		boolean flags[] = new boolean[8];
		flags[FLAG_ACK]=true;
		BitSet ack = new BitSet(ACK_BYTES*8);
		for(int i=0;i<ACK_BYTES*8;i++){
			ack.set(i, received.get(i));;
		}
		ReliableDatagramPacketHeader head = new ReliableDatagramPacketHeader(0, flags, recSeq, ack);
		ackPack.setHeader(head);
		
		socket.send(ackPack.toPacket());
		
		rwsack=0;
		lastAckSent=JJnet.currentTimeMillis();
	}
	
	
	public void send(byte[] data, boolean shouldResend) throws IOException{
		ReliableDatagramPacket pack = new ReliableDatagramPacket(address, data, shouldResend, false, false, false, false);
		boolean success=false;
		
		if(sentData.size()>maxbit/2){
			_decreaseWindow();
			System.out.println("resendQueue getting large! reducing window");
		}
		
		try {
			if(sendQueue.size()<sendThreadSendRepeatCount){
				sendQueue.put(pack);
				success=true;
			}else{
				success = sendQueue.tryTransfer(pack, sendTimeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(!success){
			throw new IOException("Connection timed out");
		}
	}
	
	public void send(byte[] data) throws IOException{
		send(data,true);
	}
	
	
	/**
	 * Set timeout for sending messages
	 * @param timeout timeout in milliseconds
	 */
	public void setSendTimeout(long timeout){
		this.sendTimeout=timeout;
	}
	
	void _doSend(ReliableDatagramPacket packet) throws IOException{
		
		if(shouldResend.get(maxbit-1) & !sendAcked.get(maxbit-1)){
			int seq = JJNetUtils.floorMod(sendSeq-(maxbit-1),MAX_SEQ+1);
			byte[] dat=null;
			
			synchronized(sentData){
				try{
					dat = sentData.remove(seq).data;					
				}catch(NullPointerException ex){}
			}
			
			if(dat!=null){
				ReliableDatagramPacket pack = new ReliableDatagramPacket(address, dat, true, true, false, false, false);
				sendQueue.put(pack);
			}

			shouldResend.set(maxbit-1, false);
			sendAcked.set(maxbit-1, true);
			System.out.println("overflow! decreasing window");
			_decreaseWindow();
		}
		
		
		boolean flags[] = new boolean[8];
		flags[FLAG_ACK]=packet.isAck;
		flags[FLAG_RESET]=packet.isReset;
		flags[FLAG_RELIABLE]=packet.shouldResend;
		flags[FLAG_FINAL]=packet.isFinal;
		flags[FLAG_RETRANSMIT]=packet.isRetransmit;
		
		BitSet ack = new BitSet(ACK_BYTES*8);
		
		for(int i=0;i<ACK_BYTES*8;i++){
			ack.set(i, received.get(i));;
		}
		
		ReliableDatagramPacketHeader head = new ReliableDatagramPacketHeader(sendSeq, flags, recSeq, ack);
		
		packet.setHeader(head);
		socket.send(packet.toPacket());
		
		if(packet.shouldResend){_insertDataPair(packet.data, sendSeq);}
		
		sendSeq++;
		sendAcked.set(0, false);
		sendAcked.shiftRight(1);
		shouldResend.set(0, false);
		shouldResend.shiftRight(1);
		if(sendSeq>MAX_SEQ){sendSeq=0;sendTimes++;}
		
		
		//reset rwsack to zero
		rwsack=0;
		lastAckSent=JJnet.currentTimeMillis();
	}
	
	
	void _insertDataPair(byte[] data, int seq){
		synchronized(sentData){
			sentData.put(seq, new DataTimePair(JJnet.currentTimeMillis(),data));
		}
	}
	
	void _markAsDelivered(int seq){
		int hop = Math.abs(sendSeq-seq);
		if(hop>(MAX_SEQ/2)){
			hop = (MAX_SEQ)-seq+sendSeq+1;
			
		}
		if(hop<maxbit){
			shouldResend.set(hop,false);
			sendAcked.set(hop, true);
		}

	}
	
	void _clearDataPair(int seq){
		synchronized(sentData){
			sentData.remove(seq);
		}
	}
	
	
	void _checkResend(long currTime){		
		synchronized(sentData){
			TIntObjectIterator<DataTimePair> it = sentData.iterator();
			while(it.hasNext()){
				it.advance();
				int seq = it.key();
				DataTimePair pair = it.value();
				
				if(currTime-pair.timestamp>resendTimeout){
					ReliableDatagramPacket pack = new ReliableDatagramPacket(address, pair.data, true, true, false, false, false);
					sendQueue.put(pack);
					removeList.add(seq);
					_markAsDelivered(seq);
				}
			}
		}
		
		if(!removeList.isEmpty()){
			_decreaseWindow();
			System.out.println("Packet loss detected! reducing window");
			for(int i : removeList){
				sentData.remove(i);
			}
			removeList.clear();
		}

		
		lastCheckResend=JJnet.currentTimeMillis();
	}
	
	
	
	//congestion control
	void _decreaseWindow(){
		window_size=(int)(window_size*window_decrease_rate);
		if(window_size<window_min_size){
			window_size=window_min_size;
		}
	}
	
	void _sDecreaseWindow(){
		window_size=window_size-window_small_decrease_rate;
		if(window_size<window_min_size){
			window_size=window_min_size;
		}
	}
	
	void _increaseWindow(){
		window_size+=window_increase_rate;
	}
	//congestion control end



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
