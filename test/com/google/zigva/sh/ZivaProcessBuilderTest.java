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
