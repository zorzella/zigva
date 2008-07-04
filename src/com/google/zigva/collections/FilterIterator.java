package com.google.zigva.collections;

import com.google.zigva.lang.Lambda;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FilterIterator<T> implements Iterator<T> {

	private Iterator<T> it;
	private T next;
	private boolean hasNext;
	private Lambda<T, Boolean> filter;
	
	public FilterIterator(Iterator<T> it, Lambda<T, Boolean> filter) {
		this.it = it;
		this.filter = filter;
		findNewNext();
	}

	private synchronized T findNewNext() {
		T oldNext = next;
		next = null;
		hasNext = false;
		
		while(it.hasNext()) {
			T temp = it.next();
			if (filter.apply(temp)) {
			  next = temp;
			  hasNext = true;
			  break;
			}
		}
		return oldNext;
		
	}
	public boolean hasNext() {
		return this.hasNext;
	}

	public T next() {
		if (!hasNext) {
			throw new NoSuchElementException ();
		}
		return findNewNext();
		
	}

	public void remove() {
		findNewNext();		
	}
	

}
