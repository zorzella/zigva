package com.google.zigva.sh;

import com.google.zigva.io.NullAppendable;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class BlockingOS {

  public static ZivaProcess run(String[] cmdArray, File dir) {
    ZivaProcess result = OS.run(cmdArray, dir, System.out, System.err);
    result.waitFor();
    return result;
  }

  /**
   * A simple wrapper to run a given command, this method assumes the command is
   * space-separated, i.e., it will split the given string at each " ". Note
   * that it currently does not honour slash escapes or quotes, but that might
   * change in the future.
   */
  public static ZivaProcess run(String cmd, String dir, Appendable out,
      Appendable err) {
    ZivaProcess result = OS.run(cmd.split(" "), new File(dir), out, err, null);
    result.waitFor();
    return result; 
  }

  public static ZivaProcess run(String cmd, File dir, Appendable out,
      Appendable err) {
    ZivaProcess result = OS.run(cmd.split(" "), dir, out, err, null);
    result.waitFor();
    return result;
  }

  public static ZivaProcess run(String[] cmdArray, File dir, Appendable out,
      Appendable err) {
    ZivaProcess result = OS.run(cmdArray, dir, out, err, null);
    result.waitFor();
    return result;
  }

  public static ZivaProcess run(String[] cmdArray, String dir, Appendable out,
      Appendable err) {
    ZivaProcess result = OS.run(cmdArray, new File(dir), out, err, null);
    result.waitFor();
    return result;
  }

  public static ZivaProcess run(String[] cmdArray, File dir, Appendable out,
      Appendable err, InputStream in) {
    ZivaProcess result = OS.run(cmdArray, dir, out, err, in);
    result.waitFor();
    return result;
  }

  public static ZivaProcess run(List<String> cmdArray, File dir, Appendable out,
      Appendable err) {
    return run (toArray(cmdArray), dir, out, err, null);
  }

  public static ZivaProcess run(List<String> cmdArray, String dir, Appendable out,
      Appendable err) {
    return run (toArray(cmdArray), new File(dir), out, err, null);
  }

  private static String[] toArray(List<String> cmdArray) {
    String[] result = new String[cmdArray.size()];
    cmdArray.toArray(result);
    return result;
  }

  public static String readFullContent(List<String> cmdArray, File dirToRun) {
    StringBuilder builder = new StringBuilder();
    run(cmdArray, dirToRun, builder, NullAppendable.INSTANCE);
    return builder.toString();
  }
}
