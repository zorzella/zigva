package com.google.zigva.io;

public class LineGrepper implements Appendable {

  private StringBuilder incompleteLine = new StringBuilder();

  private LineGrepperCallBack[] callBacks;

  private boolean stopOnMatch = true;
  
  public LineGrepper(LineGrepperCallBack... callBacks) {
    this.callBacks = callBacks;
  }
  
  public Appendable append(CharSequence csq) {
    incompleteLine.append(csq);
    dealWithIncompleteLine();
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end) {
    incompleteLine.append(csq, start, end);
    dealWithIncompleteLine();
    return this;
  }

  public Appendable append(char c) {
    incompleteLine.append(c);
    dealWithIncompleteLine();
    return this;
  }

  private void dealWithIncompleteLine() {
    int indexOfNewLine = incompleteLine.indexOf("\n");
    while (indexOfNewLine >= 0) {
      String lineToTest = incompleteLine.substring(0, indexOfNewLine);
      for (LineGrepperCallBack callBack: callBacks) {
        if (callBack.doWork(lineToTest)) {
          if (this.stopOnMatch) {
            break;
          }
        }
      }
      incompleteLine.delete(0, indexOfNewLine + 1);
      indexOfNewLine = incompleteLine.indexOf("\n");
    }
  }

}
