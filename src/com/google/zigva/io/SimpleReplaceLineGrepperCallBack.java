package com.google.zigva.io;


import java.util.regex.Matcher;

public class SimpleReplaceLineGrepperCallBack extends RegexLineCatcherCallBack {

  private String replacement;

  public SimpleReplaceLineGrepperCallBack(String replacement, String regex) {
    super (regex);
    this.replacement = replacement;
  }
  
//  public Set<String> getTransformedMatches(){
//    return this.transformedMatches;
//  }
  
  @Override
  protected String transform(String lineToTest, Matcher m) {
    return m.replaceFirst(replacement);
  }


}
