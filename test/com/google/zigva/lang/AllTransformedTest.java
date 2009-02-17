/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zigva.lang;

import com.google.zigva.collections.AllTransformed;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AllTransformedTest extends TestCase {

  String[] SET_A_ARRAY = {
    "c",
    "a",
    "b",
    "ac",
    "ab",
  };

  List<String> SET_A_SET = Arrays.asList(SET_A_ARRAY);    

  public void testListTransformer() throws Exception {

    Lambda<String,List<String>> function = new Lambda<String,List<String>>() {
      public List<String> apply(String object) {
        List<String> result = new ArrayList<String>();
        result.add(object + "-lambaded");
        result.add(object);
        return result;
      }
    };

    Iterator<String> it = SET_A_SET.iterator();
    it = AllTransformed.list(it, function).iterator();

    String[] EXPECTED = {
      "c-lambaded", "c",
      "a-lambaded", "a",
      "b-lambaded", "b",
      "ac-lambaded", "ac",
      "ab-lambaded", "ab",
    };
    for (String expected: EXPECTED) {
      assertTrue (expected, it.hasNext());
      assertEquals(expected, it.next());
    }
  }
}

