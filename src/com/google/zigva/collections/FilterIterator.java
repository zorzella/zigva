/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
