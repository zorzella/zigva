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
import com.google.zigva.sh.ZivaProcess;
import com.google.zigva.sh.ZivaProcessBuilder;


import java.util.List;

public class ZivaProcessUtil {

  public static List<String> readLines(ZivaProcessBuilder zivaProcessBuilder) {
    
    // Create SplittingAppendable
    StringBuilder out = new StringBuilder();
    StringBuilder err = new StringBuilder();

    ZivaProcess zivaProcess = 
      zivaProcessBuilder
        .setOut(out)
        .setErr(err)
        .run();
    
    zivaProcess.waitFor();
    
    if (zivaProcess.exitValue() != 0) {
      throw new RuntimeException(String.format(
          "Failed to execute '%s' under '%s'. stderr says '%s'", 
          zivaProcessBuilder.getCommandString(), 
          zivaProcessBuilder.getWorkingDir(),
          err.toString().trim()));
    }
    
    return Lists.newArrayList(out.toString().split("\n"));
  }

  public static String readContents(ZivaProcessBuilder zivaProcessBuilder) {
    
    StringBuilder out = new StringBuilder();
    StringBuilder err = new StringBuilder();

    ZivaProcess zivaProcess = 
      zivaProcessBuilder
        .setOut(out)
        .setErr(err)
        .run();
    
    zivaProcess.waitFor();
    
    if (zivaProcess.exitValue() != 0) {
      throw new RuntimeException(String.format(
          "Failed to execute '%s'. stderr says '%s'", 
          zivaProcessBuilder.getCommandString(), err.toString().trim()));
    }
    
    return out.toString();
  }
}
