/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.container.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.jboss.arquillian.config.descriptor.api.Multiline;

/**
 * MapObjectPopulator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class MapObject {

    public static Logger log = Logger.getLogger(MapObject.class.getName());

    public static void populate(Object object, Map<String, String> values) throws Exception {
        final Map<String, String> clonedValues = new HashMap<String, String>(values);
        final Set<String> candidates = new HashSet<String>();
        final Class<?> clazz = object.getClass();
        for (Method candidate : clazz.getMethods()) {
            if (isSetter(candidate)) {
                candidate.setAccessible(true);
                final String methodName = candidate.getName();
                String propertyName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
                candidates.add(propertyName);
                if (clonedValues.containsKey(propertyName)) {
                    if (shouldBeTrimmed(candidate)) {
                        String trimmed = MultilineTrimmer.trim(clonedValues.get(propertyName));
                        clonedValues.put(propertyName, trimmed);
                    }
                    candidate.invoke(
                        object,
                        convert(candidate.getParameterTypes()[0], clonedValues.get(propertyName)));
                    clonedValues.remove(propertyName);
                }
            }
        }
        if (!clonedValues.isEmpty()) {
            log.warning(
                "Configuration contain properties not supported by the backing object " + clazz.getName() + "\n" +
                    "Unused property entries: " + clonedValues + "\n" +
                    "Supported property names: " + candidates);
        }
    }

    public static URL[] convert(File[] files) {
        final URL[] urls = new URL[files.length];
        try {
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURI().toURL();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create URL from a File object?", e);
        }
        return urls;
    }

    private static boolean isSetter(Method candidate) {
        return candidate.getName().matches("^set[A-Z].*") &&
            candidate.getReturnType().equals(Void.TYPE) &&
            candidate.getParameterTypes().length == 1;
    }

    private static boolean shouldBeTrimmed(Method candidate) {
        return (String.class.equals(candidate.getParameterTypes()[0]) && !candidate.isAnnotationPresent(Multiline.class));
    }

    /**
     * Converts a String value to the specified class.
     */
    private static Object convert(Class<?> clazz, String value) {
      /* TODO create a new Converter class and move this method there for reuse */

        if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
            return Integer.valueOf(value);
        } else if (Double.class.equals(clazz) || double.class.equals(clazz)) {
            return Double.valueOf(value);
        } else if (Long.class.equals(clazz) || long.class.equals(clazz)) {
            return Long.valueOf(value);
        } else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            return Boolean.valueOf(value);
        }

        return value;
    }
}