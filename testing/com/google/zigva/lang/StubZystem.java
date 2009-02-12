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

package com.google.zigva.lang;

import com.google.zigva.io.FilePath;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.Propertiez;
import com.google.zigva.lang.UserInfo;
import com.google.zigva.lang.Zystem;

import java.util.Map;

public class StubZystem implements Zystem {

  @Override
  public FilePath getCurrentDir() {
    return null;
  }

  @Override
  public String getHostname() {
    return null;
  }

  @Override
  public FilePath getHomeDir() {
    return null;
  }

  @Override
  public Propertiez properties() {
    return null;
  }

  @Override
  public Map<String, String> env() {
    return null;
  }

  @Override
  public IoFactory ioFactory() {
    return null;
  }

  @Override
  public UserInfo userInfo() {
    return null;
  }
}
