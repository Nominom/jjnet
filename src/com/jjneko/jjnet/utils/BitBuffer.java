package com.jjneko.jjnet.utils;


import java.util.Arrays;
import java.util.Iterator;

public class BitBuffer implements Iterable<Boolean>{
	static final long[] masks = new long[64];
	static final long[] rightMasks = new long[64];
	static final long[] leftMasks = new long[64];
	static {
		long masksI=1;
		for(int i=63;i>=0;i--){
			masks[i]=masksI;
			masksI<<=1;
		}
		long lMasksI=0;
		for(int i=0;i<64;i++){
			lMasksI|=masks[i];
			leftMasks[i]=lMasksI;
		}
		long rMasksI=0;
		for(int i=63;i>=0;i--){
			rMasksI|=masks[i];
			rightMasks[63-i]=rMasksI;
		}
	}
	
	
	long[] _array;
	int _length;
	
	public BitBuffer(int length){
		_array= new long[(int)Math.ceil((double)length/64.0)];
		this._length=length;
	}

	public synchronized void set(int index, boolean value){
		if(index>=_length)
			throw new IndexOutOfBoundsException("index="+index+" Buffer.length="+_length);
		if(value){
			_array[_getArrayIndex(index)]|=masks[index%64];
		}else{
			_array[_getArrayIndex(index)]&=~masks[index%64];
		}
	}
	
	public synchronized void setAll(boolean value){
		Arrays.fill(_array, value?-1:0);
		_setHidden();
	}

	public boolean get(int index){
		int remainder=index%64;
		return (_array[_getArrayIndex(index)]&masks[remainder])==masks[remainder];
	}

	private int _getArrayIndex(int realIndex){
		return (int)Math.floor((realIndex)/64.0);
	}
	
	
	public synchronized void shiftLeft(int times){
		while(times>=64){
			times-=63;
			_shiftLeft(63);
		}
		_shiftLeft(times);
	}
	void _shiftLeft(int times){
		long carry = 0;
		long tmpCarry = 0;
		for(int i=_array.length-1;i>=0;i--){
			tmpCarry = _array[i]&(leftMasks[times-1]);
			_array[i]<<=times;
			_array[i]|=carry>>>(64-times);
			carry=tmpCarry;
		}
		
	}
	
	
	public synchronized void shiftRight(int times){
		while(times>=64){
			times-=63;
			_shiftRight(63);
		}
		_shiftRight(times);
		
		//set Hidden bits to 0
		_setHidden();
	}
	void _shiftRight(int times){
		long carry = 0;
		long tmpCarry = 0;
		for(int i=0;i<_array.length;i++){
			tmpCarry = _array[i]&(rightMasks[times-1]);
			_array[i]>>>=times;
			_array[i]|=carry<<(64-times);
			carry=tmpCarry;
		}
	}
	
	void _setHidden(){
		if(_length%64>0){
			_array[_array.length-1]&=~rightMasks[63-(_length%64)];
		}
	}
	
	public int length(){
		return _length;
	}

	@Override
	public Iterator<Boolean> iterator() {
		return new BitBufferIterator();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<length();i++){
			sb.append(get(i)?'1':'0');
		}
		return sb.toString();
	}
	
	// For testing
	public static void main(String args[]){
		for(long l : masks){
			System.out.println(Long.toBinaryString(l));
		}
		for(long l : leftMasks){
			System.out.println(Long.toBinaryString(l));
		}
		for(long l : rightMasks){
			System.out.println(Long.toBinaryString(l));
		}
		
		BitBuffer bf = new BitBuffer(63);
		bf.setAll(true);
		System.out.println(bf);
		bf._shiftLeft(1);
		System.out.println(bf);
		bf.shiftRight(3);
		System.out.println(bf);
		bf._shiftLeft(3);
		System.out.println(bf);
	}
	
	
	class BitBufferIterator implements Iterator<Boolean>{
		
		int i=0;

		@Override
		public boolean hasNext() {
			return i<length();
		}

		@Override
		public Boolean next() {
			return get(i++);
		}
		
	}
}
