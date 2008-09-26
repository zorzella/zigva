// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

public class CircularBuffer {

  private final char[] buffer;
  private final int size;

  private int nextReadPos = 0;
  private int amountOfData = 0;
  private boolean interrupted;

  public CircularBuffer(int size) {
    this.buffer = new char[size];
    this.size = size;
  }

  /**
   * Causes all threads that are blocked, either on an {@link #enq(char)} or a
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
  
  public CircularBuffer enq(char c) throws InterruptedException {
    synchronized(buffer) {
      while (amountOfData == size) {
        buffer.wait();
        if (interrupted) {
          throw new InterruptedException();
        }
      }
      buffer[((nextReadPos + amountOfData) % size)] = c;
      amountOfData++;
      buffer.notifyAll();
      return this;
    }
  }

  public char deq() throws InterruptedException {
    synchronized(buffer) {
      while (amountOfData == 0) {
        buffer.wait();
        if (interrupted) {
          throw new InterruptedException();
        }
      }
      char result = buffer[nextReadPos];
      nextReadPos = (nextReadPos + 1) % size;
      amountOfData--;
      buffer.notifyAll();
      return result;
    }
  }
}
