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

package com.google.zigva.sh;

import java.io.InputStream;
import java.io.OutputStream;

public class StubProcess extends Process {

  private InputStream inputStream = new StubInputStream();
  private InputStream errorStream = new StubInputStream();
  private OutputStream outputStream = new StubOutputStream();

  @Override
  public void destroy() {
  }

  @Override
  public int exitValue() {
    return 0;
  }

  @Override
  public InputStream getErrorStream() {
    return errorStream;
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

  @Override
  public OutputStream getOutputStream() {
    return outputStream;
  }

  @Override
  public int waitFor() {
    return 0;
  }
}
