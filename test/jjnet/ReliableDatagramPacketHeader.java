package jjnet;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;

import com.jjneko.jjnet.utils.JJNetUtils;

public class ReliableDatagramPacketHeader {
	
	public static final int MAX_SEQ=65535;
	
	public static final int ACK_BYTES=5;
	/** 
	 * seq + flags + ackSeq + ack
	 * */
	public static final int HEADER_LENGTH=5+ACK_BYTES;
	
	/** 
	 * v       <br>
	 * 10000000<br>
	 * Flag for connection reset
	 * */
	static final int FLAG_RESET=0;
	/** 
	 *  v      <br>
	 * 01000000<br>
	 * Flag for if this is a previous packet that did not arrive
	 * */
	static final int FLAG_RETRANSMIT=1;
	/** 
	 *   v     <br>
	 * 00100000<br>
	 * Flag for if this is only an acknowledgement packet and should be discarded
	 * */
	static final int FLAG_ACK=2;
	/** 
	 *        v<br>
	 * 00000001<br>
	 * Flag that this is a final packet and connection should be terminated
	 * */
	static final int FLAG_FINAL=7;
		
	int seq;
	boolean[] flags;
	int ackSeq;
	BitSet ack;
	
	
	
	public ReliableDatagramPacketHeader(int seq, boolean[] flags, int ackSeq, BitSet ack) {
		super();
		this.seq = seq;
		this.flags = flags;
		this.ackSeq = ackSeq;
		this.ack = ack;
	}
	
	void updateFromBytes(byte[] bytes){
		if(bytes==null || bytes.length<HEADER_LENGTH){
			return;
		}
		try{
			seq = byteArrayToUShort(bytes);
			flags = _getFlags(bytes[2]);
			ackSeq = byteArrayToUShort(bytes,3);
			byte[] ackBytes = Arrays.copyOfRange(bytes, 5, HEADER_LENGTH);
			ack = _getAck(ackBytes);
		}catch(Exception ex){
			return;
		}
		
	}

	public static ReliableDatagramPacketHeader fromBytes(byte[] bytes){
		if(bytes==null || bytes.length<HEADER_LENGTH){
			return null;
		}
		try{
			int seq = byteArrayToUShort(bytes);
			boolean[] flags = _getFlags(bytes[2]);
			int ackSeq = byteArrayToUShort(bytes,3);
			byte[] ackBytes = Arrays.copyOfRange(bytes, 5, HEADER_LENGTH);
			BitSet ack = _getAck(ackBytes);
			
			return new ReliableDatagramPacketHeader(seq, flags, ackSeq, ack);
		}catch(Exception ex){
			return null;
		}
	}
	
	protected static boolean[] _getFlags(byte b){
		boolean[] flags = new boolean[8];
		for(int i=0;i<Byte.SIZE;i++){
			flags[i] = (b&-128)==-128;
			b=(byte) (b<<1);
		}
		return flags;
	}
	
	protected static byte _getFlagsByte(boolean[] flags){
		byte b = 0;
		for(int i=0;i<flags.length;i++){
			b=(byte) (b<<1);
			if(flags[i])
				b=(byte) (b|1);
		}
		return b;
	}
	
	protected static BitSet _getAck(byte[] ackBytes){
		return BitSet.valueOf(ackBytes);
	}
	
	protected static byte[] _getAckBytes(BitSet set){
		return set.toByteArray();
		
	}
	
	public byte[] toBytes(){
		byte[] bytes = new byte[HEADER_LENGTH];
		uShortToByteArray(seq, bytes, 0);
		bytes[2] = _getFlagsByte(flags);
		uShortToByteArray(ackSeq, bytes, 3);
		byte[] ackBytes = _getAckBytes(ack);
		System.arraycopy(ackBytes, 0, bytes, 5, ackBytes.length);
		return bytes;
	}
	
	public void insertBytes(byte[] dest){
		if(dest.length<HEADER_LENGTH){
			throw new RuntimeException("Destination _array too short! Header won't fit");
		}
		uShortToByteArray(seq, dest, 0);
		dest[2] = _getFlagsByte(flags);
		uShortToByteArray(ackSeq, dest, 3);
		byte[] ackBytes = _getAckBytes(ack);
		System.arraycopy(ackBytes, 0, dest, 5, ackBytes.length);
	}
	
	@Override
	public String toString() {
		return "ReliableDatagramPacketHeader [raw="+Hex.encodeHexString(toBytes())+" seq=" + seq + ", flags="
				+ Arrays.toString(flags) + ", ackSeq=" + ackSeq + ", ack={" + BinaryCodec.toAsciiString(_getAckBytes(ack)) + "}]";
	}
	
	
	public static int byteArrayToUShort(byte[] b) 
			throws IndexOutOfBoundsException{
			    return   b[1] & 0xFF |
			            (b[0] & 0xFF) << 8;
			}
			
	public static int byteArrayToUShort(byte[] b, int offset) 
			throws IndexOutOfBoundsException{
			    return   b[1+offset] & 0xFF |
			            (b[0+offset] & 0xFF) << 8;
	}

	public static byte[] uShortToByteArray(int a)
	{
	    return new byte[] {  
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
			
	public static void uShortToByteArray(int a, byte[] dest, int offset)
	{ 
	       	dest[offset]=(byte) ((a >> 8) & 0xFF);  
	       	dest[offset+1]=(byte) (a & 0xFF);
	}
	
	public boolean isReset(){
		return flags[FLAG_RESET];
	}
	
	public boolean isRetransmit(){
		return flags[FLAG_RETRANSMIT];
	}
	
	public boolean isFinal(){
		return flags[FLAG_FINAL];
	}
	
	public boolean isAck() {
		return flags[FLAG_ACK];
	}
	
	public int getSeq(){
		return seq;
	}
	
	public static void main(String[] args){
		
		long beforeTime = System.nanoTime();
		for(int i=0;i<MAX_SEQ;i++){
			int seq = i;
			boolean[] flags = {true,true,true,false,false,true,false,true};
			int ackSeq = i;
			BitSet ack=BitSet.valueOf(new byte[]{-1,-1});
			ReliableDatagramPacketHeader hdr1 = new ReliableDatagramPacketHeader(seq,flags,ackSeq,ack);
			byte[] raw = hdr1.toBytes();
			ReliableDatagramPacketHeader hdr2 = ReliableDatagramPacketHeader.fromBytes(raw);
		}
		System.out.println("took "+(double)((System.nanoTime()-beforeTime)/1000000.0f)+"ms");
	}

	

}
