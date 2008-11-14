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

import com.google.zigva.collections.LambdaTransformingList;
import com.google.zigva.lang.Lambda;

import junit.framework.TestCase;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LambdaTransformingListTest extends TestCase {

  String[] SET_A_ARRAY = {
    "c",
    "a",
    "b",
    "ac",
    "ab",
  };

  private Lambda<String,String> NO_OP_LAMBDA = new Lambda<String, String>() {
    @Override
    public String apply(String object) {
      return object;
    }
  };
  
  public void testListTransformer() throws Exception {

    Lambda<String,String> function = new Lambda<String,String>() {
      public String apply(String object) {
        return object + "-lambaded";
      }
    };

    LambdaTransformingList<String> ltl = getList(function);
    
    ltl.addAll(Arrays.asList(SET_A_ARRAY));

    Iterator<String> it = ltl.iterator();
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

  public void testToArray() throws Exception {
    LambdaTransformingList<String> list = getList(NO_OP_LAMBDA);
    String[] asArray = list.toArray(new String[]{});
    Object[] asArrayOfObject = list.toArray();
    int i=-1;
    for (String element: list) {
      i++;
      assertEquals(element, asArray[i]);
      assertEquals(element, asArrayOfObject[i]);
    }
  }
  
  private LambdaTransformingList<String> getList(Lambda<String, String> function) {
    List<String> SET_A_SET = Arrays.asList(SET_A_ARRAY);    

    LambdaTransformingList<String> ltl = 
      LambdaTransformingList.newInstance(function);
    return ltl;
  }
}

