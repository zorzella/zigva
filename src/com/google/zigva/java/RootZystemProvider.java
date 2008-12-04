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

import com.google.inject.Provider;
import com.google.zigva.guice.ZigvaThreadFactory;
import com.google.zigva.io.DataNotReadyException;
import com.google.zigva.io.DataSourceClosedException;
import com.google.zigva.io.EndOfDataException;
import com.google.zigva.io.FilePath;
import com.google.zigva.io.RealFileSpec;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.io.AppendableSink;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.java.io.Writers;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.IoFactorySelfBuilder;
import com.google.zigva.lang.Zystem;

import java.io.File;
import java.io.FileDescriptor;

public final class RootZystemProvider implements Provider<Zystem> {

	private static final ZigvaThreadFactory ROOT_THREAD_FACTORY = new ZigvaThreadFactory();

  /**
	 * A {@link Source} that is already at its end of stream.
	 * @author zorzella
	 *
	 * @param <T>
	 */
  private static final class SourceAtEOS<T> implements Source<T> {
		@Override
		public void close() {
		}

		@Override
		public boolean isEndOfStream() throws DataSourceClosedException {
			return true;
		}

		@Override
		public boolean isReady() throws DataSourceClosedException {
			return true;
		}

		@Override
		public T read() throws DataNotReadyException,
				DataSourceClosedException, EndOfDataException {
			return null;
		}
	}

  private static final Object IN_LOCK = new StringBuilder("System in lock");
  private static final Object OUT_LOCK = new StringBuilder("System out lock");
  private static final Object ERR_LOCK = new StringBuilder("System err lock");

  private static final ReaderSource IN_READER_SOURCE = 
    new ReaderSource.Builder(ROOT_THREAD_FACTORY).withCombo(100, 500, IN_LOCK)
      .create(Readers.buffered(FileDescriptor.in));

  private static final AppendableSink OUT_WRITER_SINK = 
    new AppendableSink.Builder(ROOT_THREAD_FACTORY).withCombo( 
        100, 500, OUT_LOCK).create(Writers.buffered(FileDescriptor.out));

  private static final AppendableSink ERR_WRITER_SINK = 
    new AppendableSink.Builder(ROOT_THREAD_FACTORY).withCombo( 
        100, 500, ERR_LOCK).create(Writers.buffered(FileDescriptor.out));
  
  //TODO: package private constructor?
  
  public Zystem get() {
    return new RealZystem(
        buildIoFactory(), 
        getCurrentDir(), 
        getHomeDir(),
        System.getenv()
        );
  }
  
  private static IoFactory buildIoFactory() {
    return new IoFactory() {

      @Override
      public Sink<Character> buildErr(Source<Character> source) {
        return new SpecialSinkSink<Character>(ERR_WRITER_SINK);
      }

      @Override
      public Source<Character> buildIn() {
        if (false) {
          return new SpecialSourceSource<Character>(IN_READER_SOURCE, IN_LOCK);
        }
        return new SourceAtEOS<Character>();
      }

      @Override
      public Sink<Character> buildOut(Source<Character> source) {
        return new SpecialSinkSink<Character>(OUT_WRITER_SINK);
      }

      @Override
      public boolean redirectErrToOut() {
        return false;
      }
    };
  }

  private static RealFileSpec getCurrentDir() {
    return new RealFileSpec(new File("."));
  }
  
  private static FilePath getHomeDir() {
    return new RealFileSpec(new File(System.getProperty("user.home")));
  }
}
