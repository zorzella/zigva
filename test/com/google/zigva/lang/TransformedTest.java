package com.google.zigva.lang;

import com.google.zigva.collections.Transformed;
import com.google.zigva.lang.Lambda;

import junit.framework.TestCase;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TransformedTest extends TestCase {

  String[] SET_A_ARRAY = {
    "c",
    "a",
    "b",
    "ac",
    "ab",
  };

  List<String> SET_A_SET = Arrays.asList(SET_A_ARRAY);    

  public void testListTransformer() throws Exception {

    Lambda<String,String> function = new Lambda<String,String>() {
      public String apply(String object) {
        return object + "-lambaded";
      }
    };

    Iterator<String> it = SET_A_SET.iterator();
    it = Transformed.list(it, function).iterator();

    String[] EXPECTED = {
      "c-lambaded",
      "a-lambaded",
      "b-lambaded",
      "ac-lambaded",
      "ab-lambaded",
    };
    for (String expected: EXPECTED) {
      assertTrue (expected, it.hasNext());
      assertEquals(expected, it.next());
    }
  }
}

