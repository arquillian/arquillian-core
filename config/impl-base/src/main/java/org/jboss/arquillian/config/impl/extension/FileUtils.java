/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.config.impl.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * FileUtils
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
class FileUtils {
    static Properties loadArquillianProperties(String propertyName, String defaultName) {
        Properties props = new Properties();
        FileName resourceName = getConfigFileName(propertyName, defaultName);
        final InputStream input = loadResource(resourceName);
        if (input != null) {
            try {
                props.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Could not load Arquillian properties file, " + resourceName.getName(), e);
            }
        }
        props.putAll(System.getProperties());
        return props;
    }

    static InputStream loadArquillianXml(String propertyName, String defaultName) {
        FileName resourceName = getConfigFileName(propertyName, defaultName);
        return loadResource(resourceName);
    }

    static InputStream loadResource(FileName resourceName) {
        InputStream stream = loadClassPathResource(resourceName.getName());
        if (stream == null) {
            stream = loadFileResource(resourceName.getName());
        }
        // only throw Exception if configured (non default) could not be found
        if (stream == null && !resourceName.isDefault()) {
            throw new IllegalArgumentException(
                "Could not find configured filename as either classpath resource nor file resource: "
                    + resourceName.getName());
        }
        return stream;
    }

    static InputStream loadFileResource(String resourceName) {
        File file = new File(resourceName);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // should not happen unless file has been deleted since we did file.exists call
                throw new IllegalArgumentException("Configuration file could not be found, " + resourceName);
            }
        }
        return null;
    }

    static InputStream loadClassPathResource(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(resourceName);
    }

    static FileName getConfigFileName(String propertyName, String defaultName) {
        String name = System.getProperty(propertyName);
        if (name == null) {
            return new FileName(defaultName, true);
        }
        return new FileName(name, false);
    }

    static class FileName {
        private String name;
        private boolean isDefault;

        public FileName(String name, boolean isDefault) {
            this.name = name;
            this.isDefault = isDefault;
        }

        public String getName() {
            return name;
        }

        public boolean isDefault() {
            return isDefault;
        }
    }
}