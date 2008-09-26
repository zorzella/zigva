package com.google.zigva.java.io;

import java.io.File;
import java.io.IOException;

public class JavaFileUtil {

  public static String getCanonicalPath(File file) {
    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
}
