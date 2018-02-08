/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package org.jboss.arquillian.config.impl.extension;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * A utility class for replacing properties in strings.
 * <p>
 * NOTE: Copied from jboss-common-core.jar
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="claudio.vesco@previnet.it">Claudio Vesco</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version <tt>$Revision: 2898 $</tt>
 */
public final class StringPropertyReplacer {

    /**
     * Environment variable base property
     */
    private static final String ENV_VAR_BASE_PROPERTY_KEY = "env.";

    // States used in property parsing
    private static final int NORMAL = 0;
    private static final int SEEN_DOLLAR = 1;
    private static final int IN_BRACKET = 2;

    /**
     * Go through the input string and replace any occurrence of ${classpath(p)} with
     * the classpath URL value. If there is no such p defined in classpath,
     * then the ${p} reference will remain unchanged.
     *
     * @param string
     *     - the string with possible ${classpath()} references
     *
     * @return the input string with all property references replaced if any.
     * If there are no valid references the input string will be returned.
     */
    public static String replaceClasspath(String string) {
        return replaceProperties(string, new ClasspathPropertyResolver());
    }

    /**
     * Go through the input string and replace any occurrence of ${p} with
     * the System.getProtocolProperty(p) value. If there is no such property p defined,
     * then the ${p} reference will remain unchanged.
     * <p>
     * If the property reference is of the form ${p:v} and there is no such property p,
     * then the default value v will be returned.
     * <p>
     * If the property reference is of the form ${p1,p2} or ${p1,p2:v} then
     * the primary and the secondary properties will be tried in turn, before
     * returning either the unchanged input, or the default value.
     * <p>
     * The property ${/} is replaced with System.getProtocolProperty("file.separator")
     * value and the property ${:} is replaced with System.getProtocolProperty("path.separator").
     * <p>
     * Prior to resolving variables, environment variables are assigned to the
     * collection of properties. Each environment variable is prefixed with the
     * prefix "env.". If a system property is already defined for the prefixed
     * environment variable, the system property is honored as an override
     * (primarily for testing).
     *
     * @param string
     *     - the string with possible ${} references
     *
     * @return the input string with all property references replaced if any.
     * If there are no valid references the input string will be returned.
     */
    public static String replaceProperties(final String string) {
        Properties props = System.getProperties();
        for (Map.Entry<String, String> var : System.getenv().entrySet()) {
            String propKey = ENV_VAR_BASE_PROPERTY_KEY + var.getKey();
            // honor overridden environment variable (primarily for testing)
            if (!props.containsKey(propKey)) {
                props.setProperty(propKey, var.getValue());
            }
        }
        return replaceProperties(string, new PropertiesPropertyResolver(props));
    }

    /**
     * Go through the input string and replace any occurrence of ${p} with
     * the props.getProtocolProperty(p) value. If there is no such property p defined,
     * then the ${p} reference will remain unchanged.
     * <p>
     * If the property reference is of the form ${p:v} and there is no such property p,
     * then the default value v will be returned.
     * <p>
     * If the property reference is of the form ${p1,p2} or ${p1,p2:v} then
     * the primary and the secondary properties will be tried in turn, before
     * returning either the unchanged input, or the default value.
     * <p>
     * The property ${/} is replaced with System.getProtocolProperty("file.separator")
     * value and the property ${:} is replaced with System.getProtocolProperty("path.separator").
     *
     * @param string
     *     - the string with possible ${} references
     * @param propertyResolver
     *     - property resolver to get the value from detected key
     *
     * @return the input string with all property references replaced if any.
     * If there are no valid references the input string will be returned.
     */
    private static String replaceProperties(final String string, PropertyResolver propertyResolver) {
        final char[] chars = string.toCharArray();
        StringBuffer buffer = new StringBuffer();
        boolean properties = false;
        int state = NORMAL;
        int start = 0;
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];

            // Dollar sign outside brackets
            if (c == '$' && state != IN_BRACKET) {
                state = SEEN_DOLLAR;
            }

            // Open bracket immediately after dollar
            else if (c == '{' && state == SEEN_DOLLAR) {
                buffer.append(string.substring(start, i - 1));
                state = IN_BRACKET;
                start = i - 1;
            }

            // No open bracket after dollar
            else if (state == SEEN_DOLLAR) {
                state = NORMAL;
            }

            // Closed bracket after open bracket
            else if (c == '}' && state == IN_BRACKET) {
                // No content
                if (start + 2 == i) {
                    buffer.append("${}"); // REVIEW: Correct?
                } else // Collect the system property
                {
                    String value;

                    String key = string.substring(start + 2, i);

                    value = propertyResolver.getValue(key);

                    if (value != null) {
                        properties = true;
                        buffer.append(value);
                    } else {
                        buffer.append("${");
                        buffer.append(key);
                        buffer.append('}');
                    }
                }
                start = i + 1;
                state = NORMAL;
            }
        }

        // No properties
        if (properties == false) {
            return string;
        }

        // Collect the trailing characters
        if (start != chars.length) {
            buffer.append(string.substring(start, chars.length));
        }

        // Done
        return buffer.toString();
    }


    public interface PropertyResolver {
        String getValue(String key);
    }

    static class ClasspathPropertyResolver implements PropertyResolver {

        /**
         * Classpath base property
         */
        private static final String CLASSPATH = "classpath(";

        public String getValue(String key) {

            if (key.startsWith(CLASSPATH)) {
                final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                String classpathResource = key.substring(CLASSPATH.length(), key.length() - 1);
                final URL resource = contextClassLoader.getResource(classpathResource);

                //If resource is not found it is returned as null so no change is applicable.
                if (resource == null) {
                    return null;
                }

                return resource.toString();
            }

            return null;
        }
    }

    static class PropertiesPropertyResolver implements PropertyResolver {

        /**
         * File separator value
         */
        private static final String FILE_SEPARATOR = File.separator;

        /**
         * Path separator value
         */
        private static final String PATH_SEPARATOR = File.pathSeparator;

        /**
         * File separator alias
         */
        private static final String FILE_SEPARATOR_ALIAS = "/";

        /**
         * Path separator alias
         */
        private static final String PATH_SEPARATOR_ALIAS = ":";


        private final Properties props;

        PropertiesPropertyResolver(Properties props) {
            this.props = props;
        }

        public String getValue(String key) {

            String value;

            if (FILE_SEPARATOR_ALIAS.equals(key)) {
                value = FILE_SEPARATOR;
            } else if (PATH_SEPARATOR_ALIAS.equals(key)) {
                value = PATH_SEPARATOR;
            } else {
                // check from the properties
                if (props != null) {
                    value = props.getProperty(key);
                } else {
                    value = System.getProperty(key);
                }

                if (value == null) {
                    // Check for a default value ${key:default}
                    int colon = key.indexOf(':');
                    if (colon > 0) {
                        String realKey = key.substring(0, colon);
                        if (props != null) {
                            value = props.getProperty(realKey);
                        } else {
                            value = System.getProperty(realKey);
                        }

                        if (value == null) {
                            // Check for a composite key, "key1,key2"
                            value = resolveCompositeKey(realKey, props);

                            // Not a composite key either, use the specified default
                            if (value == null) {
                                value = key.substring(colon + 1);
                            }
                        }
                    } else {
                        // No default, check for a composite key, "key1,key2"
                        value = resolveCompositeKey(key, props);
                    }
                }
            }

            return value;
        }

        /**
         * Try to resolve a "key" from the provided properties by
         * checking if it is actually a "key1,key2", in which case
         * try first "key1", then "key2". If all fails, return null.
         * <p>
         * It also accepts "key1," and ",key2".
         *
         * @param key
         *     the key to resolve
         * @param props
         *     the properties to use
         *
         * @return the resolved key or null
         */
        private String resolveCompositeKey(String key, Properties props) {
            String value = null;

            // Look for the comma
            int comma = key.indexOf(',');
            if (comma > -1) {
                // If we have a first part, try resolve it
                if (comma > 0) {
                    // Check the first part
                    String key1 = key.substring(0, comma);
                    if (props != null) {
                        value = props.getProperty(key1);
                    } else {
                        value = System.getProperty(key1);
                    }
                }
                // Check the second part, if there is one and first lookup failed
                if (value == null && comma < key.length() - 1) {
                    String key2 = key.substring(comma + 1);
                    if (props != null) {
                        value = props.getProperty(key2);
                    } else {
                        value = System.getProperty(key2);
                    }
                }
            }
            // Return whatever we've found or null
            return value;
        }
    }

}
