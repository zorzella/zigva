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
