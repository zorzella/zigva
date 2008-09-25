package com.google.zigva.io;

import junit.framework.TestCase;

public class SampleSourceTest extends TestCase {

  public void testFoo() throws Exception {
      
     Source<Integer> source = null;
     Sink sink = null;
     while (!source.isEndOfStream()) {
       sink.write(source.read());
     }
     source.close();
     
     
     
  }
}
