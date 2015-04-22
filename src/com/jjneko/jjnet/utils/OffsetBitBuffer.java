package com.jjneko.jjnet.utils;

import static com.jjneko.jjnet.utils.JJNetUtils.floorMod;
import java.util.Arrays;
import java.util.Iterator;

public class OffsetBitBuffer extends BitBuffer implements Iterable<Boolean>{
	int offset=0;
	
	public OffsetBitBuffer(int length){
		super(length);
	}
	
	@Override
	public synchronized void set(int index, boolean value){
		int rindex = _getRealIndex(index);
		if(value){
			_array[_getArrayIndex(rindex)]|=masks[rindex%64];
		}else{
			_array[_getArrayIndex(rindex)]&=~masks[rindex%64];
		}
	}
	
	public synchronized void setAll(boolean value){
		Arrays.fill(_array, value?-1:0);
	}

	public boolean get(int index){
		int rindex = _getRealIndex(index);
		return (_array[_getArrayIndex(rindex)]&masks[rindex%64])==masks[rindex%64];
	}

	private int _getRealIndex(int index){
		return floorMod(index+offset, length());
	}
	private int _getArrayIndex(int realIndex){
		return (int)Math.floor((realIndex)/64.0);
	}
	
	public void shiftLeft(int times){
		if(times>=length()){
			offset=floorMod(offset+times, length());
			setAll(false);
			return;
		}
		for(int i=0;i<times;i++){
			offset++;
			set(length()-1,false);
		}
		offset=floorMod(offset, length());
	}
	
	public void shiftRight(int times){
		if(times>=length()){
			offset=floorMod(offset+times, length());
			setAll(false);
			return;
		}
		for(int i=0;i<times;i++){
			offset--;
			set(0,false);
		}
		offset=floorMod(offset, length());
	}
	
	public int length(){
		return _length;
	}

	@Override
	public Iterator iterator() {
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
