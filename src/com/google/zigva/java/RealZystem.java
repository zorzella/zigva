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

import com.google.zigva.exec.CommandExecutor;
import com.google.zigva.io.FilePath;
import com.google.zigva.lang.IoFactory;
import com.google.zigva.lang.Propertiez;
import com.google.zigva.lang.UserInfo;
import com.google.zigva.lang.Zystem;

import java.net.UnknownHostException;
import java.util.Map;

public class RealZystem implements Zystem {

  private final IoFactory ioFactory;
  private final FilePath currentDir;
  private final FilePath homeDir;
  private final UserInfo userInfo;
  private final Map<String, String> env;
  
  public RealZystem(
      IoFactory ioFactory,
      FilePath currentDir,
      FilePath homeDir,
      UserInfo userInfo,
      Map<String, String> env) {
    this.ioFactory = ioFactory;
    this.currentDir = currentDir;
    this.homeDir = homeDir;
    this.userInfo = userInfo;
    this.env = env;
  }
  
  @Override
  public String toString() {
    return String.format("[%s]", currentDir);
  }

  @Override
  public String getHostname() {
    String temp = null;
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return System.getenv("HOSTNAME");
    }
  }

  @Override
  public FilePath getCurrentDir() {
    return currentDir;
  }

  @Override
  public FilePath getHomeDir() {
    return homeDir;
  }

  @Override
  public Propertiez properties() {
    return new RealPropertiez();
  }

  @Override
  public Map<String, String> env() {
    return env;
  }

  @Override
  public IoFactory ioFactory() {
    return ioFactory;
  }

  @Override
  public UserInfo userInfo() {
    return userInfo;
  }
  
  public CommandExecutor executor() {
    return null;
  }
}
