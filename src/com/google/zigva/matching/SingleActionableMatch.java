package com.google.zigva.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleActionableMatch implements ActionableMatch {

  private Pattern pattern;
  private List<String> results;
  
  public SingleActionableMatch(String regex) {
    this.pattern = Pattern.compile(regex);
  }

  public SingleActionableMatch(Pattern pattern) {
    this.pattern = pattern;
  }

  public Pattern pattern() {
    return pattern;
  }

  public boolean doWork(String lineToTest) {
    Matcher m = pattern().matcher(lineToTest);
    if (m.matches()) {
      if (results != null) {
        throw new IllegalStateException ("Matched pattern more than once.");
      }
      results = new ArrayList<String>(m.groupCount()); 
      for (int i=0; i<=m.groupCount();i++){
        results.add(m.group(i));
      }
      return true;
    }
    return false;
  }

  public String getResult(int index) {
    return this.results.get(index);
  }
  
}
