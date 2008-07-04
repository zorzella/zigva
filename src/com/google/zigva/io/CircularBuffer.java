package com.google.zigva.io;

public class CircularBuffer implements AppendableLite {

  private final char[] buffer;
  private int readPos = 0;
  private int writePos = 0;
  

  public CircularBuffer(int size) {
    this.buffer = new char[size];
  }

  @Override
  public AppendableLite append(char c) {
    
    return this;
  }
  
  
}
