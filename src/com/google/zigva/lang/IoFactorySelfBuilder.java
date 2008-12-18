// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.io.Sink;
import com.google.zigva.io.SimpleSink;
import com.google.zigva.io.PassiveSink;
import com.google.zigva.io.Source;

@Immutable
public class IoFactorySelfBuilder implements IoFactory {

  private final SourceFactory<Character> inFactory; 
  private final SinkFactory<Character> outFactory; 
  private final SinkFactory<Character> errFactory;
  private final IoFactoryMisc misc;

  public IoFactorySelfBuilder(IoFactory ioFactory, IoFactoryMisc misc) {
    this(ioFactory.in(), ioFactory.out(), ioFactory.err(), ioFactory);
  }
  
  public IoFactorySelfBuilder(
      SourceFactory<Character> inFactory, 
      SinkFactory<Character> outFactory, 
      SinkFactory<Character> errFactory, IoFactoryMisc misc) {
    this.inFactory = inFactory;
    this.outFactory = outFactory;
    this.errFactory = errFactory;
    this.misc = misc;
  }
  
  public IoFactorySelfBuilder withIn(final Source<Character> in) {
    return new IoFactorySelfBuilder(in(in), this.out(), this.err(), misc);
  }

  public IoFactorySelfBuilder withOut(final PassiveSink<Character> out) {
    return new IoFactorySelfBuilder(this.in(), out(out), this.err(), misc);
  }

  public IoFactorySelfBuilder withErr(final PassiveSink<Character> err) {
    return new IoFactorySelfBuilder(this.in(), this.out(), err(err), misc);
  }

  public static SinkFactory<Character> err(final PassiveSink<Character> err) {
    return new SinkFactory<Character>() {
    
      @Override
      public Sink build(Source<Character> source) {
        return new SimpleSink<Character>(source, err);
      }
    };
  }

  public static SinkFactory<Character> out(final PassiveSink<Character> out) {
    return new SinkFactory<Character>() {
    
      @Override
      public Sink build(Source<Character> source) {
        return new SimpleSink<Character>(source, out);
      }
    };
  }

  public static SourceFactory<Character> in(final Source<Character> in) {
    return new SourceFactory<Character>() {
      @Override
      public Source<Character> build() {
        return in;
      }
    };
  }

  @Override
  public boolean redirectErrToOut() {
    return misc.redirectErrToOut();
  }

  @Override
  public SourceFactory<Character> in() {
    return inFactory;
  }

  @Override
  public SinkFactory<Character> out() {
    return outFactory;
  }

  @Override
  public SinkFactory<Character> err() {
    return errFactory;
  }
}
