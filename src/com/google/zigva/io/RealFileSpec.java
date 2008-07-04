package com.google.zigva.io;

import java.io.File;
import java.io.IOException;

public class RealFileSpec implements FilePath {

  private final File backingFile;

  public RealFileSpec(File backingFile) {
    this.backingFile = backingFile;
  }

  @Override
  public String getCanonicalPath() {
    try {
      return backingFile.getCanonicalPath();
    } catch (IOException e) {
      //TODO(zorzella): specific exceptions?
      throw new RuntimeException(e);
    }
  }

  @Override
  public File toFile() {
    return backingFile;
  }
  
  @Override
  public String toString() {
    return JavaFileUtil.getCanonicalPath(backingFile);
  }
}
