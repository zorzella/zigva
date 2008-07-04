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
