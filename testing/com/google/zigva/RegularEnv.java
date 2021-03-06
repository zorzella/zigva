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

package com.google.zigva;

import com.google.inject.AbstractModule;
import com.google.inject.testing.guiceberry.NoOpTestScopeListener;
import com.google.inject.testing.guiceberry.TestScopeListener;
import com.google.inject.testing.guiceberry.junit3.BasicJunit3Module;
import com.google.zigva.guice.ZigvaModule;


public class RegularEnv extends AbstractModule {

  @Override
  protected void configure() {
    install(new BasicJunit3Module());
    install(new ZigvaModule());
    bind(TestScopeListener.class).to(NoOpTestScopeListener.class);
  }
}
