package com.google.zigva.sh;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
    try {
      Process p = new ProcessBuilder(cmdArray)
        .directory(dirToRun)
        .redirectErrorStream(err == null)
        .start();
      Thread outS = new Thread(new ActivePipe(p.getInputStream(), out));
      outS.start();
      Thread errS = new Thread(new ActivePipe(p.getErrorStream(), err));
      errS.start();
      if (in == null) {
        p.getOutputStream().close();
      } else {
        Thread inS = new Thread(new ActivePipe(in, p.getOutputStream()));
        inS.start();
      }
      return new ZivaProcess(p, outS, errS);
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
