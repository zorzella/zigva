package com.google.zigva.matching;

import java.util.regex.Pattern;

public interface ActionableMatch {

  Pattern pattern();

  boolean doWork(String lineToTest);

}
