package org.jboss.arquillian.core.spi;

import java.util.Hashtable;

/**
 * Mapping for ThreadId to a value. Same as "ThreadLocal", but with simpler cleanup.
 *
 */
public class ArquillianThreadLocal<T> {
  private Hashtable<Long, T> table = new Hashtable<Long, T>();

  protected T initialValue() {
    return null;
  }

  /**
   * Returns the value in the current thread's copy of this
   * thread-local variable.  If the variable has no value for the
   * current thread, it is first initialized to the value returned
   * by an invocation of the {@link #initialValue} method.
   *
   * @return the current thread's value of this thread-local
   */
  public T get() {
    Thread t = Thread.currentThread();
    long threadId = t.getId();

    if (table.containsKey(threadId)) {
        return table.get(threadId);
    }
    else {
      T value = initialValue();
      table.put(threadId, value);
      return value;
    }
  }

  /**
   * Removes the current thread's value for this thread-local
   * variable.
   *
   */
   public void remove() {
     Thread t = Thread.currentThread();
     long threadId = t.getId();

     if (table.containsKey(threadId)) {
       table.remove(threadId);
     }
   }

   /**
    * Clears the cache
    */
   public void clear() {
     table.clear();
   }
}

