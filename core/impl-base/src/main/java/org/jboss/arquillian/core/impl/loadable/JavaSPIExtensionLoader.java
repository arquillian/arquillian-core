/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.core.impl.loadable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.jboss.arquillian.core.spi.ExtensionLoader;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.Validate;

/**
 * ServiceLoader implementation that use META-INF/services/interface files to registered Services.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JavaSPIExtensionLoader implements ExtensionLoader {
    //-------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    private static final String SERVICES = "META-INF/services";
    private static final String EXCLUSIONS = "META-INF/exclusions";

    //-------------------------------------------------------------------------------------||
    // Required Implementations - ExtensionLoader -----------------------------------------||
    //-------------------------------------------------------------------------------------||

    @Override
    public Collection<LoadableExtension> load() {
        return all(JavaSPIExtensionLoader.class.getClassLoader(), LoadableExtension.class);
    }

    @Override
    public Map<Class<?>, Set<Class<?>>> loadVetoed() {
        return loadVetoed(JavaSPIExtensionLoader.class.getClassLoader());
    }

    //-------------------------------------------------------------------------------------||
    // General JDK SPI Loader -------------------------------------------------------------||
    //-------------------------------------------------------------------------------------||

    public <T> Collection<T> all(ClassLoader classLoader, Class<T> serviceClass) {
        Validate.notNull(classLoader, "ClassLoader must be provided");
        Validate.notNull(serviceClass, "ServiceClass must be provided");

        return createInstances(
            serviceClass,
            load(serviceClass, classLoader));
    }

    /**
     * This method first finds all files that are in claspath placed at META-INF/exclusions
     * Each of this file has a name that represents the service type that needs to veto.
     * The content of this file is a list of real implementations that you want to veto.
     *
     * @return List of vetos
     */
    public Map<Class<?>, Set<Class<?>>> loadVetoed(ClassLoader classLoader) {

        Validate.notNull(classLoader, "ClassLoader must be provided");

        final Map<Class<?>, Set<Class<?>>> vetoed = new LinkedHashMap<Class<?>, Set<Class<?>>>();

        try {
            final Enumeration<URL> exclusions = classLoader.getResources(EXCLUSIONS);

            while (exclusions.hasMoreElements()) {
                URL exclusion = exclusions.nextElement();
                Properties vetoedElements = new Properties();
                final InputStream inStream = exclusion.openStream();

                try {
                    vetoedElements.load(inStream);

                    final Set<Map.Entry<Object, Object>> entries = vetoedElements.entrySet();

                    for (Map.Entry<Object, Object> entry : entries) {
                        String service = (String) entry.getKey();
                        String serviceImpls = (String) entry.getValue();

                        addVetoedClasses(service, serviceImpls, classLoader, vetoed);
                    }
                } finally {
                    if (inStream != null) {
                        inStream.close();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load exclusions from " + EXCLUSIONS, e);
        }

        return vetoed;
    }

    //-------------------------------------------------------------------------------------||
    // Internal Helper Methods - Service Loading ------------------------------------------||
    //-------------------------------------------------------------------------------------||

    private void addVetoedClasses(String serviceName, String serviceImpls, ClassLoader classLoader,
        Map<Class<?>, Set<Class<?>>> vetoed) {
        try {
            final Class<?> serviceClass = classLoader.loadClass(serviceName);
            final Set<Class<?>> classes = loadVetoedServiceImpl(serviceImpls, classLoader);

            final Set<Class<?>> registeredVetoedClasses = vetoed.get(serviceClass);
            if (registeredVetoedClasses == null) {
                vetoed.put(serviceClass, classes);
            } else {
                registeredVetoedClasses.addAll(classes);
            }
        } catch (ClassNotFoundException e) {
            // ignores since this is a veto that it is not applicable
        }
    }

    private Set<Class<?>> loadVetoedServiceImpl(String serviceImpls, ClassLoader classLoader) {

        final StringTokenizer serviceImplsSeparator = new StringTokenizer(serviceImpls, ",");
        final Set<Class<?>> serviceImplsClass = new LinkedHashSet<Class<?>>();

        while (serviceImplsSeparator.hasMoreTokens()) {
            try {
                serviceImplsClass.add(classLoader.loadClass(serviceImplsSeparator.nextToken().trim()));
            } catch (ClassNotFoundException e) {
                // ignores since this is a veto that it is not applicable
            }
        }

        return serviceImplsClass;
    }

    private <T> Set<Class<? extends T>> load(Class<T> serviceClass, ClassLoader loader) {
        String serviceFile = SERVICES + "/" + serviceClass.getName();

        Set<Class<? extends T>> providers = new LinkedHashSet<Class<? extends T>>();
        Set<Class<? extends T>> vetoedProviders = new LinkedHashSet<Class<? extends T>>();

        try {
            Enumeration<URL> enumeration = loader.getResources(serviceFile);
            while (enumeration.hasMoreElements()) {
                final URL url = enumeration.nextElement();
                final InputStream is = url.openStream();
                BufferedReader reader = null;

                try {
                    reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String line = reader.readLine();
                    while (null != line) {
                        line = skipCommentAndTrim(line);

                        if (line.length() > 0) {
                            try {
                                boolean mustBeVetoed = line.startsWith("!");
                                if (mustBeVetoed) {
                                    line = line.substring(1);
                                }

                                Class<? extends T> provider = loader.loadClass(line).asSubclass(serviceClass);

                                if (mustBeVetoed) {
                                    vetoedProviders.add(provider);
                                }

                                if (vetoedProviders.contains(provider)) {
                                    providers.remove(provider);
                                } else {
                                    providers.add(provider);
                                }
                            } catch (ClassCastException e) {
                                throw new IllegalStateException("Service " + line + " does not implement expected type "
                                    + serviceClass.getName());
                            }
                        }
                        line = reader.readLine();
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load services for " + serviceClass.getName(), e);
        }
        return providers;
    }

    private String skipCommentAndTrim(String line) {
        final int comment = line.indexOf('#');
        if (comment > -1) {
            line = line.substring(0, comment);
        }

        line = line.trim();
        return line;
    }

    private <T> Set<T> createInstances(Class<T> serviceType, Set<Class<? extends T>> providers) {
        Set<T> providerImpls = new LinkedHashSet<T>();
        for (Class<? extends T> serviceClass : providers) {
            providerImpls.add(createInstance(serviceClass));
        }
        return providerImpls;
    }

    /**
     * Create a new instance of the found Service. <br/>
     * <p>
     * Verifies that the found ServiceImpl implements Service.
     *
     * @param serviceType
     *     The Service interface
     * @param className
     *     The name of the implementation class
     * @param loader
     *     The ClassLoader to load the ServiceImpl from
     *
     * @return A new instance of the ServiceImpl
     *
     * @throws Exception
     *     If problems creating a new instance
     */
    private <T> T createInstance(Class<? extends T> serviceImplClass) {
        try {
            return SecurityActions.newInstance(serviceImplClass, new Class<?>[0], new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException(
                "Could not create a new instance of Service implementation " + serviceImplClass.getName(), e);
        }
    }
}
