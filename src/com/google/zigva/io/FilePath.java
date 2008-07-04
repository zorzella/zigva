package com.google.zigva.io;

import java.io.File;

public interface FilePath {

  File toFile();

  String getCanonicalPath();

}
