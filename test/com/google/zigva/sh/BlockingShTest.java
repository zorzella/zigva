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

package com.google.zigva.sh;

import com.google.zigva.sh.BlockingSh;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

public class BlockingShTest extends TestCase {

	private static final File THIS_FOLDER = new File (".");

	public void testReadLines() throws Exception {
		List<String> lines = BlockingSh.readLines("echo -e 'a\nb'", THIS_FOLDER);
		assertEquals (2, lines.size());
	}

	public void testReadLinesFromTwoCommands() throws Exception {
		List<String> lines = BlockingSh.readLines("echo -e 'a\nb'; echo c", THIS_FOLDER);
		assertEquals (3, lines.size());
	}

	public void testReadLinesWithError() throws Exception {
		List<String> lines = BlockingSh.readLines("echo -e 'a\nb'; cho c", THIS_FOLDER);
		assertEquals (2, lines.size());
	}
	
	
}
