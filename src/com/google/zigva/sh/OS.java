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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.io.OutputStreamPassiveSink;
import com.google.zigva.java.JavaProcessStarter;
import com.google.zigva.lang.Waitables;
import com.google.zigva.lang.ZigvaThreadFactory;

public class OS {
  
  private final ZigvaThreadFactory zigvaThreadFactory;
  private final OutputStreamPassiveSink.Builder outputStreamPassiveSinkBuilder;
  private final JavaProcessStarter javaProcessStarter;
  private final Waitables waitables;

  @Inject
  public OS ( 
      ZigvaThreadFactory zigvaThreadFactory, 
      OutputStreamPassiveSink.Builder outputStreamPassiveSinkBuilder, 
      JavaProcessStarter javaProcessStarter, 
      Waitables waitables) {
    this.zigvaThreadFactory = zigvaThreadFactory;
    this.outputStreamPassiveSinkBuilder = outputStreamPassiveSinkBuilder;
    this.javaProcessStarter = javaProcessStarter;
    this.waitables = waitables;
  }
  
  public Command command(Iterable<String> command) {
    return new SystemCommand(
        zigvaThreadFactory, 
        outputStreamPassiveSinkBuilder, 
        javaProcessStarter, 
        Lists.newArrayList(command), 
        waitables);
  }

  public Command command(String... command) {
    return new SystemCommand(
        zigvaThreadFactory, 
        outputStreamPassiveSinkBuilder, 
        javaProcessStarter, 
        Lists.newArrayList(command), 
        waitables);
  }
  
  /**
   * The basic difference between this method and {@link #command(String...)} is
   * that this command will leverage some shell interpreter to execute 
   * {@code command}, whereas {@link #command(String...)} executes a command
   * through the OS. The specific shell that will be used by this command
   * depends on what {@link #TODO} is bound to.
   * 
   * <p>E.g. in Linux/bash, you may call 
   * {@code buildShell("ls *.java 2>&1 | wc -1 && echo foo")}; note that the 
   * glob expand (*.java), redirect (2>&1), pipe (|) and conditional (&&) 
   * are all handled by bash, and, therefore, not available with 
   * {@link #command(String...)}.
   * 
   * <p>Also note how the command in here is a single String which is simply
   * given as a param for, say, "bash -c" to interpret it, whereas 
   * {@code #build(String...)} takes in a String[]. So, all escaping needs to
   * be heeded in this method.
   *  
   * <p>E.g. to list a file named "My File" (note the space), we need either
   * execute: {@code build("ls", "My File")} or 
   * {@code buildShell("ls My\\ File")} (double backslashes since 
   * java needs backslash to be escaped).
   */
  public Command shellCommand(String command) {
    return new SystemCommand(
        zigvaThreadFactory, 
        outputStreamPassiveSinkBuilder, 
        javaProcessStarter, 
        // TODO @Inject this!
        Lists.newArrayList("/bin/bash", "-c", command), 
        waitables);
  }
}