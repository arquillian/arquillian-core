package org.jboss.arquillian.container.impl;

public class Converter {

    /**
     * Converts a String value to the specified class.
     *
     * @param clazz
     * @param value
     * @return
     */
    public static Object convert(Class<?> clazz, String value) {

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
