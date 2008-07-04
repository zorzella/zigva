package com.google.zigva.sh;

import com.google.zigva.io.NullAppendable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlockingSh {

  public static String readFullContent(String command, String dirToRun) {
    StringBuilder builder = new StringBuilder();
    run(command, dirToRun, builder, NullAppendable.INSTANCE);
    return builder.toString();
  }

  public static String readLine(String command, File dir) {
    StringBuilder temp = new StringBuilder();
    run(command, dir, temp, NullAppendable.INSTANCE);

    int index = temp.indexOf("\n");
    if (index < 0) {
      index = temp.length();
    }
    return temp.substring(0, index).toString();
  }

  public static List<String> readLines(String command, File dir) {
    StringBuilder temp = new StringBuilder();
    run(command, dir, temp, NullAppendable.INSTANCE);

    List<String> result;

    if (temp.length() == 0) {
      result = Collections.emptyList();
    } else {
      result = Arrays.asList(temp.toString().split("\n"));
    }
    return result;
  }

  public static ZivaProcess run(String command) {
    return BlockingSh.run(command, (File) null);
  }

  public static ZivaProcess run(String command, String dir, Appendable out,
      Appendable err) {
    return BlockingSh.run(command, new File(dir), out, err);
  }

  public static ZivaProcess run(String command, String dir) {
    return BlockingSh.run(command, new File(dir));
  }

  public static ZivaProcess run(String command, File dir) {
    return run(command, dir, System.out, System.err);
  }

  public static ZivaProcess run(String command, File dir, Appendable out,
      Appendable err) {
    String[] cmdArray = {"sh", "-c", command};
    ZivaProcess p = OS.run(cmdArray, dir, out, err);
    p.waitFor();
    return p;
  }

  public static String readFullContent(String command, File dirToRun) {
    StringBuilder builder = new StringBuilder();
    run(command, dirToRun, builder, NullAppendable.INSTANCE);
    return builder.toString();
  }
}
