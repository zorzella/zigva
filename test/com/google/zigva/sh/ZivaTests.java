package com.google.zigva.sh;

import com.google.zigva.sh.BlockingSh;
import com.google.zigva.sh.ZivaProcess;

import junit.framework.TestCase;


public class ZivaTests extends TestCase {

  
  public void testProcessStatus() throws Exception {
    ZivaProcess zp = BlockingSh.run("ls");
    assertEquals(0, zp.exitValue());
  }
  
  
}
