// Copyright 2008 Google Inc. All Rights Reserved.

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
import com.google.zigva.io.WriterSink;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.java.io.Writers;
import com.google.zigva.lang.IoFactory;
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

  private static final WriterSink OUT_WRITER_SINK = 
    new WriterSink(Writers.buffered(FileDescriptor.out), 100, 500, OUT_LOCK);

  private static final WriterSink ERR_WRITER_SINK = 
    new WriterSink(Writers.buffered(FileDescriptor.out), 100, 500, ERR_LOCK);
  
  //TODO: package private constructor?
  
  public Zystem get() {
    return new RealZystem(
        buildIoFactory(), 
        getCurrentDir(), 
        getHomeDir(),
        System.getenv(), 
        ROOT_THREAD_FACTORY
        );
  }
  
  private static IoFactory buildIoFactory() {
    return new IoFactory() {

      @Override
      public Sink<Character> buildErr() {
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
      public Sink<Character> buildOut() {
        return new SpecialSinkSink<Character>(OUT_WRITER_SINK);
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
