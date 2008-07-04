package com.google.zigva;

import com.google.zigva.io.AppendableWriterTest;
import com.google.zigva.io.BasicZystemExecutorTest;
import com.google.zigva.io.CircularBufferTest;
import com.google.zigva.io.LazyCreateFileAppendableTest;
import com.google.zigva.io.ReadersTest;
import com.google.zigva.io.TimeBombOutputParserTest;
import com.google.zigva.io.ZFileTest;
import com.google.zigva.io.ZivaEnvs;
import com.google.zigva.io.ZystemExecutorLiveTest;
import com.google.zigva.lang.AllTransformedTest;
import com.google.zigva.lang.FilterIteratorTest;
import com.google.zigva.lang.LambdaTransformingListTest;
import com.google.zigva.lang.TransformedTest;
import com.google.zigva.sh.BlockingShTest;
import com.google.zigva.sh.OSTest;
import com.google.zigva.sh.ZivaProcessBuilderTest;
import com.google.zigva.sh.ZivaTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.google.zigva");
    //$JUnit-BEGIN$
    suite.addTestSuite(AppendableWriterTest.class);
    suite.addTestSuite(BasicZystemExecutorTest.class);
    suite.addTestSuite(CircularBufferTest.class);
    suite.addTestSuite(LazyCreateFileAppendableTest.class);
    suite.addTestSuite(ReadersTest.class);
    suite.addTestSuite(TimeBombOutputParserTest.class);
    suite.addTestSuite(ZFileTest.class);
    suite.addTestSuite(ZivaEnvs.class);
    suite.addTestSuite(ZystemExecutorLiveTest.class);
    suite.addTestSuite(AllTransformedTest.class);
    suite.addTestSuite(FilterIteratorTest.class);
    suite.addTestSuite(LambdaTransformingListTest.class);
    suite.addTestSuite(TransformedTest.class);
    suite.addTestSuite(BlockingShTest.class);
    suite.addTestSuite(OSTest.class);
    suite.addTestSuite(ZivaProcessBuilderTest.class);
    suite.addTestSuite(ZivaTests.class);
    
    //$JUnit-END$
    return suite;
  }

}
