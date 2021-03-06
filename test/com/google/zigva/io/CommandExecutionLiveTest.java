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
import com.google.zigva.command.Cat;
import com.google.zigva.command.Echo;
import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.exec.CommandExecutor.PreparedCommand;
import com.google.zigva.java.ProcessFailedToStartException;
import com.google.zigva.java.io.SourceOfCharFromFile;
import com.google.zigva.lang.CommandResponse;
import com.google.zigva.lang.ConvenienceWaitable;
import com.google.zigva.lang.Waitable.CommandFailedException;
import com.google.zigva.lang.impl.ZystemSelfBuilder;
import com.google.zigva.sh.OS;
import com.google.zigva.sys.Zystem;

import java.io.File;
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
  private SourceOfCharFromFile fileSourceBuilder;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void runTest() throws Throwable {
    ThreadCountAsserter asserter = new ThreadCountAsserter();
    super.runTest();
    // suppress asserter.assertThreadCount();
  }
  
  public void suppress_testFoo() throws Exception {
    doTestExistingCommandErr();
  }
  
  // $ FOOBARBAZ=z printenv FOOBARBAZ
  public void testEnv() throws Exception {
    
    PumpToString out = new PumpToString();
    Map<String,String> fooBarBazIsZ = Maps.newHashMap();
    String expected = "z";
    String envName = "FOOBARBAZ";
    fooBarBazIsZ.put(envName, expected);
    
    Command printenv = os.command("printenv", envName);

    Zystem localZystem = 
      zystem
        .withEnv(fooBarBazIsZ)
        .withOut(out);
    
    ConvenienceWaitable process = 
      commandExecutor
        .with(localZystem)
        .command(printenv)
        .execute();
    process.waitFor();
    assertEquals(expected, out.asString().trim());
  }

  // This is like executing "cat" and typing "zigva rules"
  public void testIn() throws Exception {
    PumpToString out = new PumpToString();
    String expected = "zigva rules";
    
    Command cat = os.command("cat");

    Zystem localZystem =
      zystem
        .withIn(new CharSequenceSource(expected))
        .withOut(out);
    
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
    PumpToString actual = new PumpToString();
    Zystem localZystem = 
      zystem
        .withOut(actual);
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
    PumpToString out = new PumpToString();
    
    String expected = "foo";
    Command echoFoo = os.command("echo", expected);
    Command cat = os.command("cat");

    Zystem localZystem = 
      zystem
        .withOut(out);
    
    ConvenienceWaitable process = 
      commandExecutor
        .with(localZystem)
        .command(echoFoo)
        .pipe(cat)
        .execute();
    process.waitFor();
    assertEquals(expected, out.asString().trim());
  }

  // $ echo foo | cat | cat | cat | grep foo
  public void testMultiplePipes() throws Exception {
    PumpToString out = new PumpToString();

    String expected = "foo";
    Command cat = os.command("cat");
    Command grepFoo = os.command("grep", expected);
    Command echoFoo = os.command("echo", expected);
    
    Zystem localZystem = 
      zystem
        .withOut(out);

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
    PumpToString out = new PumpToString();
    
    String expected = "foo";
    Command cat = os.command("cat");
    Command grepFoo = os.command("grep", expected);
    Command echoFoo = os.command("echo", expected);

    Zystem localZystem = 
      zystem
        .withOut(out);

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

  // $ echo foo > /tmp/bar.txt
  public void testLsPipeCatWriteToFile() throws Exception {
    File barFile = File.createTempFile("bar", "txt");
    PumpFactory<Character> out = CharacterPumpFactories.forFile(barFile);
    Command ls = new Echo("foo");
    Zystem localZystem =
      zystem.withOut(out);
    
    commandExecutor
      .with(localZystem)
      .command(ls)
      .execute()
      .waitFor();
    
    Source<Character> in = fileSourceBuilder.create(barFile);
    
    PumpToString contents = new PumpToString();
    contents.getPumpFor(in).run();
    assertEquals("foo", contents.asString());
  }
  
  // TODO
  // $ ls | (cd ../bar; cat > bar.txt)
  // executor().source("foo").sink(bar).execute();
  

  // $ echo -n foo
  public void testWithParams() throws Exception {
    PumpToString out = new PumpToString();
    String expected = "foo";
    Command echoDashNFoo = os.command("echo", "-n", expected);

    Zystem localZystem = zystem
      .withOut(out);
    commandExecutor.with(localZystem)
      .command(echoDashNFoo)
      .execute().waitFor();
    assertEquals(expected, out.asString());
  }

  // $ ls /idontexist
  public void doTestExistingCommandErr() throws Exception {
    PumpFactory<Character> err = new PumpToVoid<Character>();
    
    Command lsIDontExist = os.command("ls", "/idontexist");

    Zystem localZystem = zystem
      .withErr(err);
    try {
      commandExecutor
        .with(localZystem)
        .command(lsIDontExist)
        .execute()
        .waitFor();
      fail();
    } catch (CommandFailedException expected) {
      //TODO brittle UNIXism
      assertTrue(expected.getMessage(),
          expected.getMessage().contains(
              "ls: cannot access /idontexist: No such file or directory"));
    }
  }
  
  // $ /idontexist
  public void testNonExistingCommandErr() throws Exception {
    Command iDontExist = os.command("/idontexist");
    try {
      commandExecutor
        .command(iDontExist)
        .execute()
        .waitFor();
      fail();
    } catch (ProcessFailedToStartException expected) {
      //TODO: assert regexp message
      assertTrue(expected.getMessage().startsWith("Failed to start process"));
    }
  }
  
  /**
   * The {@link PreparedCommand#switchPipe(Command)} causes the output and error
   * streams to be switched when fed to the next command in the pipeline.
   */
  public void testSwitchPipe() throws Exception {
    Command outAndErr = new Command() {
    
      @Override
      public CommandResponse go(Zystem zystem, Source<Character> in) {
        return CommandResponse.forOutErr(
            this, 
            new CharSequenceSource("out"), 
            new CharSequenceSource("err"));
      }
    };
    
    PumpToString otherOut = new PumpToString();
    PumpToString otherErr = new PumpToString();
    Zystem localZystem = zystem
      .withOut(otherOut)
      .withErr(otherErr);
    
    Command cat = new Cat();
    
    commandExecutor
      .with(localZystem)
      .command(outAndErr)
      .switchPipe(cat)
      .execute()
      .waitFor();
    
    assertEquals("out", otherErr.asString());
    assertEquals("err", otherOut.asString());
  }

  /**
   * The {@link PreparedCommand#mergePipe(Command)} causes the output and error
   * streams to be merged when fed to the next command in the pipeline.
   */
  public void SUPPRESS_testMergePipe0() throws Exception {
    Command outAndErr = new Command() {
    
      @Override
      public CommandResponse go(Zystem zystem, Source<Character> in) {
        final CharSequenceSource outSource = new CharSequenceSource("out");
        return CommandResponse.forOutErr(
            this, 
            outSource, 
            new CharSequenceSource("err") {
              @Override
              public boolean isEndOfStream() throws DataSourceClosedException {
                // We'll be sure to block here until "out" is all read so
                // the merged pipe will be predictable.
                
                //TODO: use monitors
                while (!outSource.isClosed()) {
                  try {
                    Thread.sleep(1);
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                }
                return super.isEndOfStream();
              }
            });
      }
    };
    
    PumpToString otherOut = new PumpToString();
    PumpToString otherErr = new PumpToString();
    Zystem localZystem = zystem
      .withOut(otherOut)
      .withErr(otherErr);
    
    Command cat = new Cat();
    
    commandExecutor
      .with(localZystem)
      .command(outAndErr)
      .mergePipe(cat)
      .execute()
      .waitFor();
    
    assertEquals("out", otherErr.asString());
    assertEquals("err", otherOut.asString());
  }

  public void testComplexCommand() throws Exception {
    PumpToString out = new PumpToString();
    Zystem localZystem = zystem
      .withOut(out);
    commandExecutor
      .with(localZystem)
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
      
      PumpFactory<Character> foo = new PumpFactory<Character> () {
      
        @Override
        public Pump getPumpFor(Source<Character> source) {
          temp.add(source);
          return new Pump() {
          
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
      return CommandResponse.forOut(this, new CharSequenceSource("z"));
    }
  }
}
