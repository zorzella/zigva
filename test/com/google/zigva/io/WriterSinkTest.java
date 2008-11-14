// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import junit.framework.TestCase;

import java.io.StringWriter;

public class WriterSinkTest extends TestCase {

  public void testFoo() throws InterruptedException {
    
    StringWriter out = new StringWriter();
    
    Sink<Character> sink = new WriterSink(out);
    
    String expected = "foo";
    
    CharacterSource source = new CharacterSource(expected);
    while(!source.isEndOfStream()) {
      sink.write(source.read());
    }
    sink.flush();
    sink.close();
    
    // TODO: 
    Thread.sleep(100);
    
    assertEquals(expected, out.toString());
    
  }
  
  public void testBar() {
    System.out.println("foo");
    System.out.close();
    System.out.println("bar");
  }
  
}
