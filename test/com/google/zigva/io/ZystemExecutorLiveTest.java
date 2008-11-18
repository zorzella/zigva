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

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.zigva.ZigvaEnvs;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.SyncZivaTask;
import com.google.zigva.exec.WaitableZivaTask;
import com.google.zigva.exec.ZigvaTask;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.lang.Waitable;
import com.google.zigva.lang.Zystem;

import java.util.Map;

@GuiceBerryEnv(ZigvaEnvs.REGULAR)
public class ZystemExecutorLiveTest extends GuiceBerryJunit3TestCase {

  @Inject
  private ZystemSelfBuilder zystem;
  
  @Inject
  private CommandExecutor.Builder commandExecutorBuilder;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void runTest() throws Throwable {
    ThreadCountAsserter asserter = new ThreadCountAsserter();
    super.runTest();
    asserter.assertThreadCount();
  }
  
  public void testEnv() throws Exception {
    
    SinkToString out = new SinkToString();
    Map<String,String> fooBarBazIsZ = Maps.newHashMap();
    String expected = "z";
    String envName = "FOOBARBAZ";
    fooBarBazIsZ.put(envName, expected);
    
    Zystem localZystem = 
      zystem
        .withEnv(fooBarBazIsZ)
        .withOut(out);

    Waitable process = commandExecutorBuilder.with(localZystem).create()
      .command("printenv", envName).execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  
  public void testIn() throws Exception {
    SinkToString out = new SinkToString();
    String expected = "ziva rules";
    
    Zystem localZystem =
      zystem
        .withIn(new CharacterSource(expected))
        .withOut(out);

    Waitable process = commandExecutorBuilder.with(localZystem).create()
      .command("cat").execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  public void testNonShell() throws Exception {
    MyCommand myCommand = new MyCommand();
    SinkToString actual = new SinkToString();
    Zystem localZystem = 
      zystem
        .withOut(actual);
    WaitableZivaTask task = commandExecutorBuilder.with(localZystem).create()
      .command(myCommand).execute();
    task.waitFor();
    assertEquals("z", actual.toString());
  }
  
  
  // $ echo foo | cat
  public void testPipe() throws Exception {
    SinkToString out = new SinkToString();
    
    Zystem localZystem = 
      zystem
        .withOut(out);

    String expected = "foo";
    Waitable process = commandExecutorBuilder.with(localZystem).create()
      .command("echo", expected)
      .pipe("cat")
      .execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  // $ echo foo | cat | cat | cat | grep foo
  public void testMultiplePipes() throws Exception {
    SinkToString out = new SinkToString();
    
    Zystem localZystem = 
      zystem
        .withOut(out);

    String expected = "foo";
    Waitable process = commandExecutorBuilder.with(localZystem).create()
      .command("echo", expected)
      .pipe("cat")
      .pipe("cat")
      .pipe("cat")
      .pipe("grep", "foo")
      .execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  // ls | cat > bar.txt
  // $ ls | (cd ../bar; cat > bar.txt)
  // executor().source("foo").sink(bar).execute();

  private static final class MyCommand implements Command {

    @Override
    public ZigvaTask execute(final Zystem zystem) {
      return new SyncZivaTask(new ZigvaTask() {
        @Override
        public void kill() {
        }

        @Override
        public String getName() {
          return "MyCommand";
        }

        @Override
        public void run() {
          zystem.ioFactory().buildOut().write('z');
        }
      });
    }
  }
}
