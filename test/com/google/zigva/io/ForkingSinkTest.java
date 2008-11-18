// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import junit.framework.TestCase;

public class ForkingSinkTest extends TestCase {

  public void testSimple() throws Exception {
    
    Sink<Character> sink1 = new SinkToString();
    Sink<Character> sink2 = new SinkToString();
    
    @SuppressWarnings("unchecked")
    ForkingSink<Character> sink = new ForkingSink<Character>(sink1, sink2);
    
    String expected = "foo bar baz";
    Source<Character> source = new CharacterSource(expected);

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
