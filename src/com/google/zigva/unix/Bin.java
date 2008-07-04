package com.google.zigva.unix;

import com.google.zigva.sh.BlockingSh;

public class Bin {

  public static void cp(String source, String destination) {
    BlockingSh.run(String.format("cp %s %s", source, destination));
  }

}
