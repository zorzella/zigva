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

import com.google.common.base.Joiner;

import java.io.IOException;

public class RealJavaProcessStarter implements JavaProcessStarter {

  @Override
  public Process start(ProcessBuilder processBuilder) {
    try {
      return processBuilder.start();
    } catch (IOException e) {
      // TODO: document this exception
      throw new ProcessFailedToStartException(String.format(
          "Failed to start process '%s' in '%s' with env '%s'.", 
          Joiner.on(" ").join(processBuilder.command()), 
          processBuilder.directory().toString(),
          processBuilder.environment()), e);
    } catch (Exception e) {
      // TODO: document this exception
      throw new ProcessFailedToStartException(String.format(
          "Failed to start process '%s' in '%s' with env '%s'.", 
          Joiner.on(" ").join(processBuilder.command()), 
          processBuilder.directory().toString(),
          processBuilder.environment()), e);
    }
  }
}
