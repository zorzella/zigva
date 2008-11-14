/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
