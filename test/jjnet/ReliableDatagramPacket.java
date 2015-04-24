package jjnet;

import static jjnet.ReliableDatagramPacketHeader.HEADER_LENGTH;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class ReliableDatagramPacket {
	
	InetSocketAddress addr;
	ReliableDatagramPacketHeader header;
	byte[] data;
	boolean shouldResend=false;
	boolean isRetransmit=false;
	boolean isAck=false;
	boolean isFinal=false;
	boolean isReset=false;
	
	ReliableDatagramPacket(InetSocketAddress addr,
			ReliableDatagramPacketHeader header, byte[] data) {
		super();
		this.addr = addr;
		this.header = header;
		this.data = data;
		updateFlagsFromHeader();
	}
	
	public ReliableDatagramPacket() {
		
	}
	
	public ReliableDatagramPacket(InetSocketAddress addr, byte[] data, boolean shouldResend, boolean isRetransmit, boolean isAck, boolean isFinal, boolean isReset){
		this.addr = addr;
		this.data = data;
		this.shouldResend=shouldResend;
		this.isRetransmit=isRetransmit;
		this.isAck=isAck;
		this.isFinal=isFinal;
		this.isReset=isReset;
	}

	public InetSocketAddress getAddr() {
		return addr;
	}
	public ReliableDatagramPacketHeader getHeader() {
		return header;
	}
	public void setHeader(ReliableDatagramPacketHeader header){
		this.header=header;
	}
	public boolean shouldResend(){
		return shouldResend;
	}
	public boolean isRetransmit(){
		return isRetransmit;
	}
	public boolean isAck(){
		return isAck;
	}
	public boolean isFinal(){
		return isFinal;
	}
	public boolean isReset(){
		return isReset;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int length(){
		return data.length;
	}
	
	public DatagramPacket toPacket(){
		byte[] msg = new byte[HEADER_LENGTH+data.length];
		DatagramPacket packet = new DatagramPacket(msg,msg.length);
		try{
			header.insertBytes(msg);
			System.arraycopy(data, 0, msg, HEADER_LENGTH, data.length);
			packet.setSocketAddress(addr);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return packet;
	}

	@Override
	public String toString() {
		return "ReliableDatagramPacket [addr=" + addr + ", header=" + header
				+ ", data=" + Arrays.toString(data) + "]";
	}

	public void updateFlagsFromHeader() {
		shouldResend=header.isReliable();
		isRetransmit=header.isRetransmit();
		isAck=header.isAck();
		isFinal=header.isFinal();
		isReset=header.isReset();
	}

}
