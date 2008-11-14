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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LazyCreateFileOutputStream extends OutputStream {

  private String name;
  private boolean append = false;
  private OutputStream outputStream;
  private OutputStream fallback;

  public LazyCreateFileOutputStream(String name) {
    this.name = name;
  }

  public LazyCreateFileOutputStream(String name, boolean append) {
    this (name);
    this.append = append;
  }

  public LazyCreateFileOutputStream(OutputStream fallback, String name, 
      boolean append) {
    this (name, append);
    this.fallback = fallback;
  }

  @Override
  public void write(int b) throws IOException {
    initializeFileOutputStream();
    outputStream.write(b);    
  }

  private synchronized void initializeFileOutputStream() 
      throws FileNotFoundException {
    if (outputStream == null) {
      try {
        outputStream = new FileOutputStream(this.name, this.append);
      } catch (FileNotFoundException e) {
        if (this.fallback == null) {
          throw e;
        }
        outputStream = this.fallback;
      }
    }
  }
  
}
