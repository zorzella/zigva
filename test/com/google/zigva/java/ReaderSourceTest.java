// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java;

import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;
import com.google.zigva.sh.ReaderSource;

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;

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

  // UNIX-ism
  //TODO: make this really deterministic
  //TODO: think!
  public void suppresstestCloseCase() {
    Reader is = new InputStreamReader(System.in);
    Source<Character> source = new ReaderSource(is, 1);
//    waitUntilReady(source);
    Thread.yield();
    try {
      source.close();
      fail();
    } catch (FailedToCloseException expected) {
    }
    // The thing *really* being tested here is the thread count
  }
}

























