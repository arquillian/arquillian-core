/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.spi;

import java.io.Serializable;

/**
 * A test result which may be serialized for communicate between client and
 * server
 * 
 * @author Pete Muir
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 *
 */
public class TestResult implements Serializable
{
   private static final long serialVersionUID = 1L;

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

   private Status status;
   private Throwable throwable;

   /**
    * Create a empty result. 
    */
   public TestResult()
   {
   }
   
   /**
    * Create a new TestResult.
    * 
    * @param status The result status.
    */
   public TestResult(Status status)
   {
      this(status, null);
   }
   
   /**
    * Create a new TestResult.
    * 
    * @param status The result status.
    * @param throwable thrown exception if any
    */
   public TestResult(Status status, Throwable throwable)
   {
      this.status = status;
      this.throwable = throwable;
   }

   /**
    * Get the status of this test
    */
   public Status getStatus() 
   {
      return status;
   }

   public void setStatus(Status status)
   {
      this.status = status;
   }
   
   /**
    * If the test failed, the exception that was thrown. It does not need to be
    * the root cause.
    */
   public Throwable getThrowable() 
   {
      return throwable;
   }
   
   public void setThrowable(Throwable throwable)
   {
      this.throwable = throwable;
   }
}