// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java.io;

import com.google.common.collect.Lists;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;
import com.google.zigva.java.io.ReaderSource;

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.util.List;

public class ReaderSourceTest extends TestCase {

  @Override
  protected void runTest() throws Throwable {
    int threadCount = Thread.activeCount();
    super.runTest();
    // We need to make sure we are not leaking threads
    assertEquals(threadCount, Thread.activeCount());
  }
  
  public void testSunnycase() {
    String data = "znjt";
    Reader is = new InputStreamReader(new StringBufferInputStream(data));
    Source<Character> source = new ReaderSource(is);
    StringBuilder result = new StringBuilder();
    while (!source.isEndOfStream()) {
      result.append(Character.toChars(source.read()));
    }
    source.close();
    assertEquals("znjt", result.toString());
  }

  public void testEmptyString() {
    String data = "";
    InputStreamReader is = new InputStreamReader(new StringBufferInputStream(data));
    Source<Character> source = new ReaderSource(is);
    StringBuilder result = new StringBuilder();
    waitUntilReady(source);
    while (!source.isEndOfStream()) {
      result.append(Character.toChars(source.read()));
    }
    source.close();
    assertEquals("", result.toString());
  }

  public void testCloseForIsReady() {
    String data = "zrules";
    InputStreamReader is = new InputStreamReader(new StringBufferInputStream(data));
    Source<Character> source = new ReaderSource(is);
    StringBuilder result = new StringBuilder();
    waitUntilReady(source);
    result.append(Character.toChars(source.read()));
    source.close();
    try {
      source.isReady();
      fail();
    } catch (DataSourceClosedException expected) {
    }
  }

  private void waitUntilReady(Source<Character> source) {
    while (!source.isReady()) {
      Thread.yield();
    }
  }

  public void testCloseForReadWhileNotBlocked() {
    String data = "zrules";
    InputStreamReader is = new InputStreamReader(new StringBufferInputStream(data));
    Source<Character> source = new ReaderSource(is);
    StringBuilder result = new StringBuilder();
    waitUntilReady(source);
    result.append(Character.toChars(source.read()));
    source.close();
    try {
      source.read();
      fail();
    } catch (DataSourceClosedException expected) {
    }
  }

  //TODO: implement
  public void testCloseForReadWhileBlocked() {
  }

  public void testReadBeyondEndOfStream() {
    String data = "znjt";
    InputStreamReader is = new InputStreamReader(new StringBufferInputStream(data));
    Source<Character> source = new ReaderSource(is);
    StringBuilder result = new StringBuilder();
    while (!source.isEndOfStream()) {
      result.append(Character.toChars(source.read()));
    }
    try {
      source.read();
      fail();
    } catch (EndOfDataException expected) {
    }
    source.close();
    assertEquals("znjt", result.toString());
  }
  
  public void testCloseTwiceThrows() {
    String data = "znjt";
    InputStreamReader is = new InputStreamReader(new StringBufferInputStream(data));
    Source<Character> source = new ReaderSource(is, 500, 50000);
    source.close();
    try {
      source.close();
      fail();
    } catch (DataSourceClosedException expected) {
    }
  }

  
  // UNIX-ism
  //TODO: this relies on the assumption that we won't read the 5000 chars from
  // /dev/zero before we close the source. Make it deterministic
  public void testCloseCaseA() throws FileNotFoundException {
    Reader is = new InputStreamReader(new FileInputStream("/dev/zero"));
    Source<Character> source = new ReaderSource(is, 5000);
    waitUntilReady(source);
    // Make sure /dev/zero is ok before doing the real test
    Character result = source.read();
    assertEquals(new Character('\0'), result);
    source.close();
    // The thing *really* being tested here is the thread count
  }

  // UNIX-ism
  //TODO: make this really deterministic
  public void testCloseCaseD() throws FileNotFoundException {
    Reader is = new InputStreamReader(new FileInputStream("/dev/zero"));
    Source<Character> source = new ReaderSource(is, 1);
    waitUntilReady(source);
    // Make sure /dev/zero is ok before doing the real test
    assertEquals(new Character('\0'), source.read());
    Thread.yield();
    source.close();
    // The thing *really* being tested here is the thread count
  }
  
  //TODO: case "C" is not all that interesting, and it's hard to test

  //TODO: make this really deterministic
  public void testCloseCaseB() {
    FakeReader is = new FakeReader().block();
    Source<Character> source = new ReaderSource(is, 1);
    Thread.yield();
    try {
      source.close();
      fail();
    } catch (FailedToCloseException expected) {
    }
    is.terminate();
  }
  
  //TODO: make this really deterministic
  public void testCloseWhileBlockingOnIsEOS() {
    FakeReader is = new FakeReader().block();
    final Source<Character> source = new ReaderSource(is, 1);
    
    final StringBuilder result = new StringBuilder();
    
    Thread t = new Thread(new Runnable(){
    
      @Override
      public void run() {
        try {
          source.isEndOfStream();
          result.append("fail");
        } catch (DataSourceClosedException e) {
          result.append("success");
        } catch (RuntimeException e) {
          result.append(e.getStackTrace());
        }
      }
    }, "IsEOS Thread");
    t.start();
    
    Thread.yield();
    try {
      source.close();
      fail();
    } catch (FailedToCloseException expected) {
    }
    try {
      t.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    is.terminate();
    assertEquals("success", result.toString());
    assertEquals(1, is.interruptedExceptions);
  }
  
  /**
   * Fake implementation of an InputStream.
   * 
   * <p>You can set the result with {@link #stubResult}, or you can make it block
   * with {@link #block}.
   * 
   * <p>When blocking, this behaves as closely as I can make it to a real input 
   * stream: it "ignores" an interrupt; the difference is that this actually 
   * honors the interrupt, but locks itself again (until the test sets 
   * {@link #done} to true, so as to reclaim.
   * 
   *  //TODO javadoc more
   */
  private static final class FakeReader extends Reader {

    public Object lock = "LOCK";
    public boolean done;
    public int interruptedExceptions = 0;
    public int stubResult;
    private boolean block;
    private boolean infiniteData = false;

    public FakeReader block() {
      this.block = true;
      return this;
    }
    
    public void terminate() {
      done = true;
      synchronized(lock) {
        lock.notify();
      }
      Thread.yield();
    }

    @Override
    public int read() {
      
      while(!done) {
        if (block) {
          try {
            synchronized(lock) {
              lock.wait();
            }
          } catch (InterruptedException e) {
            // We'll be nasty here, and not return from read under any 
            // circumstance (just like a real InputStream does).
            // the "done" allows the test to clean up after itself.
            interruptedExceptions++;
          }
        } else {
          try {
            return data();
          } catch (RuntimeException e) {}
        }
      }      
      throw new RuntimeException("Terminated.");
    }
    
    private List<Integer> data = Lists.newArrayList(); 
    
    public FakeReader withData(String moreData, boolean eos) {
      block = false;
      for (char c : moreData.toCharArray()) {
        this.data.add((int)c);
      }
      if (eos) {
        this.data.add(-1);
      }
      synchronized (lock) {
        lock.notify();
      }
      return this;
    }
    
    private int data() {
      if (this.data.size() > 0) {
        return this.data.remove(0);
      }
      if (infiniteData) {
        return stubResult;
      }
      block = true;
      throw new RuntimeException();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      throw new UnsupportedOperationException();
//      return 0;
    }
  }  
}
