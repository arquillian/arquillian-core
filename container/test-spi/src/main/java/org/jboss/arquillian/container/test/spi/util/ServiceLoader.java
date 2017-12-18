/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.test.spi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class handles looking up service providers on the class path. It
 * implements the <a href="http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider"
 * >Service Provider section of the JAR File Specification</a>.
 * <p>
 * The Service Provider programmatic lookup was not specified prior to Java 6 so
 * this interface allows use of the specification prior to Java 6.
 * <p>
 * The API is copied from <a
 * href="http://java.sun.com/javase/6/docs/api/java/util/ServiceLoader.html"
 * >java.util.ServiceLoader</a>
 *
 * @author Pete Muir
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ServiceLoader<S> implements Iterable<S> {

    private static final String SERVICES = "META-INF/services";
    private final String serviceFile;
    private final ClassLoader loader;
    private Class<S> expectedType;
    private Set<S> providers;
    private ServiceLoader(Class<S> service, ClassLoader loader) {
        this.loader = loader;
        this.serviceFile = SERVICES + "/" + service.getName();
        this.expectedType = service;
    }

    /**
     * Creates a new service loader for the given service type, using the current
     * thread's context class loader.
     * <p>
     * An invocation of this convenience method of the form
     * <p>
     * {@code ServiceLoader.load(service)</code>}
     * <p>
     * is equivalent to
     * <p>
     * <code>ServiceLoader.load(service,
     * Thread.currentThread().getContextClassLoader())</code>
     *
     * @param service
     *     The interface or abstract class representing the service
     *
     * @return A new service loader
     */
    public static <S> ServiceLoader<S> load(Class<S> service) {
        return load(service, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a new service loader for the given service type and class loader.
     *
     * @param service
     *     The interface or abstract class representing the service
     * @param loader
     *     The class loader to be used to load provider-configuration
     *     files and provider classes, or null if the system class loader
     *     (or, failing that, the bootstrap class loader) is to be used
     *
     * @return A new service loader
     */
    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        if (loader == null) {
            loader = service.getClassLoader();
        }
        return new ServiceLoader<S>(service, loader);
    }

    /**
     * Creates a new service loader for the given service type, using the
     * extension class loader.
     * <p>
     * This convenience method simply locates the extension class loader, call it
     * extClassLoader, and then returns
     * <p>
     * <code>ServiceLoader.load(service, extClassLoader)</code>
     * <p>
     * If the extension class loader cannot be found then the system class loader
     * is used; if there is no system class loader then the bootstrap class
     * loader is used.
     * <p>
     * This method is intended for use when only installed providers are desired.
     * The resulting service will only find and load providers that have been
     * installed into the current Java virtual machine; providers on the
     * application's class path will be ignored.
     *
     * @param service
     *     The interface or abstract class representing the service
     *
     * @return A new service loader
     */
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        throw new UnsupportedOperationException();
    }

    /**
     * Clear this loader's provider cache so that all providers will be reloaded.
     * <p>
     * After invoking this method, subsequent invocations of the iterator method
     * will lazily look up and instantiate providers from scratch, just as is
     * done by a newly-created loader.
     * <p>
     * This method is intended for use in situations in which new providers can
     * be installed into a running Java virtual machine.
     */
    public void reload() {
        providers = new LinkedHashSet<S>();
        Enumeration<URL> enumeration = null;
        boolean errorOccurred = false;

        try {
            enumeration = loader.getResources(serviceFile);
        } catch (IOException ioe) {
            errorOccurred = true;
        }

        if (!errorOccurred) {
            while (enumeration.hasMoreElements()) {
                try {
                    final URL url = enumeration.nextElement();
                    final InputStream is = url.openStream();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    String line = reader.readLine();
                    while (null != line) {
                        try {
                            final int comment = line.indexOf('#');

                            if (comment > -1) {
                                line = line.substring(0, comment);
                            }

                            line.trim();

                            if (line.length() > 0) {
                                providers.add(createInstance(line));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            // TODO Don't use exceptions for flow control!
                            // try the next line
                        }

                        line = reader.readLine();
                    }
                    reader.close();
                } catch (Exception e) {
                    // try the next file
                }
            }
        }
    }

    public S createInstance(String line) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
        NoClassDefFoundError, InstantiationException, IllegalAccessException {
        try {
            Class<?> clazz = loader.loadClass(line);
            Class<? extends S> serviceClass;
            try {
                serviceClass = clazz.asSubclass(expectedType);
            } catch (ClassCastException e) {
                throw new IllegalStateException(
                    "Service " + line + " does not implement expected type " + expectedType.getName());
            }
            Constructor<? extends S> constructor = serviceClass.getConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance();
        } catch (NoClassDefFoundError e) {
            throw e;
        } catch (InstantiationException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw e;
        }
    }

    /**
     * Lazily loads the available providers of this loader's service.
     * <p>
     * The iterator returned by this method first yields all of the elements of
     * the provider cache, in instantiation order. It then lazily loads and
     * instantiates any remaining providers, adding each one to the cache in
     * turn.
     * <p>
     * To achieve laziness the actual work of parsing the available
     * provider-configuration files and instantiating providers must be done by
     * the iterator itself. Its hasNext and next methods can therefore throw a
     * ServiceConfigurationError if a provider-configuration file violates the
     * specified format, or if it names a provider class that cannot be found and
     * instantiated, or if the result of instantiating the class is not
     * assignable to the service type, or if any other kind of exception or error
     * is thrown as the next provider is located and instantiated. To write
     * robust code it is only necessary to catch ServiceConfigurationError when
     * using a service iterator.
     * <p>
     * If such an error is thrown then subsequent invocations of the iterator
     * will make a best effort to locate and instantiate the next available
     * provider, but in general such recovery cannot be guaranteed.
     * <p>
     * Design Note Throwing an error in these cases may seem extreme. The
     * rationale for this behavior is that a malformed provider-configuration
     * file, like a malformed class file, indicates a serious problem with the
     * way the Java virtual machine is configured or is being used. As such it is
     * preferable to throw an error rather than try to recover or, even worse,
     * fail silently.
     * <p>
     * The iterator returned by this method does not support removal. Invoking
     * its remove method will cause an UnsupportedOperationException to be
     * thrown.
     *
     * @return An iterator that lazily loads providers for this loader's service
     */
    public Iterator<S> iterator() {
        if (providers == null) {
            reload();
        }
        return providers.iterator();
    }

    public Set<S> getProviders() {
        if (providers == null) {
            reload();
        }
        return Collections.unmodifiableSet(providers);
    }

    /**
     * Returns a string describing this service.
     *
     * @return A descriptive string
     */
    @Override
    public String toString() {
        return "Services for " + serviceFile;
    }
}
