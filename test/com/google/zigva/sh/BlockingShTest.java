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
