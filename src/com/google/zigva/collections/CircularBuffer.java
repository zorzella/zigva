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

public class CircularBuffer<T> {

  private final T[] buffer;
  private final int capacity;
  private final Object lock;

  private int nextReadPos = 0;
  private int amountOfData = 0;
  private boolean interrupted;

  public CircularBuffer(int capacity) {
    this(capacity, "LOCK");
  }
  
  @SuppressWarnings("unchecked")
  public CircularBuffer(int capacity, Object lock) {
    this.buffer = (T[]) new Object[capacity];
    this.capacity = capacity;
    this.lock = lock;
  }

  /**
   * Causes all threads that are blocked, either on an {@link #enq(Object)} or a
   * {@link #deq()} method to get an {@link InterruptedException}.
   * 
   * <p>Though zigva disavows the use of checked exceptions, java special-cases
   * the {@link InterruptedException}.
   */
  public void interrupt() {
    synchronized(lock) {
      interrupted = true;
      lock.notifyAll();
    }
  }
  
  public CircularBuffer<T> enq(T c) throws InterruptedException {
    synchronized(lock) {
      while (amountOfData == capacity) {
        lock.wait();
        if (interrupted) {
          throw new InterruptedException();
        }
      }
      buffer[((nextReadPos + amountOfData) % capacity)] = c;
      amountOfData++;
      lock.notifyAll();
      return this;
    }
  }

  public T deq() throws InterruptedException {
    synchronized(lock) {
      while (amountOfData == 0) {
        lock.wait();
        if (interrupted) {
          throw new InterruptedException();
        }
      }
      T result = buffer[nextReadPos];
      nextReadPos = (nextReadPos + 1) % capacity;
      amountOfData--;
      lock.notifyAll();
      return result;
    }
  }
  
  public int size() {
    return amountOfData;
  }
  
  public boolean isEmpty() {
    return amountOfData == 0;
  }

  public boolean isFull() {
    return amountOfData == capacity;
  }
  
  /**
   * Blocks the current thread until
   */
  public void blockUntilEmpty() {
    synchronized(lock) {
      while(amountOfData > 0) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
