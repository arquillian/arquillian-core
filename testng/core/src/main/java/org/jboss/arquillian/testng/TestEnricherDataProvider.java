/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.testng;

import java.lang.reflect.Method;
import org.testng.annotations.DataProvider;

/**
 * TestEnricherDataProvider
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 */
public class TestEnricherDataProvider {

    public static final String PROVIDER_NAME = "enrich";

    @DataProvider(name = PROVIDER_NAME)
    public static Object[][] enrich(Method method) {
        // actual enrichment happens inside a Observer
        Object[] parameterValues = new Object[method.getParameterTypes().length];
        Object[][] values = new Object[1][method.getParameterTypes().length];
        values[0] = parameterValues;

        return values;
    }
}
