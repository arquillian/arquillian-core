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
package org.jboss.arquillian.testenricher.resource;

import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;

public class ResourceInjectionEnricherTestCase {
    private final Map<String, Object> injectionValueMap = new HashMap<String, Object>();

    {
        injectionValueMap.put("primitive_char", '1');
        injectionValueMap.put("primitive_byte", Byte.valueOf("1"));
        injectionValueMap.put("primitive_int", 10);
        injectionValueMap.put("primitive_short", (short) 10);
        injectionValueMap.put("primitive_long", 10L);
        injectionValueMap.put("primitive_float", 10f);
        injectionValueMap.put("primitive_double", 10d);
        injectionValueMap.put("primitive_boolean", true);
        injectionValueMap.put("primitive_non_default_value", 120);

        injectionValueMap.put("primitive", 100);
        injectionValueMap.put("primitive2", 100);
        injectionValueMap.put("object", this);
        injectionValueMap.put("object2", this);
    }

    @Test
    public void shouldInjectResourcesIntoObject() throws Exception {
        InjectableTestClass testClass = new InjectableTestClass();

        new ResourceInjectionEnricher() {
            protected Object lookup(String jndiName) throws Exception {
                return injectionValueMap.get(jndiName);
            }

            ;
        }.injectClass(testClass);

        Assert.assertEquals(
            "Should be able to inject into primitive field",
            injectionValueMap.get("primitive"),
            testClass.getPrimitive());

        Assert.assertEquals(
            "Should be able to inject into primitive setter",
            injectionValueMap.get("primitive2"),
            testClass.getPrimitive2());

        Assert.assertEquals(
            "Should be able to inject into object field",
            injectionValueMap.get("object"),
            testClass.getObject());

        Assert.assertEquals(
            "Should be able to inject into object ssetter",
            injectionValueMap.get("object2"),
            testClass.getObject2());

        Assert.assertEquals(
            "Should inject primitive if it has default value",
            injectionValueMap.get("primitive_char"),
            testClass.primitive_char);

        Assert.assertEquals(
            "Should inject primitive if it has default value",
            injectionValueMap.get("primitive_byte"),
            testClass.primitive_byte);

        Assert.assertEquals(
            "Should inject primitive if it has default value",
            injectionValueMap.get("primitive_int"),
            testClass.primitive_int);

        Assert.assertEquals(
            "Should inject primitive if it has default value",
            injectionValueMap.get("primitive_short"),
            testClass.primitive_short);

        Assert.assertEquals(
            "Should inject primitive if it has default value",
            injectionValueMap.get("primitive_long"),
            testClass.primitive_long);

        Assert.assertEquals(
            "Should inject primitive if it has default value",
            injectionValueMap.get("primitive_float"),
            testClass.primitive_float);

        Assert.assertEquals(
            "Should inject primitive if it has default value",
            injectionValueMap.get("primitive_double"),
            testClass.primitive_double);

        Assert.assertEquals(
            "Should inject primitive if it has default value",
            injectionValueMap.get("primitive_boolean"),
            testClass.primitive_boolean);

        Assert.assertNotSame(
            "Should not inject primitive if it does not have default value",
            injectionValueMap.get("primitive_non_default_value"),
            testClass.primitive_non_default_value);
    }

    private static class InjectableTestClass {
        @Resource(mappedName = "primitive_char")
        public char primitive_char;
        protected int primitive2;
        @Resource(mappedName = "primitive_byte")
        byte primitive_byte;
        @Resource(mappedName = "primitive_int")
        int primitive_int;
        @Resource(mappedName = "primitive_short")
        short primitive_short;
        @Resource(mappedName = "primitive_long")
        long primitive_long;
        @Resource(mappedName = "primitive_float")
        float primitive_float;
        @Resource(mappedName = "primitive_double")
        double primitive_double;
        @Resource(mappedName = "primitive_boolean")
        boolean primitive_boolean;
        @Resource(mappedName = "primitive_non_default_value")
        int primitive_non_default_value = 100;
        @Resource(mappedName = "primitive")
        private int primitive;
        @Resource(mappedName = "object")
        private Object object;

        private Object object2;

        public int getPrimitive() {
            return primitive;
        }

        public int getPrimitive2() {
            return primitive2;
        }

        @SuppressWarnings("unused")
        @Resource(mappedName = "primitive")
        public void setPrimitive2(int primitive2) {
            this.primitive2 = primitive2;
        }

        public Object getObject() {
            return object;
        }

        public Object getObject2() {
            return object2;
        }

        @SuppressWarnings("unused")
        @Resource(mappedName = "object")
        public void setObject2(Object object2) {
            this.object2 = object2;
        }
    }
}
