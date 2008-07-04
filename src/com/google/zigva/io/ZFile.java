package com.google.zigva.io;

import com.google.zigva.lang.RegexReplacement;
import com.google.zigva.sh.ActivePipe;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZFile {

  public enum FileCreationFailurePolicy {
    FAIL_IF_FILE_EXISTS,
    FAIL_IF_FILE_EXISTS_AND_IS_A_DIRECTORY,
    DELETE_DIR_IF_EXISTS_TO_CREATE_FILE,
    NO_OP_IF_FILE_EXISTS,
  }

  /**
   * Given a path to a directory, creates this directory, creating all parent
   * directories if needed.
   * 
   * @param path a path to a directory we want to create. Each element in the
   *          array is a sudirectory
   * @return the created directory
   * @throws RuntimeException if the dir can't be created
   */
  public static File createDirAndParentsIfNeeded(String[] path) {

    String osPath = "";
    // initialize it
    File dir = new File(osPath);
    for (String step : path) {
      dir = new File(osPath += step);
      osPath += File.separatorChar;

      if (dir.exists()) {
        if (!dir.isDirectory()) {
          throw new RuntimeException("File " + dir
            + " exists and is not a directory.");
        }
      } else {
        if (!dir.mkdir()) {
          throw new RuntimeException("Folder " + dir + " cannot be created.");
        }
      }
    }
    return dir;
  }

  public static void fileReplaceRegexp(File fileToChange, String regex,
      String replacement) {
    fileReplaceRegexp(fileToChange, new RegexReplacement(regex, replacement));
  }
  
  public static void fileReplaceRegexp(File fileToChange, RegexReplacement... rrs) {
    String contents = readFileContents(fileToChange);
    if (contents == null) {
      throw new IllegalArgumentException(String.format(
        "File '%s' does not exist or can't be read.", fileToChange.getAbsolutePath()));
    }
    boolean modified = false;
    for (RegexReplacement rr : rrs) {
      Pattern p = Pattern.compile(rr.getRegex());
      Matcher matcher = p.matcher(contents);
      if (matcher.find()) {
        modified  = true;
        contents = matcher.replaceAll(rr.getReplacement());
      }
    }
    if (modified) {
    createFileWithContents(fileToChange, contents,
      FileCreationFailurePolicy.FAIL_IF_FILE_EXISTS_AND_IS_A_DIRECTORY, false);
    }
  }
  
  public static void createFileWithContents(File file, CharSequence contents,
      FileCreationFailurePolicy policy, boolean append) {
    if (file.exists()) {
      if (policy == FileCreationFailurePolicy.FAIL_IF_FILE_EXISTS) {
        throw new RuntimeException(String.format("File '%s' exists.", file
          .getAbsolutePath()));
      }
      if (policy == FileCreationFailurePolicy.NO_OP_IF_FILE_EXISTS) {
        return;
      }
      if (file.isDirectory()) {
        if (policy == FileCreationFailurePolicy.DELETE_DIR_IF_EXISTS_TO_CREATE_FILE) {
          file.delete();
        } else {
          throw new RuntimeException(String.format(
            "File '%s' exists and is a directory.", file.getAbsolutePath()));
        }
      }
    }
    try {
      PrintStream out = new PrintStream(new BufferedOutputStream(
        new FileOutputStream(file, append)));
      out.print(contents);
      out.close();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static String readFileContents(File toRead) {
    FileInputStream in;
    try {
      in = new FileInputStream(toRead);
      StringBuilder builder = new StringBuilder();
      Thread t = new Thread(new ActivePipe(in, builder));
      t.start();
      t.join();
      in.close();
      return builder.toString();
    } catch (FileNotFoundException e) {
      return null;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  // FIXME: a MUCH more efficient implementation is needed
  public static Iterator<String> readFileLines(File toRead) {
    String temp = readFileContents(toRead);
    String[] split = temp.split("\\n");
    Iterator<String> result = Arrays.asList(split).iterator();
    return result;
  }

}