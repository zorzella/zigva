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
