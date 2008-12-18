// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.guice.ZigvaThreadFactory;

import junit.framework.TestCase;

public class ForkingSinkTest extends TestCase {

  public void testSimple() throws Exception {
  for (int i=0; i< 1000; i++ ){
    System.out.println(i);
    dotestSimple();
  }
  }
  
  public void dotestSimple() throws Exception {
    String expected = "foo bar baz";
    Source<Character> source = new CharacterSource(expected);

    SinkToString sink1 = new SinkToString();
    SinkToString sink2 = new SinkToString();
    
    @SuppressWarnings("unchecked")
    NewSink sink = new ForkingSinkFactory<Character>(
        new ZigvaThreadFactory(),
        sink1.asSinkFactory(), 
        sink2.asSinkFactory()).newBuild(source);
    
    sink.run();
    
    assertEquals(expected, sink1.toString());
    assertEquals(expected, sink2.toString());
  }
}
