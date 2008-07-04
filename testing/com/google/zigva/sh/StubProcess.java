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
