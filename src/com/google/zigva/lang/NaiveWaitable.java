// Copyright 2008 Google Inc.  All Rights Reserved.
package com.google.zigva.lang;

import com.google.zigva.lang.Waitable.CommandFailedException;

/**
 * Like a {@link Waitable}, but without the timeout.
 * 
 * @author zorzella@google.com
 */
public interface NaiveWaitable {

  void waitFor() throws CommandFailedException;

}
