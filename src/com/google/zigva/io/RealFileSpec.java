/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.io;

import com.google.zigva.java.io.JavaFileUtil;

import java.io.File;
import java.io.IOException;

public class RealFileSpec implements FilePath {

  private final File backingFile;

  // TODO: Why can't I create one passing a String?
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
