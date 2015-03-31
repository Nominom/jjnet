package com.jjneko.jjnet.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class CircularObjectLockableQueue<T> implements Iterable<T> {

	
	//TODO Add contains(T o) 
	
	
	private static Random rand = new Random();
	private T[] buffer;
	private boolean[] locked;
	private int size;
	private int lockedCount;

	private int tail;
	private int head;

	private int count = 0;
	private T[] randomArr;
	private boolean headIsTail = true;

	@SuppressWarnings("unchecked")
	public CircularObjectLockableQueue(int size) {
		this.size = size;
		buffer = (T[]) new Object[size];
		randomArr = (T[]) new Object[size];
		locked = new boolean[size];
		tail = 0;
		head = -1;
	}

	public void add(T toAdd) {
		if (lockedCount >= size)
			throw new AllSlotsLockedException("All buffer slots are locked!");

		int i = head;
		do {
			i = (i + 1) % size;
		} while (locked[i]);

		buffer[i] = toAdd;
		head = i;
		if (head == tail && !headIsTail) {
			do {
				tail = (tail + 1) % size;
			} while (locked[tail]);
		}
		headIsTail = false;

		updateRandomArr();
	}

	public T get(int index) {
		return randomArr[index];
	}
	
	public int size(){
		return count;
	}
	
	public int lockedCount(){
		return lockedCount();
	}
	
	public boolean isLocked(int index){
		T o = randomArr[index];
		if(o==null){
			return false;
		}
		for (int i = 0; i < size(); i++) {
			if (buffer[i] == o) {
				return locked[i];
			}
		}
		return false;
	}
	
	public boolean isLocked(T o){
		if(o==null){
			return false;
		}
		for (int i = 0; i < size(); i++) {
			if (buffer[i] == o) {
				return locked[i];
			}
		}
		return false;
	}
	
	public void remove(int index){
		T o = randomArr[index];
		if(o==null){
			return;
		}
		for (int i = 0; i < size(); i++) {
			if (buffer[i] == o) {
				buffer[i]=null;
				updateRandomArr();
				break;
			}
		}
	}
	
	public void remove(T o){
		if(o==null){
			return;
		}
		for (int i = 0; i < size(); i++) {
			if (buffer[i] == o) {
				buffer[i]=null;
				updateRandomArr();
				break;
			}
		}
	}

	public T remove() {
		if (lockedCount >= size)
			throw new AllSlotsLockedException("All buffer slots are locked!");
		T t = null;
		int i = tail;
		if (!locked[i]) {
			t = (T) buffer[i];
			buffer[i] = null;
			i = (i + 1) % size;
		} else {
			do {
				i = (i + 1) % size;
			} while (locked[i]);
			t = (T) buffer[i];
			buffer[i] = null;
		}

		tail = i;

		if (tail == 0 ? head == size - 1 : head == tail - 1) {
			head = tail;
			headIsTail = true;
		}

		updateRandomArr();

		return t;
	}

	public T peek() {
		if (lockedCount >= size)
			throw new AllSlotsLockedException("All buffer slots are locked!");
		T t = null;
		int i = tail;
		if (!locked[i]) {
			t = (T) buffer[i];
		} else {
			do {
				i = (i + 1) % size;
			} while (locked[i]);
			t = (T) buffer[i];
		}
		return t;
	}

	public void lock(T t) {
		for (int i = 0; i < size; i++) {
			if (buffer[i] == t) {
				if (!locked[i]) {
					lockedCount++;
					locked[i] = true;
					if (tail == i && lockedCount < size) {
						do {
							tail = (tail + 1) % size;
						} while (locked[tail]);
						headIsTail = true;
					}
				}
			}
		}
	}

	public void unlock(T t) {
		for (int i = 0; i < size; i++) {
			if (buffer[i] == t) {
				if (locked[i]) {
					locked[i] = false;
					lockedCount--;
					if (lockedCount == size - 1) {
						do {
							tail = (tail + 1) % size;
						} while (locked[tail]);
						head = tail;
						headIsTail = true;
					}
				}
			}
		}
	}

	public void lock(int index) {
		if (!locked[index]) {
			lockedCount++;
			locked[index] = true;
			if (tail == index && lockedCount < size) {
				do {
					tail = (tail + 1) % size;
				} while (locked[tail]);
				headIsTail = true;
			}
		}
	}

	public void unlock(int index) {
		if (locked[index]) {
			locked[index] = false;
			lockedCount--;
			if (lockedCount == size - 1) {
				do {
					tail = (tail + 1) % size;
				} while (locked[tail]);
				head = tail;
				headIsTail = true;
			}
		}
	}

	public int indexOf(T o) {
		for (int i = 0; i < size(); i++) {
			if (randomArr[i] == o) {
				return i;
			}
		}
		return -1;
	}

	public T getRandom() {
		if (count == 0)
			return null;
		return randomArr[rand.nextInt(count)];
	}

	private void updateRandomArr() {
		count = 0;
		Arrays.fill(randomArr, null);
		for (T tt : buffer) {
			if (tt != null) {
				randomArr[count++] = tt;
			}
		}
	}

	public String toString() {
		return "CircularObjectLockableQueue(size=" + size + ", head=" + head
				+ ", tail=" + tail + " lockedCount=" + lockedCount + ")";
	}

	// public void print(){
	// System.out.print("[");
	// for(int i=0;i<size;i++){
	// try{
	// Thread.sleep(2);
	// }catch(Exception ex){}
	// if(i==size-1){
	// if(locked[i])
	// System.err.print(buffer[i]);
	// else
	// System.out.print(buffer[i]);
	// }else{
	// if(locked[i])
	// System.err.print(buffer[i]+", ");
	// else
	// System.out.print(buffer[i]+", ");
	// }
	// }
	// System.out.println("]");
	// }

	public void clear() {
		Arrays.fill(buffer, null);
		Arrays.fill(randomArr, null);
		Arrays.fill(locked, false);
		tail = 0;
		head = -1;
		count = 0;
		lockedCount = 0;
		headIsTail = true;
	}

	public static void main(String[] args) {
		CircularObjectLockableQueue<String> b = new CircularObjectLockableQueue<String>(
				50);
		for (int i = 0; i < 100; i++) {
			if (rand.nextBoolean()) {
				System.out.println("add");
				b.add(i + "");
				System.out.println(b);
			} else if (rand.nextBoolean()) {
				System.out.println("lock");
				b.lock(rand.nextInt(b.size));
				System.out.println(b);
			} else if (rand.nextBoolean()) {
				System.out.println("unlock");
				b.unlock(rand.nextInt(b.size));
				System.out.println(b);
			} else if (rand.nextBoolean()) {
				System.out.println("got: " + b.get(rand.nextInt(b.size)));
				b.get(rand.nextInt(b.size));
				System.out.println(b);
			} else if (rand.nextBoolean()) {
				System.out.println("removed: " + b.remove());
				System.out.println(b);
			}
		}

		for (String ss : b) {
			System.out.println(ss);
		}

		for (int i = 0; i < 50; i++) {
			b.unlock(i);
			System.out.println(b);
		}

		for (int i = 0; i < 20; i++) {
			System.out.println("peeked: " + b.peek());
			System.out.println(b);
			System.out.println(b.remove());
		}

		// int index = b.indexOf(s);
		// b.unlock(index);

		System.out.println(b.remove());

		for (String ss : b) {
			System.out.println(ss);
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new COBIterator<T>();
	}

	@SuppressWarnings("hiding")
	class COBIterator<T> implements Iterator<T> {
		int index = 0;

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return index < size;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			return (T) buffer[((index++) + tail) % size];
		}

	}

}
