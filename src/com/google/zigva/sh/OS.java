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

import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Deprecated
public class OS {

  public static ZivaProcess run(String[] cmdArray, File dir) {
    return run(cmdArray, dir, System.out, System.err);
  }
   
  public static ZivaProcess run(List<String> cmdArray, File dir, Appendable out,
      Appendable err) {
    return run (toArray(cmdArray), dir, out, err, null);
  }

  public static ZivaProcess run(String cmd, String dir, 
	      Appendable out, Appendable err) {
	    return run(cmd.split(" "), new File(dir), out, err, null);
	  }
	    
  public static ZivaProcess run(String cmd, File dir, 
	      Appendable out, Appendable err) {
	    return run(cmd.split(" "), dir, out, err, null);
	  }
	    
  public static ZivaProcess run(String[] cmdArray, File dir, 
	      Appendable out, Appendable err) {
	    return run(cmdArray, dir, out, err, null);
	  }
	    
  public static ZivaProcess run(String[] cmdArray, File dirToRun, 
        Appendable out, Appendable err, InputStream in) {
    
    ActivePipe.Builder activePipeBuilder = Static.injector.getInstance(ActivePipe.Builder.class);

    try {
      Process p = new ProcessBuilder(cmdArray)
        .directory(dirToRun)
        .redirectErrorStream(err == null)
        .start();
      Thread outS  = activePipeBuilder.comboCreate(
          "OS - sysout", p.getInputStream(), out).start();
      Thread errS = activePipeBuilder.comboCreate(
          "OS - syserr", p.getErrorStream(), err).start();
      Thread inS = null;
      if (in == null) {
        p.getOutputStream().close();
      } else {
        inS = activePipeBuilder.comboCreate(
            "OS - sysin",
            Static.injector.getInstance(ReaderSource.Builder.class)
              .create(Readers.buffered(in)), 
            p.getOutputStream()).start();
      }
      return new ZivaProcess(p, inS, outS, errS);
    } catch (IOException e) {
      // TODO 
      throw new RuntimeException(e);
    }
  }

  private static String[] toArray(List<String> cmdArray) {
    String[] result = new String[cmdArray.size()];
    cmdArray.toArray(result);
    return result;
  }
}
