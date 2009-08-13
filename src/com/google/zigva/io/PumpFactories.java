/*
 * Copyright (C) 2009 Google Inc.
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

import com.google.inject.Inject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class PumpFactories {

  private final SinkToOutputStream.Builder sinkToOutputStreamBuilder;

  @Inject
  public PumpFactories(SinkToOutputStream.Builder sinkToOutputStreamBuilder) {
    this.sinkToOutputStreamBuilder = sinkToOutputStreamBuilder;
  }

  public PumpFactory<Character> from(final Appendable out) {
    return new PumpFactory<Character>() {
    
      @Override
      public Pump getPumpFor(Source<Character> source) {
        return new PumpToSink<Character> (source, new SinkToAppendable(out));
      }
    };
  }

  public PumpFactory<Character> from(final OutputStream out) {
    final SinkToOutputStream sink = sinkToOutputStreamBuilder.create(out);
    return new PumpFactory<Character>() {
    
      @Override
      public Pump getPumpFor(Source<Character> source) {
        return new PumpToSink<Character> (source, 
            sink);
      }
    };
  }
  
  public PumpFactory<Character> from(final File file) {
    return new PumpFactory<Character>() {
    
      @Override
      public Pump getPumpFor(Source<Character> source) {
        try {
          return new PumpToSink<Character> (source, 
              sinkToOutputStreamBuilder.create(new FileOutputStream(file, true)));
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
}
