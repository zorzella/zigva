// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.command;

import com.google.zigva.exec.CommandExecutor.Command;
import com.google.zigva.io.CharacterSource;
import com.google.zigva.io.Source;
import com.google.zigva.lang.CommandResponse;
import com.google.zigva.sys.Zystem;

public class Echo implements Command {

  private final CharSequence toBeEchoed;
  
  public Echo(CharSequence toBeEchoed) {
    this.toBeEchoed = toBeEchoed;
  }
  
  public static Echo fromFormatted(String toBeFormatted, Object... params) {
    return new Echo(String.format(toBeFormatted, params));
  }

  @Override
  public CommandResponse go(Zystem zystem, Source<Character> in) {
    in.close();
    return CommandResponse.forOut(this, new CharacterSource(toBeEchoed));
  }
}