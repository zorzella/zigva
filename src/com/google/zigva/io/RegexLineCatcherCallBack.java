package com.google.zigva.io;


import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexLineCatcherCallBack implements LineGrepperCallBack {

  protected Set<String> transformedMatches = new HashSet<String>();

  private Pattern p;

  public RegexLineCatcherCallBack(String regex) {
    p = Pattern.compile(regex);
  }
  
  public Set<String> getTransformedMatches(){
    return this.transformedMatches;
  }
  
  public boolean doWork(String lineToTest) {
    Matcher m = p.matcher(lineToTest);
    if (m.matches()){
      transformedMatches.add(transform(lineToTest, m));
      return true;
    }
    return false;
    
  }

  protected String transform(String lineToTest, Matcher m) {
    return lineToTest;
  }


}
