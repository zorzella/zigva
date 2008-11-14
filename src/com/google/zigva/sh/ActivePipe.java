package com.google.zigva.sh;

import com.google.inject.Inject;
import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;
import com.google.zigva.io.WriterSink;
import com.google.zigva.java.io.ReaderSource;
import com.google.zigva.java.io.Readers;
import com.google.zigva.java.io.Writers;
import com.google.zigva.lang.NamedRunnable;
import com.google.zigva.lang.ZThread;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ThreadFactory;

/**
 * Bad name for now -- contrary to a regular pipe, which _is_ both a Reader and
 * a Writer (and not a thread), this is an active thread that reads from a 
 * given "in" and dumps into a given "out".
 */
public class ActivePipe implements NamedRunnable {

  private final String name;
  private final Sink<Character> out;
  private final Source<Character> in;

  public static final class Builder {
    
    private final ReaderSource.Builder readerSourceBuilder;
    private final ThreadFactory threadFactory;

    private String name;
    private Source<Character> in;
    private Sink<Character> out;

    @Inject
    public Builder(ReaderSource.Builder readerSourceBuilder, ThreadFactory threadFactory) {
      this.readerSourceBuilder = readerSourceBuilder;
      this.threadFactory = threadFactory;
    }

    Builder(ReaderSource.Builder readerSourceBuilder, 
        ThreadFactory threadFactory, 
        String name, Source<Character> in, Sink<Character> out) {
      this.readerSourceBuilder = readerSourceBuilder;
      this.threadFactory = threadFactory;
      this.name = name;
      this.in = in;
      this.out = out;
    }
    
    public ActivePipe create() {
      if ((name == null) || (in == null) || (out == null)) {
        throw new NullPointerException();
      }
      return new ActivePipe(name, in, out);
    }

    public ActivePipe comboCreate(String name, Source<Character> in, OutputStream out) {
      return new ActivePipe(name, in, new WriterSink(Writers.buffered(out)));
    }
    
    public ActivePipe comboCreate(String name, InputStream in, Appendable out) {
      return new ActivePipe(
          name, 
          readerSourceBuilder.withIn(Readers.buffered(in)).create(), 
          new WriterSink(Writers.buffered(out)));
    }

    public ActivePipe comboCreate(String name, InputStream in, Sink<Character> out) {
      return new ActivePipe(
          name, 
          readerSourceBuilder.withIn(Readers.buffered(in)).create(), 
          out);
    }

    public Builder withtName(String name) {
      this.name = name;
      return new Builder(readerSourceBuilder, threadFactory, name, in, out);
    }
    
    public Builder withIn(Source<Character> in) {
      this.in = in;
      return new Builder(readerSourceBuilder, threadFactory, name, in, out);
    }
    
    public Builder withOut(Sink<Character> out) {
      this.out = out;
      return new Builder(readerSourceBuilder, threadFactory, name, in, out);
    }
  }
  
  private ActivePipe(String name, Source<Character> in, Sink<Character> out) {
    this.name = name;
    this.in = in;
    this.out = out;
  }
  
  @Override
  public String getName() {
    return "ActivePipe: " + name;
  }

  public void run(){
    while(!in.isEndOfStream()) {
      out.write(in.read());
    }
    out.flush();
    out.close();
  }
  
  //TODO: it shouldn't be ActivePipe's responsibility to create itself. Think.
  public Thread start() {
    ActivePipeThread result = new ActivePipeThread(name, this);
    result.start();
    return result;
  }
  
  public static class ActivePipeThread extends ZThread {

    private final ActivePipe activePipe;

    public ActivePipeThread(String name, ActivePipe activePipe) {
      super(activePipe);
      this.activePipe = activePipe;
    }
    
    //TODO: do I need this?
    public ActivePipe getActivePipe() {
      return activePipe;
    }

    //TODO: kill this?
    @Override
    public void interrupt() {
      activePipe.in.close();
      super.interrupt();
    }
  }
}
