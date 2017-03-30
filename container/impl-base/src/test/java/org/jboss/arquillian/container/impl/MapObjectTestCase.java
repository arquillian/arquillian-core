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
package org.jboss.arquillian.container.impl;

import java.util.HashMap;
import junit.framework.Assert;
import org.jboss.arquillian.config.descriptor.api.Multiline;
import org.junit.Test;

/**
 * MapObjectTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class MapObjectTestCase {
    private static final String VAL_STRING = "test123";
    private static final String VAL_MULTILINE_STRING =
        "\n\n\n\n\r\n\t\t\ttest123 \r\n\t\t\t\ttest123" + System.getProperty("line.separator");
    private static final Integer VAL_INTEGER = 123;
    private static final Boolean VAL_BOOLEAN = true;
    private static final Double VAL_DOUBLE = 3.4;

    @Test
    public void shouldBeAbleToSetString() throws Exception {
        TestObject test = new TestObject();
        MapObject.populate(test, map("s", VAL_STRING));

        Assert.assertEquals(VAL_STRING, test.s);
    }

    @Test
    public void shouldBeAbleToSetInteger() throws Exception {
        TestObject test = new TestObject();
        MapObject.populate(test, map("i", VAL_INTEGER));

        Assert.assertEquals(VAL_INTEGER, test.i);
    }

    @Test
    public void shouldBeAbleToSetDouble() throws Exception {
        TestObject test = new TestObject();
        MapObject.populate(test, map("d", VAL_DOUBLE));

        Assert.assertEquals(VAL_DOUBLE, test.d);
    }

    @Test
    public void shouldBeAbleToSetBoolean() throws Exception {
        TestObject test = new TestObject();
        MapObject.populate(test, map("b", VAL_BOOLEAN));

        Assert.assertEquals(VAL_BOOLEAN, test.b);
    }

    @Test // log.warning is produced, Manual test?
    public void shouldNotFailOnUnusedOptions() throws Exception {
        TestObject test = new TestObject();
        MapObject.populate(test, map("a", VAL_BOOLEAN));
    }

    @Test
    public void shouldKeepMultiline() throws Exception {
        TestObject test = new TestObject();
        MapObject.populate(test, map("m", VAL_MULTILINE_STRING));
        Assert.assertEquals(VAL_MULTILINE_STRING, test.m);
    }

    @Test
    public void shouldTrimIfNotAnnotatedWithMultiline() throws Exception {
        TestObject test = new TestObject();
        MapObject.populate(test, map("s", VAL_MULTILINE_STRING));
        Assert.assertEquals(VAL_STRING + " " + VAL_STRING, test.s);
    }

    private ChainedMap map(String name, Object value) {
        return new ChainedMap().map(name, value);
    }

    public static class TestObject {
        private String s;
        private Integer i;
        private Double d;
        private Boolean b;
        private String m;

        public void setS(String s) {
            this.s = s;
        }

        public void setI(Integer i) {
            this.i = i;
        }

        public void setD(Double d) {
            this.d = d;
        }

        public void setB(Boolean b) {
            this.b = b;
        }

        @Multiline
        public void setM(String m) {
            this.m = m;
        }
    }

    private class ChainedMap extends HashMap<String, String> {
        private static final long serialVersionUID = 8237042898515778650L;

        public ChainedMap map(String name, Object value) {
            put(name, String.valueOf(value));
            return this;
        }
    }
}
