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
}
