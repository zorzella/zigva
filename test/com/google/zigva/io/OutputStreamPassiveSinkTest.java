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

import com.google.zigva.guice.ZigvaThreadFactory;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

public class OutputStreamPassiveSinkTest extends TestCase {

  public void testSimpleScenario() {
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    PassiveSink<Character> sink = 
      new OutputStreamPassiveSink.Builder(new ZigvaThreadFactory()).create(out);
    
    String expected = "foo";
    
    CharacterSource source = new CharacterSource(expected);
    while(!source.isEndOfStream()) {
      sink.write(source.read());
    }
    sink.flush();
    sink.close();
    
    assertEquals(expected, out.toString());
  }

  public void testAppendableNullIsNotAcceptabler() {
    try {
      new AppendablePassiveSink.Builder(new ZigvaThreadFactory()).create(null);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
}
