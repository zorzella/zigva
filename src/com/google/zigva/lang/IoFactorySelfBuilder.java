// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.io.Sink;
import com.google.zigva.io.PumpToSink;
import com.google.zigva.io.Pump;
import com.google.zigva.io.Source;

@Immutable
public class IoFactorySelfBuilder implements IoFactory {

  private final SourceFactory<Character> inFactory; 
  private final PumpFactory<Character> outFactory; 
  private final PumpFactory<Character> errFactory;
  private final IoFactoryMisc misc;

  public IoFactorySelfBuilder(IoFactory ioFactory, IoFactoryMisc misc) {
    this(ioFactory.in(), ioFactory.out(), ioFactory.err(), ioFactory);
  }
  
  public IoFactorySelfBuilder(
      SourceFactory<Character> inFactory, 
      PumpFactory<Character> outFactory, 
      PumpFactory<Character> errFactory, IoFactoryMisc misc) {
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

  public static PumpFactory<Character> err(final Sink<Character> err) {
    return new PumpFactory<Character>() {
    
      @Override
      public Pump getPumpFor(Source<Character> source) {
        return new PumpToSink<Character>(source, err);
      }
    };
  }

  public static PumpFactory<Character> out(final Sink<Character> out) {
    return new PumpFactory<Character>() {
    
      @Override
      public Pump getPumpFor(Source<Character> source) {
        return new PumpToSink<Character>(source, out);
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

//  @Override
//  public boolean redirectErrToOut() {
//    return misc.redirectErrToOut();
//  }

  @Override
  public SourceFactory<Character> in() {
    return inFactory;
  }

  @Override
  public PumpFactory<Character> out() {
    return outFactory;
  }

  @Override
  public PumpFactory<Character> err() {
    return errFactory;
  }
}
