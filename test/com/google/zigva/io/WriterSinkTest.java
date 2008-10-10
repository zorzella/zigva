// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import junit.framework.TestCase;

import java.io.StringWriter;

public class WriterSinkTest extends TestCase {

  public void testFoo() {
    
    StringWriter out = new StringWriter();
    
    Sink<Character> sink = new WriterSink(out);
    
    String expected = "foo";
    
    CharacterSource source = new CharacterSource(expected);
    while(!source.isEndOfStream()) {
      sink.write(source.read());
    }
    
    sink.close();
    
    assertEquals(expected, out.toString());
    
  }
  
}
