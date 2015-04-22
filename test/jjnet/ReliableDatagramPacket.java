package jjnet;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class ReliableDatagramPacket {
	
	InetSocketAddress addr;
	ReliableDatagramPacketHeader header;
	byte[] data;
	
	public ReliableDatagramPacket(InetSocketAddress addr,
			ReliableDatagramPacketHeader header, byte[] data) {
		super();
		this.addr = addr;
		this.header = header;
		this.data = data;
	}
	
	public ReliableDatagramPacket() {
		
	}

	public InetSocketAddress getAddr() {
		return addr;
	}
	public ReliableDatagramPacketHeader getHeader() {
		return header;
	}
	public byte[] getData() {
		return data;
	}
	
	public int length(){
		return data.length;
	}

	@Override
	public String toString() {
		return "ReliableDatagramPacket [addr=" + addr + ", header=" + header
				+ ", data=" + Arrays.toString(data) + "]";
	}

}
