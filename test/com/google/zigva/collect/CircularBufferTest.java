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

package com.google.zigva.collect;

import com.google.common.testing.junit3.TearDownTestCase;
import com.google.zigva.collections.CircularBuffer;

import java.lang.Thread.State;

public class CircularBufferTest extends TearDownTestCase {

  private static void enq(CircularBuffer<Character> buffer, String string) {
    for (char c: string.toCharArray()) {
      buffer.enq(c);
    }
  }

  public void testSunnycase() {
    CircularBuffer<Character> buffer = new CircularBuffer<Character>(100);
    buffer.enq('a');
    assertEquals(new Character('a'), buffer.deq());
  }

  public void testSunnycaseIterable() {
    CircularBuffer<Character> buffer = new CircularBuffer<Character>(100);
    enq(buffer, "abc");
    assertEquals(new Character('a'), buffer.deq());
    assertEquals(new Character('b'), buffer.deq());
    assertEquals(new Character('c'), buffer.deq());
  }

  public void testWrapAround() {
    CircularBuffer<Character> buffer = new CircularBuffer<Character>(2);
    enq(buffer, "ab");
    assertEquals(new Character('a'), buffer.deq());
    assertEquals(new Character('b'), buffer.deq());
    enq(buffer, "cd");
    assertEquals(new Character('c'), buffer.deq());
    assertEquals(new Character('d'), buffer.deq());
  }

  public void testBlock() throws InterruptedException {
    CircularBuffer<Character> buffer = new CircularBuffer<Character>(2);
    
    MyThread t = new MyThread(buffer);
    t.start();
    
    Thread.sleep(50);
    assertEquals(State.WAITING, t.getState());
    
    assertEquals(new Character('a'), buffer.deq());
    assertEquals(new Character('b'), buffer.deq());
    assertEquals(new Character('c'), buffer.deq());
    assertEquals(new Character('d'), buffer.deq());
    
    Thread.sleep(100);
    assertEquals(State.TERMINATED, t.getState());
  }
  
  private final static class MyThread extends Thread {

    public MyThread(CircularBuffer<Character> buffer) {
      this.buffer = buffer;
    }

    private final CircularBuffer<Character> buffer;

    @Override
    public void run() {
      enq(buffer, "abcd");
    }
    
  }
  
}
