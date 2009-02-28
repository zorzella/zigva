// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.io;

import com.google.zigva.exec.impl.SimpleThreadRunner;
import com.google.zigva.lang.ZigvaThreadFactory;

import junit.framework.TestCase;

public class ForkingSinkTest extends TestCase {

  public void testSimple() throws Exception {
    String expected = "foo bar baz";
    Source<Character> source = new CharacterSource(expected);

    PassiveSinkToString sink1 = new PassiveSinkToString();
    PassiveSinkToString sink2 = new PassiveSinkToString();
    
    @SuppressWarnings("unchecked")
    Pump sink = new ForkingSinkFactory<Character>(
        new SimpleThreadRunner(new ZigvaThreadFactory()),
        sink1.asSinkFactory(), 
        sink2.asSinkFactory()).getPumpFor(source);
    
    sink.run();
    
    assertEquals(expected, sink1.asString());
    assertEquals(expected, sink2.asString());
  }
}
