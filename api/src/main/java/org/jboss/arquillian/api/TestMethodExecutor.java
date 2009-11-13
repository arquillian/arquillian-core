package org.jboss.arquillian.api;

import java.lang.reflect.Method;

public interface TestMethodExecutor
{
   Method getMethod();
   Object getInstance();
   void invoke() throws Throwable;
   
}
