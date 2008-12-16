// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import junit.framework.TestCase;

public class ForkingSinkTest extends TestCase {

  public void testSimple() throws Exception {
    
    String expected = "foo bar baz";
    Source<Character> source = new CharacterSource(expected);

    SinkToString sink1 = new SinkToString();
    SinkToString sink2 = new SinkToString();
    
    @SuppressWarnings("unchecked")
    Sink<Character> sink = new ForkingSinkFactory<Character>(
        sink1.asSinkFactory(), 
        sink2.asSinkFactory()).build(source);
    
    while(!source.isEndOfStream()) {
      sink.write(source.read());
    }
    sink.flush();
    sink.close();
    source.close();
    
    assertEquals(expected, sink1.toString());
    assertEquals(expected, sink2.toString());
  }
}
