package com.google.zigva.lang;

public class RegexReplacement {

  private String regex;
  private String replacement;

  public RegexReplacement(String regex, String replacement) {
    if (regex == null) {
      throw new IllegalArgumentException();
    }
    if (replacement == null) {
      throw new IllegalArgumentException();
    }
    this.regex = regex;
    this.replacement = replacement;
  }

  public String getRegex() {
    return regex;
  }

  public String getReplacement() {
    return replacement;
  }

  public static RegexReplacement[] fromPairs(String... pairs) {
    if (pairs.length % 2 != 0) {
      throw new IllegalArgumentException("Must be called with even number of elements.");
    }
    RegexReplacement[] result = new RegexReplacement[pairs.length / 2];
    for (int i=0; i<result.length; i++) {
      result[i] = new RegexReplacement(pairs[2*i], pairs[2*i + 1]);
    }
    return result;
  }
}
