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

package com.google.zigva.exec;

import com.google.zigva.exec.CommandExecutor.Builder;
import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.io.Source;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.Zystem;

public class Cat implements Command {

  private final class MyZivaTask implements ZigvaTask, NamedRunnable {
    
    private final KillableCollector toKill = new KillableCollector();
    private final IoFactory ioFactory;

    private MyZivaTask(Zystem zystem) {
      this.ioFactory = zystem.ioFactory();
    }
    
    @Override
    public void kill() {
      toKill.killable().kill();
    }

    @Override
    public void run() {
      Source<Character> in = ioFactory.in().build();
      toKill.add(ioFactory.out().build(in)).run();
    }

    @Override
    public String getName() {
      return "Cat";
    }
  }

  @Override
  public ZigvaTask buildTask(Builder cmdExecutorBuilder, final Zystem zystem) {
    ZigvaTask result = new SyncZivaTask(new MyZivaTask(zystem));
    return result;
  }
}
