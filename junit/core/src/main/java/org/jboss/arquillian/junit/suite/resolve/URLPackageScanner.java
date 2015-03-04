/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.junit.suite.resolve;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jboss.arquillian.core.spi.Validate;

/**
 * Implementation of scanner which can scan a {@link URLClassLoader}
 *
 * @author Thomas Heute
 * @author Gavin King
 * @author Norman Richards
 * @author Pete Muir
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 */
class URLPackageScanner {

    private static final Logger log = Logger.getLogger(URLPackageScanner.class.getName());

    /**
     * Name of the empty package
     */
    private static final String NAME_EMPTY_PACKAGE = "";

    private final String packageName;

    private final String packageNamePath;

    private final boolean addRecursively;

    private final ClassLoader classLoader;

    // private final Set<String> classes = new HashSet<String>();
    private Callback callback;

    /**
     * Factory method to create an instance of URLPackageScanner.
     *
     * @param addRecursively
     *            flag to add child packages
     * @param classLoader
     *            class loader that will have classes added
     * @param pkg
     *            Package that will be scanned
     * @return new instance of URLPackageScanner
     */
    public static URLPackageScanner newInstance(boolean addRecursively, final ClassLoader classLoader,
        final Callback callback, final String packageName) {
        Validate.notNull(packageName, "Package name must be specified");
        Validate.notNull(addRecursively, "AddRecursively must be specified");
        Validate.notNull(classLoader, "ClassLoader must be specified");
        Validate.notNull(callback, "Callback must be specified");

        return new URLPackageScanner(packageName, addRecursively, classLoader, callback);
    }

    /**
     * Factory method to create an instance of URLPackageScanner in the default package
     *
     * @param pkg
     *            Package that will be scanned
     * @param addRecursively
     *            flag to add child packages
     * @param classLoader
     *            class loader that will have classes added
     * @return new instance of URLPackageScanner
     */
    public static URLPackageScanner newInstance(boolean addRecursively, ClassLoader classLoader, Callback callback) {
        Validate.notNull(addRecursively, "AddRecursively must be specified");
        Validate.notNull(classLoader, "ClassLoader must be specified");
        Validate.notNull(callback, "Callback must be specified");

        return new URLPackageScanner(NAME_EMPTY_PACKAGE, addRecursively, classLoader, callback);
    }

    private URLPackageScanner(String packageName, boolean addRecursively, ClassLoader classLoader, Callback callback) {
        this.packageName = packageName;
        this.packageNamePath = packageName.replace(".", "/");
        this.addRecursively = addRecursively;
        this.classLoader = classLoader;
        this.callback = callback;
    }

    public void scanPackage() {
        try {
            Set<String> paths = new HashSet<String>();

            for (URL url : loadResources(packageNamePath)) {
                String urlPath = url.getFile();
                urlPath = URLDecoder.decode(urlPath, "UTF-8");
                if (urlPath.startsWith("file:")) {
                    urlPath = urlPath.substring(5);
                }
                if (urlPath.indexOf('!') > 0) {
                    urlPath = urlPath.substring(0, urlPath.indexOf('!'));
                }
                paths.add(urlPath);
            }
            handle(paths);
        } catch (IOException ioe) {
            log.log(Level.WARNING, "could not read: " + packageName, ioe);
        } catch (ClassNotFoundException ioe) {
            log.log(Level.WARNING, "Class coud not be loaded in package: " + packageName, ioe);
        }
    }

    private void handleArchiveByFile(File file) throws IOException, ClassNotFoundException {
        try {
            log.fine("archive: " + file);
            ZipFile zip = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(packageNamePath) && name.endsWith(".class")
                    && (addRecursively || !name.substring(packageNamePath.length() + 1).contains("/"))) {
                    String className = name.replace("/", ".").substring(0, name.length() - ".class".length());
                    foundClass(className);
                }
            }
        } catch (ZipException e) {
            throw new RuntimeException("Error handling file " + file, e);
        }
    }

    private void handle(Set<String> paths) throws IOException, ClassNotFoundException {
        for (String urlPath : paths) {
            log.fine("scanning: " + urlPath);
            File file = new File(urlPath);
            if (file.isDirectory()) {
                handle(file, packageName);
            } else {
                handleArchiveByFile(file);
            }
        }
    }

    private void handle(File file, String packageName) throws ClassNotFoundException {
        for (File child : file.listFiles()) {
            if (!child.isDirectory() && child.getName().endsWith(".class")) {
                final String packagePrefix = packageName.length() > 0 ? packageName + "." : packageName;
                foundClass(packagePrefix + child.getName().substring(0, child.getName().lastIndexOf(".class")));
            } else if (child.isDirectory() && addRecursively) {
                handle(child, packageName + "." + child.getName());
            }
        }
    }

    private void foundClass(String className) {
        callback.classFound(className);
    }

    private List<URL> loadResources(String name) throws IOException {
        return Collections.list(classLoader.getResources(name));
    }

    /**
     * Callback interface for found classes.
     *
     * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
     * @version $Revision: $
     */
    public interface Callback {
        /**
         * Called for each found class.
         *
         * @param className
         *            The name of the found class
         */
        void classFound(String className);
    }
}
