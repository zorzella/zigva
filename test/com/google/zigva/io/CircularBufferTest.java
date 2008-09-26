// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.collections.CircularBuffer;

import junit.framework.TestCase;

public class CircularBufferTest extends TestCase {

  private void enq(CircularBuffer buffer, String string) throws InterruptedException {
    for (char c: string.toCharArray()) {
      buffer.enq(c);
    }
  }

  public void testSunnycase() throws InterruptedException {
    CircularBuffer buffer = new CircularBuffer(100);
    buffer.enq('a');
    assertEquals('a', buffer.deq());
  }

  public void testSunnycaseIterable() throws InterruptedException {
    CircularBuffer buffer = new CircularBuffer(100);
    enq(buffer, "abc");
    assertEquals('a', buffer.deq());
    assertEquals('b', buffer.deq());
    assertEquals('c', buffer.deq());
  }

  public void testWrapAround() throws InterruptedException {
    CircularBuffer buffer = new CircularBuffer(2);
    enq(buffer, "ab");
    assertEquals('a', buffer.deq());
    assertEquals('b', buffer.deq());
    enq(buffer, "cd");
    assertEquals('c', buffer.deq());
    assertEquals('d', buffer.deq());
  }
}
