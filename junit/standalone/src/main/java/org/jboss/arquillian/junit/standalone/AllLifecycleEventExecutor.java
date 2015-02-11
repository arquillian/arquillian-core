package org.jboss.arquillian.junit.standalone;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.LifecycleEvent;

public class AllLifecycleEventExecutor
{

    public void on(@Observes(precedence = -100) BeforeClass event) throws Throwable
    {
        execute(event);
    }

    public void on(@Observes(precedence = 100) AfterClass event) throws Throwable
    {
        execute(event);
    }

    public void on(@Observes(precedence = -100) BeforeTestLifecycleEvent event) throws Throwable
    {
        execute(event);
    }

    public void on(@Observes(precedence = 100) AfterTestLifecycleEvent event) throws Throwable
    {
        execute(event);
    }

    private void execute(LifecycleEvent event) throws Throwable
    {
       event.getExecutor().invoke();
    }
}
