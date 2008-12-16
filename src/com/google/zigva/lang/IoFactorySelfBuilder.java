// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.io.Sink;
import com.google.zigva.io.Source;

@Immutable
public class IoFactorySelfBuilder implements IoFactory {

  private final InFactory inFactory; 
  private final OutFactory outFactory; 
  private final ErrFactory errFactory;
  private final IoFactoryMisc misc;

  public IoFactorySelfBuilder(IoFactory ioFactory, IoFactoryMisc misc) {
    this(ioFactory.in(), ioFactory.out(), ioFactory.err(), ioFactory);
  }
  
  public IoFactorySelfBuilder(
      InFactory inFactory, 
      OutFactory outFactory, 
      ErrFactory errFactory, IoFactoryMisc misc) {
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

  public static ErrFactory err(final Sink<Character> err) {
    return new ErrFactory() {
    
      @Override
      public Sink<Character> buildErr(Source<Character> source) {
        return err;
      }
    };
  }

  public static OutFactory out(final Sink<Character> out) {
    return new OutFactory() {
    
      @Override
      public Sink<Character> buildOut(Source<Character> source) {
        return out;
      }
    };
  }

  public static InFactory in(final Source<Character> in) {
    return new InFactory() {
      @Override
      public Source<Character> buildIn() {
        return in;
      }
    };
  }

  @Override
  public boolean redirectErrToOut() {
    return misc.redirectErrToOut();
  }

  @Override
  public InFactory in() {
    return inFactory;
  }

  @Override
  public OutFactory out() {
    return outFactory;
  }

  @Override
  public ErrFactory err() {
    return errFactory;
  }
}
