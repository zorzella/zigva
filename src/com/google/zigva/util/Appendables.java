package com.google.zigva.util;

import java.util.Formatter;
import java.util.Locale;

public class Appendables {
  
  /**
   * Equivalent to {@link String#format(String, Object...)}, except that it
   * can be used on an Appendable, rather than a String.
   */
  public static void format(Appendable out, String format, Object... args) {
    Formatter formatter = new Formatter(out);
    formatter.format(Locale.getDefault(), format, args);
  }
  
  /**
   * Equivalent to {@link String#format(Locale, String, Object...)} except that it
   * can be used on an Appendable, rather than a String.
   */
  public static void format(Locale locale, Appendable out, String format, Object... args) {
    Formatter formatter = new Formatter(out);
    formatter.format(locale, format, args);
  }
  
//  public static String join(CharSequence separator, Iterator it){
//    if (it == null) {
//      return null;
//    }
//    StringBuilder result = new StringBuilder();
//    if (it.hasNext()) {
//      result .append(it.next());
//    }
//    while(it.hasNext()) {
//      result.append(separator);
//      result.append(it.next());
//    }
//    return result.toString();
//    
//  }
//  
//  public static String joinArray(CharSequence separator, Object[] args){
//    return join(separator, Arrays.asList(args).iterator());
//  }
//  
//  public static String join(String separator, Object... args){
//    return joinArray(separator, args);
//  }
  
}
