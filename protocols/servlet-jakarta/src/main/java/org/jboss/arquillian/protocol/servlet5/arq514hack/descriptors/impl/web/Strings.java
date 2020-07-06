/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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

package org.jboss.arquillian.protocol.servlet5.arq514hack.descriptors.impl.web;

import java.util.Collection;
import java.util.Iterator;

/**
 * String utilities.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class Strings {
    /**
     * Capitalize the given String: "input" -> "Input"
     */
    public static String capitalize(final String input) {
        if ((input == null) || (input.length() == 0)) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static String unquote(final String value) {
        String result = null;
        if (value != null) {
            result = value.replaceAll("\"(.*)\"", "$1");
        }
        return result;
    }

    public static String enquote(final String value) {
        String result = null;
        if (value != null) {
            result = "\"" + value + "\"";
        }
        return result;
    }

    public static String join(Collection<?> collection, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<?> iter = collection.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || "".equals(string);
    }

    public static boolean isTrue(String value) {
        return value == null ? false : "true".equalsIgnoreCase(value.trim());
    }

    public static boolean areEqual(String left, String right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null || right == null) {
            return false;
        }
        return left.equals(right);
    }

    public static boolean areEqualTrimmed(String left, String right) {
        if (left != null && right != null) {
            return left.trim().equals(right.trim());
        }
        return areEqual(left, right);
    }
}
