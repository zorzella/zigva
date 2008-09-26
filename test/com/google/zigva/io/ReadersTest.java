package com.google.zigva.io;

import com.google.zigva.java.io.Readers;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ArrayBlockingQueue;

public class ReadersTest extends TestCase {

  public void testBlockingQ() throws Exception {
    ArrayBlockingQueue<Character> q = new ArrayBlockingQueue<Character>(1);
    Reader reader = Readers.fromQueue(q);
    MyRunnable myRunnable = new MyRunnable(reader, 2);
    Thread thread = new Thread(myRunnable);
    thread.start();
    
    assertThreadLife(true, thread);
    
    char[] expected = {'z', 'i'};
    q.put('z');
    assertThreadLife(true, thread);

    q.put('i');
    assertThreadLife(false, thread);
    
    assertEquals(String.valueOf(expected), String.valueOf(myRunnable.actual));
  }

  //TODO: test that producer is blocking on "full" queue 
  
  private void assertThreadLife(boolean alive, Thread thread) {
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (alive) {
      assertTrue(thread.isAlive());
    } else {
      assertFalse(thread.isAlive());
    }
  }
  
  private static final class MyRunnable implements Runnable {

    private final Reader reader;
    private final char[] actual;

    public MyRunnable(Reader reader, int size) {
      this.reader = reader;
      this.actual = new char[size];
    }
    
    @Override
    public void run() {
      try {
        reader.read(actual);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
}
