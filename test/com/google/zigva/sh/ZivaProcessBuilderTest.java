package com.google.zigva.sh;

import com.google.zigva.io.FileRepository;
import com.google.zigva.io.StubZystem;
import com.google.zigva.sh.JavaProcessExecutor;
import com.google.zigva.sh.StubFileRepository;
import com.google.zigva.sh.StubJavaProcessExecutor;
import com.google.zigva.sh.ZivaProcessBuilder;

import junit.framework.TestCase;



public class ZivaProcessBuilderTest extends TestCase {

  public void testCmdIsNecessary() throws Exception {
    FileRepository fileRepository = new StubFileRepository();
    JavaProcessExecutor javaProcessExecutor = new StubJavaProcessExecutor();
    StubZystem stubZystem = new StubZystem();
    ZivaProcessBuilder zivaProcessBuilder = 
      new ZivaProcessBuilder(stubZystem, javaProcessExecutor, fileRepository);
    try {
      zivaProcessBuilder.run();
      fail();
    } catch (IllegalArgumentException expected) {
    }
    
  }

  public void testSimpleCmdNecessary() throws Exception {
    FileRepository fileRepository = new StubFileRepository();
    JavaProcessExecutor javaProcessExecutor = new StubJavaProcessExecutor();
    StubZystem stubZystem = new StubZystem();
    ZivaProcessBuilder zivaProcessBuilder = 
      new ZivaProcessBuilder(stubZystem, javaProcessExecutor, fileRepository);
    zivaProcessBuilder.commandArray("foo");
    zivaProcessBuilder.run();
  }

}
