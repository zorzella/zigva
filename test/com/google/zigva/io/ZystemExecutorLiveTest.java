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
import com.google.zigva.exec.Cat;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.SyncZivaTask;
import com.google.zigva.exec.WaitableZivaTask;
import com.google.zigva.exec.ZigvaTask;
import com.google.zigva.exec.CommandExecutor.Builder;
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
    
    PassiveSinkToString out = new PassiveSinkToString();
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
    PassiveSinkToString out = new PassiveSinkToString();
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
    PassiveSinkToString actual = new PassiveSinkToString();
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
    PassiveSinkToString out = new PassiveSinkToString();
    
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
    PassiveSinkToString out = new PassiveSinkToString();
    
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

  // $ echo foo | cat | Cat | cat | grep foo
  public void testMixOsAndJava() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();
    
    Zystem localZystem = 
      zystem
        .withOut(out);

    String expected = "foo";
    Waitable process = commandExecutorBuilder.with(localZystem).create()
      .command("echo", expected)
      .pipe("cat")
      .pipe(new Cat())
      .pipe("cat")
      .pipe("grep", "foo")
      .execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  // ls | cat > bar.txt
  // $ ls | (cd ../bar; cat > bar.txt)
  // executor().source("foo").sink(bar).execute();
  
  public void testWithParams() throws Exception {
    PassiveSink<Character> out = new PassiveSinkToString();
    Zystem localZystem = zystem
      .withOut(out);
    commandExecutorBuilder.with(localZystem).create().command("echo", "-n", "foo")
      .execute().waitFor();
    assertEquals("foo", out.toString());
  }

  public void testExistingCommandErr() throws Exception {
    PassiveSink<Character> out = new PassiveSinkToString();
    Zystem localZystem = zystem
      .withOut(out);
    try {
      commandExecutorBuilder.with(localZystem).create().command("ls", "/idontexist")
        .execute().waitFor();
      fail();
    } catch (RuntimeException expected) {
      //TODO brittle UNIXism
      assertTrue(expected.getMessage(),
          expected.getMessage().contains("ls: cannot access /idontexist: No such file or directory"));
    }
  }
  
  public void testNonExistingCommandErr() throws Exception {
    PassiveSink<Character> out = new PassiveSinkToString();
    Zystem localZystem = zystem
      .withOut(out);
    try {
      commandExecutorBuilder.with(localZystem).create().command("/idontexist")
        .execute().waitFor();
      fail();
    } catch (RuntimeException expected) {
      // TODO: can we assert something about the exception?
    }
  }

  public void testComplexCommand() throws Exception {
    PassiveSink<Character> out = new PassiveSinkToString();
    Zystem localZystem = zystem
      .withOut(out);
    commandExecutorBuilder
      .with(localZystem)
      .create()
      .command(new MyComplexCommand())
      .execute()
      .waitFor();
    // TODO jthomas why "\n"?
    assertEquals("foo\n", out.toString());
  }
  
  /**
   * This class internally calls echo -n foo | cat | grep foo
   */
  private static final class MyComplexCommand implements Command {

    @Override
    public ZigvaTask buildTask(final Builder cmdExecutorBuilder, final Zystem zystem) {
      return new StubZigvaTask() {
        @Override
        public void run() throws RuntimeException {
          cmdExecutorBuilder.create()
            .command("echo", "foo")
            .pipe("cat")
            .pipe("grep", "foo")
            .execute()
            .waitFor();
        }
      };
    }
  }

  private static final class MyCommand implements Command {

    @Override
    public ZigvaTask buildTask(Builder cmdExecutorBuilder, final Zystem zystem) {
      return new StubZigvaTask() {
        @Override
        public void run() {
          zystem.ioFactory().out().build(new CharacterSource("z")).run();
        }
      };
    }
  }
}
