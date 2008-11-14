package com.google.zigva.sh;

import com.google.lang.StubZystem;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.FileRepository;
import com.google.zigva.io.RealFileSpec;
import com.google.zigva.java.io.ReaderSource;

import junit.framework.TestCase;

import java.io.File;

public class ZivaProcessBuilderTest extends TestCase {

  private ActivePipe.Builder activePipeBuilder = 
    Static.injector.getInstance(ActivePipe.Builder.class);
  private ReaderSource.Builder readerSourceBuilder = 
    Static.injector.getInstance(ReaderSource.Builder.class);

  public void testCmdIsNecessary() throws Exception {
    FileRepository fileRepository = new StubFileRepository();
    JavaProcessExecutor javaProcessExecutor = new StubJavaProcessExecutor();
    StubZystem stubZystem = new StubZystem();
    ZivaProcessBuilder zivaProcessBuilder = 
      new ZivaProcessBuilder(stubZystem, javaProcessExecutor, fileRepository, activePipeBuilder, readerSourceBuilder);
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
      new ZivaProcessBuilder(stubZystem, javaProcessExecutor, fileRepository, activePipeBuilder, readerSourceBuilder);
    zivaProcessBuilder.commandArray("foo");
    zivaProcessBuilder.run();
  }

}
