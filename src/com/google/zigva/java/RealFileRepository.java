package com.google.zigva.java;

import com.google.common.base.Join;
import com.google.inject.Inject;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.FileRepository;
import com.google.zigva.io.RealFileSpec;


import java.io.File;
import java.io.IOException;

public class RealFileRepository implements FileRepository {

  @Inject
  public RealFileRepository() {
  }
  
  @Override
  public boolean exists(FilePath file) {
    return file.toFile().exists();
  }

  private File canonicalized(File file) {
    try {
      return file.getCanonicalFile();
    } catch (IOException e) {
      //TODO(zorzella): specific exceptions?
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public FilePath get(String... fileNameParts) {
    return new RealFileSpec(canonicalized(
        new File(Join.join(File.separator, fileNameParts))));
  }

  @Override
  public FilePath get(File baseFile, String... fileNameParts) {
    return new RealFileSpec(canonicalized(
        new File(baseFile, Join.join(File.separator, fileNameParts))));
  }

  @Override
  public FilePath get(FilePath baseFile, String... fileNameParts) {
    return new RealFileSpec(canonicalized(
        new File(baseFile.toFile(), Join.join(File.separator, fileNameParts))));
  }


}
