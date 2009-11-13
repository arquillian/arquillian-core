package org.jboss.arquillian.api;

import java.io.Serializable;

/**
 * A test result which may be serialized for communicate between client and
 * server
 * 
 * @author Pete Muir
 *
 */
public interface TestResult extends Serializable
{
   
   /**
    * The test status
    * @author Pete Muir
    *
    */
   public enum Status
   {
      /**
       * The test passed
       */
      PASSED,
      /**
       * The test failed
       */
      FAILED,
      /**
       * The test was skipped due to some deployment problem
       */
      SKIPPED;
   }
   
   /**
    * Get the status of this test
    */
   public Status getStatus();
   
   /**
    * If the test failed, the exception that was thrown. It does not need to be
    * the root cause.
    */
   public Throwable getThrowable();
   
}