package com.google.zigva.sh;

import com.google.zigva.io.FilePath;
import com.google.zigva.io.FileRepository;
import com.google.zigva.io.RealFileSpec;

import java.io.File;


public class StubFileRepository implements FileRepository {

  FilePath file = new RealFileSpec(new File("."));
  
  @Override
  public boolean exists(FilePath file) {
    return false;
  }

  @Override
  public FilePath get(String... fileNameParts) {
    return file;
  }

  @Override
  public FilePath get(File baseFile, String... fileNameParts) {
    return file;
  }

  @Override
  public FilePath get(FilePath baseFile, String... fileNameParts) {
    return file;
  }

}
