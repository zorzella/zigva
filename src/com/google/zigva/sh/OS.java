package com.google.zigva.sh;

import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OS {

//  private static final ActivePipe.Builder activePipeBuilder = 
//    new ActivePipe.Builder(new ZigvaThreadFactory());
  
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
              .withIn(Readers.buffered(in)).create(), 
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
