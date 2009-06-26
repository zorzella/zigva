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

  @Override
  public boolean mkdir(FilePath dir) {
    return true;
  }
}
