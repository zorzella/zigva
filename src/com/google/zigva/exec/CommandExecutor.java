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

import com.google.zigva.io.Source;
import com.google.zigva.lang.CommandResponse;
import com.google.zigva.lang.ConvenienceWaitable;
import com.google.zigva.sys.Zystem;

public interface CommandExecutor {

  public interface Command {
    CommandResponse go(Zystem zystem, Source<Character> in);
  }
  
  public interface PreparedCommand {

    ConvenienceWaitable execute();

    PreparedCommand pipe(Command command);

    PreparedCommand switchPipe(Command command);

    PreparedCommand mergePipe(Command command);
  }

  PreparedCommand command(Command command);
  
  CommandExecutor with(Zystem zystem);
}
