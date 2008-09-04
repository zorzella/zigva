// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.java;

import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.Source;

import junit.framework.TestCase;

import java.io.StringBufferInputStream;

public class InputStreamSourceTest extends TestCase {

  public void testSunnycase() {
    String data = "znjt";
    StringBufferInputStream is = new StringBufferInputStream(data);
    Source source = new InputStreamSource(is);
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
    Source source = new InputStreamSource(is);
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
    Source source = new InputStreamSource(is);
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

  private void waitUntilReady(Source source) {
    while (!source.isReady()) {
      Thread.yield();
    }
  }

  public void testCloseForReadWhileNotBlocked() {
    String data = "zrules";
    StringBufferInputStream is = new StringBufferInputStream(data);
    Source source = new InputStreamSource(is);
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
    Source source = new InputStreamSource(is);
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
}
