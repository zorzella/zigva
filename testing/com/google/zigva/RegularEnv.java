package com.google.zigva;

import com.google.inject.AbstractModule;

import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.zigva.guice.ZivaModule;


public class RegularEnv extends AbstractModule {

  @Override
  protected void configure() {
    install(new ZivaModule());
    bind(TestScopeListener.class).to(NoOpTestScopeListener.class);
  }
}
