package com.google.zigva.io;

import com.google.zigva.io.ZFile;
import com.google.zigva.io.ZFile.FileCreationFailurePolicy;
import com.google.zigva.lang.RegexReplacement;

import junit.framework.TestCase;


import java.io.File;
import java.util.Date;

public class ZFileTest extends TestCase {

  public void testSimpleFileCreation(){
    File f = new File("/tmp/ztestabcdefg");
    Date now = new Date();
    // The OS clock does not have millisecond precision
    long truncatedTime = now.getTime() / 1000 * 1000;
    ZFile.createFileWithContents(f, "contentsoftestabcdefg",
      FileCreationFailurePolicy.DELETE_DIR_IF_EXISTS_TO_CREATE_FILE, false);
    long lastModified = f.lastModified();
    assertTrue(f.exists());
    assertTrue(truncatedTime <= lastModified);
  }

  public void testReplacementSimple(){
    File f = new File("/tmp/ztestabcdefg");
    ZFile.createFileWithContents(f, "contentsoftestabcdefg",
      FileCreationFailurePolicy.DELETE_DIR_IF_EXISTS_TO_CREATE_FILE, false);
    ZFile.fileReplaceRegexp(f, "of", "zz");
    assertEquals("contentszztestabcdefg", ZFile.readFileContents(f));
  }

  public void testReplacementMultiple(){
    File f = new File("/tmp/ztestabcdefg");
    ZFile.createFileWithContents(f, "contentsoftestabcdefg",
      FileCreationFailurePolicy.DELETE_DIR_IF_EXISTS_TO_CREATE_FILE, false);
    RegexReplacement[] rrs = RegexReplacement.fromPairs(
      "of", "zz", 
      "fg", "ww");
    ZFile.fileReplaceRegexp(f, rrs);
    assertEquals("contentszztestabcdeww", ZFile.readFileContents(f));
  }

  // Tests that if there's nothing to do, the file won't even be touched
  public void testReplacementNoOp() throws InterruptedException{
    File f = new File("/tmp/ztestabcdefg");
    ZFile.createFileWithContents(f, "contentsoftestabcdefg",
      FileCreationFailurePolicy.DELETE_DIR_IF_EXISTS_TO_CREATE_FILE, false);
    long lastModified = f.lastModified();
    Thread.sleep(1000);
    ZFile.fileReplaceRegexp(f, "notinthisfile", "zz");
    assertEquals("contentsoftestabcdefg", ZFile.readFileContents(f));
    assertEquals(lastModified, f.lastModified());
  }

  public void testNonExistingSource(){
    File f = new File("/tmp/ztestabcdefgh");
    assertFalse (f.exists());
    try {
      ZFile.fileReplaceRegexp(f, "of", "zz");
      throw new AssertionError();
    } catch (IllegalArgumentException e) {
      // Good!
    }
  }

}
