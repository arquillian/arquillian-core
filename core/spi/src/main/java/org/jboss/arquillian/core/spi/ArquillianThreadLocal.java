package org.jboss.arquillian.core.spi;

/**
 * Thread-local value holder backed by a real {@link ThreadLocal}.
 * A prior implementation used a map keyed by thread id, which leaked stale
 * values when a pool recycled a thread's id.
 */
public class ArquillianThreadLocal<T> {

  private volatile ThreadLocal<T> delegate = newDelegate();

  private ThreadLocal<T> newDelegate() {
    return new ThreadLocal<T>() {
      @Override
      protected T initialValue() {
        return ArquillianThreadLocal.this.initialValue();
      }
    };
  }

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
    return delegate.get();
  }

  /**
   * Removes the current thread's value for this thread-local variable.
   */
  public void remove() {
    delegate.remove();
  }

  /**
   * Drops every thread's value by swapping in a fresh backing {@link ThreadLocal};
   * subsequent {@link #get()} calls go back through {@link #initialValue()}.
   */
  public void clear() {
    delegate = newDelegate();
  }
}
