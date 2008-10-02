// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.collections;

public class CircularBuffer<T> {

  private final T[] buffer;
  private final int capacity;

  private int nextReadPos = 0;
  private int amountOfData = 0;
  private boolean interrupted;

  @SuppressWarnings("unchecked")
  public CircularBuffer(int capacity) {
    this.buffer = (T[]) new Object[capacity];
    this.capacity = capacity;
  }

  /**
   * Causes all threads that are blocked, either on an {@link #enq(Object)} or a
   * {@link #deq()} method to get an {@link InterruptedException}.
   * 
   * <p>Though zigva disavows the use of checked exceptions, java special-cases
   * the {@link InterruptedException}.
   */
  public void interrupt() {
    synchronized(buffer) {
      interrupted = true;
      buffer.notifyAll();
    }
  }
  
  public CircularBuffer<T> enq(T c) throws InterruptedException {
    synchronized(buffer) {
      while (amountOfData == capacity) {
        buffer.wait();
        if (interrupted) {
          throw new InterruptedException();
        }
      }
      buffer[((nextReadPos + amountOfData) % capacity)] = c;
      amountOfData++;
      buffer.notifyAll();
      return this;
    }
  }

  public T deq() throws InterruptedException {
    synchronized(buffer) {
      while (amountOfData == 0) {
        buffer.wait();
        if (interrupted) {
          throw new InterruptedException();
        }
      }
      T result = buffer[nextReadPos];
      nextReadPos = (nextReadPos + 1) % capacity;
      amountOfData--;
      buffer.notifyAll();
      return result;
    }
  }
  
  public int size() {
    return amountOfData;
  }
  
  public Object lock() {
    return buffer;
  }
}
