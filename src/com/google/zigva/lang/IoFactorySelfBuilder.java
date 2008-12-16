// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;

@Immutable
public class IoFactorySelfBuilder implements IoFactory {

  private final SourceFactory inFactory; 
  private final SinkFactory outFactory; 
  private final SinkFactory errFactory;
  private final IoFactoryMisc misc;

  public IoFactorySelfBuilder(IoFactory ioFactory, IoFactoryMisc misc) {
    this(ioFactory.in(), ioFactory.out(), ioFactory.err(), ioFactory);
  }
  
  public IoFactorySelfBuilder(
      SourceFactory inFactory, 
      SinkFactory outFactory, 
      SinkFactory errFactory, IoFactoryMisc misc) {
    this.inFactory = inFactory;
    this.outFactory = outFactory;
    this.errFactory = errFactory;
    this.misc = misc;
  }
  
  public IoFactorySelfBuilder withIn(final Source<Character> in) {
    return new IoFactorySelfBuilder(in(in), this.out(), this.err(), misc);
  }

  public IoFactorySelfBuilder withOut(final Sink<Character> out) {
    return new IoFactorySelfBuilder(this.in(), out(out), this.err(), misc);
  }

  public IoFactorySelfBuilder withErr(final Sink<Character> err) {
    return new IoFactorySelfBuilder(this.in(), this.out(), err(err), misc);
  }

  public static SinkFactory err(final Sink<Character> err) {
    return new SinkFactory() {
    
      @Override
      public Sink<Character> build(Source<Character> source) {
        return err;
      }
    };
  }

  public static SinkFactory out(final Sink<Character> out) {
    return new SinkFactory() {
    
      @Override
      public Sink<Character> build(Source<Character> source) {
        return out;
      }
    };
  }

  public static SourceFactory in(final Source<Character> in) {
    return new SourceFactory() {
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
  public SourceFactory in() {
    return inFactory;
  }

  @Override
  public SinkFactory out() {
    return outFactory;
  }

  @Override
  public SinkFactory err() {
    return errFactory;
  }
}
