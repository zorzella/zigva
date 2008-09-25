// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java;

import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FailedToCloseException;
import com.google.zigva.io.Source;

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

public class InputStreamSourceTest extends TestCase {

  @Override
  protected void runTest() throws Throwable {
    int threadCount = Thread.activeCount();
    super.runTest();
    // We need to make sure we are not leaking threads
    assertEquals(threadCount, Thread.activeCount());
  }
  
  public void testSunnycase() {
    String data = "znjt";
    StringBufferInputStream is = new StringBufferInputStream(data);
    Source<Integer> source = new InputStreamSource(is);
    StringBuilder result = new StringBuilder();
    while (!source.isEndOfStream()) {
      result.append(Character.toChars(source.read()));
    }
    source.close();
    assertEquals("znjt", result.toString());
  }

  public void testEmptyString() {
    String data = "";
    StringBufferInputStream is = new StringBufferInputStream(data);
    Source<Integer> source = new InputStreamSource(is);
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
    StringBufferInputStream is = new StringBufferInputStream(data);
    Source<Integer> source = new InputStreamSource(is);
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

  private void waitUntilReady(Source<Integer> source) {
    while (!source.isReady()) {
      Thread.yield();
    }
  }

  public void testCloseForReadWhileNotBlocked() {
    String data = "zrules";
    StringBufferInputStream is = new StringBufferInputStream(data);
    Source<Integer> source = new InputStreamSource(is);
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
    StringBufferInputStream is = new StringBufferInputStream(data);
    Source<Integer> source = new InputStreamSource(is);
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
    StringBufferInputStream is = new StringBufferInputStream(data);
    Source<Integer> source = new InputStreamSource(is);
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
    FileInputStream is = new FileInputStream("/dev/zero");
    Source<Integer> source = new InputStreamSource(is, 5000);
    waitUntilReady(source);
    // Make sure /dev/zero is ok before doing the real test
    assertEquals(new Integer(0), source.read());
    source.close();
    // The thing *really* being tested here is the thread count
  }

  // UNIX-ism
  //TODO: make this really deterministic
  public void testCloseCaseD() throws FileNotFoundException {
    FileInputStream is = new FileInputStream("/dev/zero");
    Source<Integer> source = new InputStreamSource(is, 1);
    waitUntilReady(source);
    // Make sure /dev/zero is ok before doing the real test
    assertEquals(new Integer(0), source.read());
    Thread.yield();
    source.close();
    // The thing *really* being tested here is the thread count
  }
  
  //TODO: case "C" is not all that interesting, and it's hard to test

  // UNIX-ism
  //TODO: make this really deterministic
  //TODO: this is all very ugly, but I don't think I have much of a choice here
  // I'll leave this test suppressed, since it's a thread leak...
  public void suppresstestCloseCase() {
    InputStream is = System.in;
    Source<Integer> source = new InputStreamSource(is, 1);
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

























