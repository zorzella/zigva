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

import java.io.File;

public class Sh {
  
  public static ZivaProcess run(String command) {
    return Sh.run (command, (File)null);
  }
  
  public static ZivaProcess run(String command, String dir,
      Appendable out, Appendable err) {
    return Sh.run (command, new File(dir), out, err);
  }
  
  public static ZivaProcess run(String command, String dir) {
    return Sh.run (command, new File(dir));
  }
  
  public static ZivaProcess run(String command, File dir) {
    return run(command, dir, System.out, System.err);
  }

  public static ZivaProcess run(String command, File dir, 
      Appendable out, Appendable err) {
    String[] cmdArray = {"sh", "-c", command};
    return OS.run (cmdArray, dir, out, err);
  }
  
}
