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

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.Channels;

public class Writers {

  public static BufferedWriter buffered(OutputStream out) {
    return new BufferedWriter(new OutputStreamWriter(out));
  }

  public static BufferedWriter buffered(Appendable out) {
    if (out instanceof BufferedWriter) {
      return (BufferedWriter)out;
    } else {
      if (out instanceof Writer) {
        return new BufferedWriter((Writer)out);
      } else {
        return new BufferedWriter(new AppendableWriter(out));
      }
    }
  }
  
  public static BufferedWriter buffered(FileDescriptor out) {
    BufferedWriter result = new BufferedWriter(
        new OutputStreamWriter(
            Channels.newOutputStream(
                new FileOutputStream(out).getChannel())));
    return result;
  }

}
