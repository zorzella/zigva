package com.google.zigva.sh;

import com.google.lang.StubZystem;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.FileRepository;
import com.google.zigva.io.RealFileSpec;

import junit.framework.TestCase;

import java.io.File;

public class ZivaProcessBuilderTest extends TestCase {

  private ActivePipe.Builder activePipeBuilder = 
    Static.injector.getInstance(ActivePipe.Builder.class);

  public void testCmdIsNecessary() throws Exception {
    FileRepository fileRepository = new StubFileRepository();
    JavaProcessExecutor javaProcessExecutor = new StubJavaProcessExecutor();
    StubZystem stubZystem = new StubZystem();
    ZivaProcessBuilder zivaProcessBuilder = 
      new ZivaProcessBuilder(stubZystem, javaProcessExecutor, fileRepository, activePipeBuilder);
    try {
      zivaProcessBuilder.run();
      fail();
    } catch (IllegalArgumentException expected) {
    }
    
  }

  public void testSimpleCmdNecessary() throws Exception {
    FileRepository fileRepository = new StubFileRepository();
    JavaProcessExecutor javaProcessExecutor = new StubJavaProcessExecutor();
    StubZystem stubZystem = new StubZystem() {
      @Override
      public FilePath getCurrentDir() {
        return new RealFileSpec(new File("."));
      }
    };
    ZivaProcessBuilder zivaProcessBuilder = 
      new ZivaProcessBuilder(stubZystem, javaProcessExecutor, fileRepository, activePipeBuilder);
    zivaProcessBuilder.commandArray("foo");
    zivaProcessBuilder.run();
  }

}
