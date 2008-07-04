package com.google.zigva.io;

import java.io.File;

public interface FileRepository {
  
  boolean exists(FilePath file);

  FilePath get(String... fileNameParts);

  FilePath get(File baseFile, String... fileNameParts);

  FilePath get(FilePath baseFile, String... fileNameParts);

  
}
