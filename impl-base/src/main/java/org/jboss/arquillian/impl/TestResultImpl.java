package org.jboss.arquillian.impl;

import org.jboss.arquillian.api.TestResult;

public class TestResultImpl implements TestResult
{
   private static final long serialVersionUID = 1L;

   private Status status;
   private Throwable throwable;

   public TestResultImpl(Status status, Throwable throwable)
   {
      this.status = status;
      this.throwable = throwable;
   }

   @Override
   public Status getStatus()
   {
      return status;
   }
   
   @Override
   public Throwable getThrowable()
   {
      return throwable;
   }
}
