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

package com.google.zigva.java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.channels.Channels;

public class InputStreams {

//  public static InputStream from(FileInputStream in) {
//    InputStream result = Channels.newInputStream(
//        in.getChannel());
//    return result;
//  }

  public static InputStream from(FileDescriptor in) {
    FileInputStream result = new FileInputStream(in);
    if (true) {
      return result;
    }
    // TODO: why did I ever do this?
    return Channels.newInputStream(result.getChannel());
  }

  public static InputStream from(File in) {
    FileInputStream result;
    try {
      result = new FileInputStream(in);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
    if (true) {
      return result;
    } 
    
    // TODO: also below: why did I ever do this?
    return Channels.newInputStream(result.getChannel());
  }

  

}
