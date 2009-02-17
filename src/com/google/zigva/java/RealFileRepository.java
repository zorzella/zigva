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
