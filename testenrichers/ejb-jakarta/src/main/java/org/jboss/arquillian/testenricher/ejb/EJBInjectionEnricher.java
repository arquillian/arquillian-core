/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.testenricher.ejb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;
import jakarta.ejb.EJB;
import javax.naming.Context;
import javax.naming.NamingException;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * Enricher that provide EJB class and setter method injection.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EJBInjectionEnricher implements TestEnricher {
    private static final String ANNOTATION_NAME = "jakarta.ejb.EJB";

    private static final Logger log = Logger.getLogger(TestEnricher.class.getName());

    @Inject
    private Instance<Context> contextInst;

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.TestEnricher#enrich(org.jboss.arquillian.spi .Context, java.lang.Object)
     */
    public void enrich(Object testCase) {
        if (SecurityActions.isClassPresent(ANNOTATION_NAME)) {
            try {
                if (createContext() != null) {
                    injectClass(testCase);
                }
            } catch (Exception e) {
                log.throwing(EJBInjectionEnricher.class.getName(), "enrich", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.TestEnricher#resolve(org.jboss.arquillian.spi .Context, java.lang.reflect.Method)
     */
    public Object[] resolve(Method method) {
        return new Object[method.getParameterTypes().length];
    }

    /**
     * Obtains all field in the specified class which contain the specified annotation
     *
     * @throws IllegalArgumentException
     *     If either argument is not specified
     */
    // TODO Hack, this leaks out privileged operations outside the package. Extract out properly.
    protected List<Field> getFieldsWithAnnotation(final Class<?> clazz, final Class<? extends Annotation> annotation)
        throws IllegalArgumentException {
        // Precondition checks
        if (clazz == null) {
            throw new IllegalArgumentException("clazz must be specified");
        }
        if (annotation == null) {
            throw new IllegalArgumentException("annotation must be specified");
        }

        // Delegate to the privileged operations
        return SecurityActions.getFieldsWithAnnotation(clazz, annotation);
    }

    protected void injectClass(Object testCase) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> ejbAnnotation =
                (Class<? extends Annotation>) SecurityActions.getThreadContextClassLoader().loadClass(ANNOTATION_NAME);

            List<Field> annotatedFields = SecurityActions.getFieldsWithAnnotation(
                testCase.getClass(),
                ejbAnnotation);

            for (Field field : annotatedFields) {
                if (field.get(testCase) == null) // only try to lookup fields that are not already set
                {
                    EJB fieldAnnotation = (EJB) field.getAnnotation(ejbAnnotation);
                    try {
                        String mappedName = fieldAnnotation.mappedName();
                        ;
                        String beanName = fieldAnnotation.beanName();
                        String lookup = attemptToGet31LookupField(fieldAnnotation);

                        String[] jndiNames = resolveJNDINames(field.getType(), mappedName, beanName, lookup);
                        Object ejb = lookupEJB(jndiNames);
                        field.set(testCase, ejb);
                    } catch (Exception e) {
                        log.fine("Could not lookup " + fieldAnnotation + ", other Enrichers might, move on. Exception: "
                            + e.getMessage());
                    }
                }
            }

            List<Method> methods = SecurityActions.getMethodsWithAnnotation(testCase.getClass(), ejbAnnotation);

            for (Method method : methods) {
                if (method.getParameterTypes().length != 1) {
                    throw new RuntimeException("@EJB only allowed on single argument methods");
                }
                if (!method.getName().startsWith("set")) {
                    throw new RuntimeException("@EJB only allowed on 'set' methods");
                }
                EJB parameterAnnotation = null; // method.getParameterAnnotations()[0]
                for (Annotation annotation : method.getParameterAnnotations()[0]) {
                    if (EJB.class.isAssignableFrom(annotation.annotationType())) {
                        parameterAnnotation = (EJB) annotation;
                    }
                }

                // Default values of the annotation attributes.
                String mappedName = null;
                String beanName = null;
                String lookup = null;

                if (parameterAnnotation != null) {
                    mappedName = parameterAnnotation.mappedName();
                    beanName = parameterAnnotation.beanName();
                    lookup = attemptToGet31LookupField(parameterAnnotation);
                }

                String[] jndiNames = resolveJNDINames(method.getParameterTypes()[0], mappedName, beanName, lookup);
                Object ejb = lookupEJB(jndiNames);
                method.invoke(testCase, ejb);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not inject members", e);
        }
    }

    protected String attemptToGet31LookupField(EJB annotation) throws IllegalAccessException,
        InvocationTargetException {
        String lookup = null;
        try {
            Method m = EJB.class.getMethod("lookup");
            lookup = String.valueOf(m.invoke(annotation));
        } catch (NoSuchMethodException e) {
            // No op, running on < 3.1 EJB lib
        }
        return lookup;
    }

    /**
     * Resolves the JNDI name of the given field.
     * <p>
     * If <tt>mappedName</tt>, <tt>lookup</tt> or <tt>beanName</tt> are specified, they're used to resolve JNDI name.
     * Otherwise, default policy
     * applies.
     * <p>
     * If more than one of the <tt>mappedName</tt>, <tt>lookup</tt> and <tt>beanName</tt> {@link EJB} annotation
     * attributes is specified at the same time, an {@link IllegalStateException}
     * will be thrown.
     *
     * @param fieldType
     *     annotated field which JNDI name should be resolved.
     * @param mappedName
     *     Value of {@link EJB}'s <tt>mappedName</tt> attribute.
     * @param beanName
     *     Value of {@link EJB}'s <tt>beanName</tt> attribute.
     * @param lookup
     *     Value of {@link EJB}'s <tt>lookup</tt> attribute.
     *
     * @return possible JNDI names which should be looked up to access the proper object.
     */
    protected String[] resolveJNDINames(Class<?> fieldType, String mappedName, String beanName, String lookup) {

        MessageFormat msg = new MessageFormat(
            "Trying to resolve JNDI name for field \"{0}\" with mappedName=\"{1}\" and beanName=\"{2}\"");
        log.finer(msg.format(new Object[] {fieldType, mappedName, beanName}));

        Validate.notNull(fieldType, "EJB enriched field cannot to be null.");

        boolean isMappedNameSet = hasValue(mappedName);
        boolean isBeanNameSet = hasValue(beanName);
        boolean isLookupSet = hasValue(lookup);

        if (isMoreThanOneValueTrue(isMappedNameSet, isBeanNameSet, isLookupSet)) {
            throw new IllegalStateException(
                "Only one of the @EJB annotation attributes 'mappedName', 'lookup' and 'beanName' can be specified at the same time.");
        }

        String[] jndiNames;

        // If set, use only mapped name or bean name to lookup the EJB.
        if (isMappedNameSet) {
            jndiNames = new String[] {mappedName};
        } else if (isLookupSet) {
            jndiNames = new String[] {lookup};
        } else if (isBeanNameSet) {
            jndiNames = new String[] {"java:module/" + beanName + "!" + fieldType.getName()};
        } else {
            jndiNames = getJndiNamesForAnonymousEJB(fieldType);
        }
        return jndiNames;
    }

    protected String[] getJndiNamesForAnonymousEJB(Class<?> fieldType) {
        String[] jndiNames;
        // TODO: These names are not spec compliant; fieldType needs to be a bean type here, but usually is just an interface of a bean. These seldom work.
        jndiNames = new String[] {
            "java:global/test.ear/test/" + fieldType.getSimpleName() + "Bean",
            "java:global/test.ear/test/" + fieldType.getSimpleName(),
            "java:global/test/" + fieldType.getSimpleName(),
            "java:global/test/" + fieldType.getSimpleName() + "Bean",
            "java:global/test/" + fieldType.getSimpleName() + "/no-interface",
            "java:module/" + fieldType.getSimpleName(),
            "test/" + fieldType.getSimpleName() + "Bean/local",
            "test/" + fieldType.getSimpleName() + "Bean/remote",
            "test/" + fieldType.getSimpleName() + "/no-interface",
            fieldType.getSimpleName() + "Bean/local",
            fieldType.getSimpleName() + "Bean/remote",
            fieldType.getSimpleName() + "/no-interface",
            // WebSphere Application Server Local EJB default binding
            "ejblocal:" + fieldType.getCanonicalName(),
            // WebSphere Application Server Remote EJB default binding
            fieldType.getCanonicalName()};
        return jndiNames;
    }

    protected Object lookupEJB(String[] jndiNames) throws Exception {
        // TODO: figure out test context ?
        Context initcontext = createContext();

        for (String jndiName : jndiNames) {
            try {
                return initcontext.lookup(jndiName);
            } catch (NamingException e) {
                // no-op, try next
            }
        }
        throw new NamingException("No EJB found in JNDI, tried the following names: " + joinJndiNames(jndiNames));
    }

    protected Context createContext() throws Exception {
        return contextInst.get();
    }

    // Simple helper for printing the jndi names
    private String joinJndiNames(String[] strings) {
        StringBuilder sb = new StringBuilder();

        for (String string : strings) {
            sb.append(string).append(", ");
        }
        return sb.toString();
    }

    /**
     * Helper method that checks if the given String has a non-empty value.
     *
     * @param string
     *     String to be checked.
     *
     * @return true if <tt>string</tt> is not null and has non-empty value; false otherwise.
     */
    private boolean hasValue(String string) {
        if (string != null && (!string.trim().equals(""))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isMoreThanOneValueTrue(boolean... values) {
        boolean trueFound = false;
        for (boolean value : values) {
            if (value) {
                if (trueFound) {
                    return true;
                }
                trueFound = true;
            }
        }
        return false;
    }
}
