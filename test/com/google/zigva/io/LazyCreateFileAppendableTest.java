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

import com.google.zigva.io.LazyCreateFileAppendable;
import com.google.zigva.io.ZFile;

import junit.framework.TestCase;

import java.io.File;

public class LazyCreateFileAppendableTest extends TestCase {

  private File tmp;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tmp = new File ("/tmp/LAZYCREATE_TEST.txt");
  }
  
  public void testSimple() throws Exception {
    assertFalse(tmp.exists());
    LazyCreateFileAppendable toTest = new LazyCreateFileAppendable(tmp.toString());
    assertFalse(tmp.exists());
    toTest.append("Test");
    assertTrue(tmp.exists());
    String contents = ZFile.readFileContents(tmp);
    assertEquals("Test", contents);
  }
  
  public void testSimple2() throws Exception {
    assertFalse(tmp.exists());
    LazyCreateFileAppendable toTest = new LazyCreateFileAppendable(tmp);
    assertFalse(tmp.exists());
    toTest.append("Test");
    assertTrue(tmp.exists());
    String contents = ZFile.readFileContents(tmp);
    assertEquals("Test", contents);
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    if ((tmp != null) && tmp.exists()) {
      tmp.delete();
    }
  }
}
