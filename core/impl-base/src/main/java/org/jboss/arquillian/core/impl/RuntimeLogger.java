package org.jboss.arquillian.core.impl;

import org.jboss.arquillian.core.spi.ObserverMethod;

import java.util.Stack;

public class RuntimeLogger {

    public static final String ARQUILLIAN_DEBUG_PROPERTY = "arquillian.debug";
    public static Boolean DEBUG = Boolean.valueOf(SecurityActions.getProperty(ARQUILLIAN_DEBUG_PROPERTY));

    private ThreadLocal<Stack<Object>> eventStack;

    void cleanStack()
    {
        if(eventStack != null)
        {
            eventStack.remove();
        }
    }

    void debug(ObserverMethod method, boolean interceptor)
    {
        if(DEBUG)
        {
            System.out.println(calcDebugPrefix() + "(" + (interceptor ? "I":"O") + ") " +method.getMethod().getDeclaringClass().getSimpleName() + "." + method.getMethod().getName());
        }
    }

    void debugExtension(Class<?> extension)
    {
        if (DEBUG)
        {
            System.out.println(calcDebugPrefix() + "(X) " + extension.getName());
        }
    }

    void debug(Object event, boolean push)
    {
        if(DEBUG)
        {
            if(push)
            {
                System.out.println(calcDebugPrefix() + "(E) " + getEventName(event));
                eventStack.get().push(event);
            }
            else
            {
                if(!eventStack.get().isEmpty())
                {
                    eventStack.get().pop();
                }
            }
        }
    }

    private String getEventName(Object object)
    {
        Class<?> eventClass = object.getClass();
        // Print the Interface name of Anonymous classes to show the defined interface, not creation point.
        if(eventClass.isAnonymousClass() && eventClass.getInterfaces().length == 1 && !eventClass.getInterfaces()[0].getName().startsWith("java"))
        {
            return eventClass.getInterfaces()[0].getSimpleName();
        }
        return eventClass.getSimpleName();
    }

    private String calcDebugPrefix()
    {

        if(eventStack == null)
        {
            eventStack = new ThreadLocal<Stack<Object>>()
            {
                @Override
                protected Stack<Object> initialValue()
                {
                    return new Stack<Object>();
                }
            };
        }

        final int size = eventStack.get().size();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i++)
        {
            sb.append("\t");
        }
        return sb.toString();
    }

}
