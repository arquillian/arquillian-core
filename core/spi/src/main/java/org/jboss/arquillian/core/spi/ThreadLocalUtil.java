package org.jboss.arquillian.core.spi;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadLocalUtil {
    private static Logger log = Logger.getLogger(ThreadLocalUtil.class.getName());

    /**
     * Force cleanup of a thread local variable: using reflection, all entries for the "ThreadLocalMap" are removed.
     * The entries are searched for all active threads.
     *
     * @param threadLocal This is the threadlocal variable to be cleaned up.
     */
    public static void forceCleanupThreadLocal(ThreadLocal<?> threadLocal) {
      //Get all threads:
      ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
      ThreadGroup parentGroup;
      while ((parentGroup = rootGroup.getParent()) != null) {
          rootGroup = parentGroup;
      }
      Thread[] threads = new Thread[rootGroup.activeCount()];
      while (rootGroup.enumerate(threads, true ) == threads.length) {
          threads = new Thread[threads.length * 2];
      }
      //Cleanup for each thread:
      for (Thread thread : threads)
      {
          //The "threads" array has NULL entries...
          if (thread != null)
          {
             cleanThreadLocals(thread, threadLocal);
          }
      }
    }

    /**
     * Performs cleanup of the ThreadLocal entry for a specific thread.
     *
     * @param thread
     * @param threadLocal
     */
    private static void cleanThreadLocals(Thread thread, ThreadLocal<?> threadLocal) {
        try {
            Class<?> threadLocalClass = Class.forName("java.lang.ThreadLocal");
            Method getMapMethod = threadLocalClass.getDeclaredMethod("getMap", Thread.class);
            getMapMethod.setAccessible(true);

            Object threadLocalMap = getMapMethod.invoke(threadLocal, thread);

            if (threadLocalMap != null)
            {
                Class<?> threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
                Method removeMethod = threadLocalMapClass.getDeclaredMethod("remove", ThreadLocal.class);
                removeMethod.setAccessible(true);

                removeMethod.invoke(threadLocalMap, threadLocal);
            }
        } catch(Exception e) {
            // We will tolerate an exception here and just log it
            log.log(Level.SEVERE, "Arquillian failed to cleanup threadlocals - did the Java API change?", e);
        }
    }
}
