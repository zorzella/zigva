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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.testing.guiceberry.GuiceBerryEnv;
import com.google.inject.testing.guiceberry.junit3.GuiceBerryJunit3TestCase;
import com.google.zigva.ZigvaEnvs;
import com.google.zigva.exec.Cat;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.Echo;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.guice.ZystemSelfBuilder;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.lang.CommandResponse;
import com.google.zigva.lang.ConvenienceWaitable;
import com.google.zigva.lang.SinkFactory;
import com.google.zigva.lang.Zystem;
import com.google.zigva.sh.OS;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

@GuiceBerryEnv(ZigvaEnvs.REGULAR)
public class CommandExecutionLiveTest extends GuiceBerryJunit3TestCase {

  @Inject
  private ZystemSelfBuilder zystem;
  
  @Inject
  private CommandExecutor commandExecutor;

  @Inject
  private OS os;
  
  @Inject
  private ReaderSource.Builder readerSourceBuilder;
  
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
  
  public void testFoo() throws Exception {
    doTestExistingCommandErr();
  }
  
  public void testEnv() throws Exception {
    
    PassiveSinkToString out = new PassiveSinkToString();
    Map<String,String> fooBarBazIsZ = Maps.newHashMap();
    String expected = "z";
    String envName = "FOOBARBAZ";
    fooBarBazIsZ.put(envName, expected);
    
    Command printenv = os.command("printenv", envName);

    Zystem localZystem = 
      zystem
        .withEnv(fooBarBazIsZ)
        .withOut(out.asSinkFactory());
    
    ConvenienceWaitable process = 
      commandExecutor
        .with(localZystem)
        .command(printenv)
        .execute();
    process.waitFor();
    assertEquals(expected, out.asString().trim());
  }

  
  public void testIn() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();
    String expected = "ziva rules";
    
    Command cat = os.command("cat");

    Zystem localZystem =
      zystem
        .withIn(new CharacterSource(expected))
        .withOut(out.asSinkFactory());
    
    ConvenienceWaitable process = 
      commandExecutor
        .with(localZystem)
        .command(cat)
        .execute();
    process.waitFor();
    assertEquals(expected, out.asString().trim());
  }

  public void testNonShell() throws Exception {
    MyCommand myCommand = new MyCommand();
    PassiveSinkToString actual = new PassiveSinkToString();
    Zystem localZystem = 
      zystem
        .withOut(actual.asSinkFactory());
    ConvenienceWaitable task = 
      commandExecutor
        .with(localZystem)
        .command(myCommand)
        .execute();
    task.waitFor();
    assertEquals("z", actual.asString());
  }
  
  
  // $ echo foo | cat
  public void testPipe() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();
    
    String expected = "foo";
    Command echoFoo = os.command("echo", expected);
    Command cat = os.command("cat");

    Zystem localZystem = 
      zystem
        .withOut(out.asSinkFactory());
    
    ConvenienceWaitable process = 
      commandExecutor
        .with(localZystem)
        .command(echoFoo)
        .pipe(cat)
        .execute();
    process.waitFor();
    assertEquals(expected, out.toString().trim());
  }

  // $ echo foo | cat | cat | cat | grep foo
  public void testMultiplePipes() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();

    String expected = "foo";
    Command cat = os.command("cat");
    Command grepFoo = os.command("grep", expected);
    Command echoFoo = os.command("echo", expected);
    
    Zystem localZystem = 
      zystem
        .withOut(out.asSinkFactory());

    ConvenienceWaitable process = 
      commandExecutor
        .with(localZystem)
        .command(echoFoo)
        .pipe(cat)
        .pipe(cat)
        .pipe(cat)
        .pipe(grepFoo)
        .execute();
    process.waitFor();
    assertEquals(expected, out.asString().trim());
  }

  // $ echo foo | cat | Cat | cat | grep foo
  public void testMixOsAndJava() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();
    
    String expected = "foo";
    Command cat = os.command("cat");
    Command grepFoo = os.command("grep", expected);
    Command echoFoo = os.command("echo", expected);

    Zystem localZystem = 
      zystem
        .withOut(out.asSinkFactory());

    ConvenienceWaitable process = 
      commandExecutor
        .with(localZystem)
        .command(echoFoo)
        .pipe(cat)
        .pipe(new Cat())
        .pipe(cat)
        .pipe(grepFoo)
        .execute();
    process.waitFor();
    assertEquals(expected, out.asString().trim());
  }

  // echo foo > /tmp/bar.txt
  public void testLsPipeCatWriteToFile() throws Exception {
    // TODO: make a FilePassiveSink
    File barFile = File.createTempFile("bar", "txt");
    AppendablePassiveSink out = 
      new AppendablePassiveSink(new FileWriter(barFile));
    Command ls = new Echo("foo");
    Zystem localZystem =
      zystem.withOut(out);
    
    commandExecutor
      .with(localZystem)
      .command(ls)
      .execute()
      .waitFor();
    
    // TODO: make a FileSource
    ReaderSource in = readerSourceBuilder.create(new FileReader(barFile));
    
    PassiveSinkToString contents = new PassiveSinkToString();
    new SimpleSink<Character>(in, contents).run();
    assertEquals("foo", contents.asString());
  }
  
  // $ ls | (cd ../bar; cat > bar.txt)
  // executor().source("foo").sink(bar).execute();
  
  public void testWithParams() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();
    String expected = "foo";
    Command echoDashNFoo = os.command("echo", "-n", expected);

    Zystem localZystem = zystem
      .withOut(out.asSinkFactory());
    commandExecutor.with(localZystem)
      .command(echoDashNFoo)
      .execute().waitFor();
    assertEquals(expected, out.asString());
  }

  public void doTestExistingCommandErr() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();
    
    Command lsIDontExist = os.command("ls", "/idontexist");

    Zystem localZystem = zystem
      .withOut(out.asSinkFactory());
    try {
      commandExecutor.with(localZystem)
        .command(lsIDontExist)
        .execute().waitFor();
      fail();
    } catch (RuntimeException expected) {
      //TODO brittle UNIXism
      assertTrue(expected.getMessage(),
          expected.getMessage().contains(
              "ls: cannot access /idontexist: No such file or directory"));
    }
  }
  
  public void testNonExistingCommandErr() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();
    Command iDontExist = os.command("/idontexist");

    Zystem localZystem = zystem
      .withOut(out.asSinkFactory());
    try {
      commandExecutor.with(localZystem)
        .command(iDontExist)
        .execute().waitFor();
      fail();
    } catch (RuntimeException expected) {
      // TODO: can we assert something about the exception?
    }
  }

  public void testComplexCommand() throws Exception {
    PassiveSinkToString out = new PassiveSinkToString();
    Zystem localZystem = zystem
      .withOut(out.asSinkFactory());
    commandExecutor = commandExecutor
      .with(localZystem);
    
    commandExecutor
      .command(new MyComplexCommand(os, commandExecutor))
      .execute()
      .waitFor();
    assertEquals("foo\n", out.asString());
  }
  
  /**
   * This class internally calls {@code echo -n foo | cat | grep foo}.
   * 
   * <p>Note that "grep" adds a newline at the end, even if "echo -n" does not print one
   */
  private static final class MyComplexCommand implements Command {

    private final OS os;
    private final CommandExecutor cmdExecutor;

    public MyComplexCommand(
        OS os, 
        CommandExecutor cmdExecutorBuilder) {
      this.os = os;
      this.cmdExecutor = cmdExecutorBuilder;
    }

    @Override
    public CommandResponse go(Zystem zystem, Source<Character> in) {

      final List<Source<Character>> temp = Lists.newArrayList();
      
      SinkFactory<Character> foo = new SinkFactory<Character> () {
      
        @Override
        public Sink build(Source<Character> source) {
          temp.add(source);
          return new Sink() {
          
            @Override
            public void kill() {
            }
          
            @Override
            public void run() {
            }
          };
        }
      };
      
      ZystemSelfBuilder localZystem = 
        new ZystemSelfBuilder(zystem)
          .withOut(foo);
      
      Command echoFoo = os.command("echo", "-n", "foo");
      Command cat = os.command("cat");
      Command grepFoo = os.command("grep", "foo");
      cmdExecutor
        .with(localZystem)
        .command(echoFoo)
        .pipe(cat)
        .pipe(grepFoo)
        .execute()
        .waitFor();
      
      return CommandResponse.forOut(this, temp.get(0));
    }
  }

  private static final class MyCommand implements Command {
    @Override
    public CommandResponse go(Zystem zystem, Source<Character> in) {
      in.close();
      return CommandResponse.forOut(this, new CharacterSource("z"));
    }
  }
}
