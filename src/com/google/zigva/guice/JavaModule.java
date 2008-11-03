// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.guice;

import com.google.inject.AbstractModule;

import java.util.concurrent.ThreadFactory;

public class JavaModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ThreadFactory.class).to(ZigvaThreadFactory.class);
  }

}
