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
package org.jboss.arquillian.testng;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.TestAnnotation;

/**
 * A IAnnotationTransformer that will add the {@link TestEnricherDataProvider} as {@link DataProvider}
 * to the given test method to enable method argument injection support.
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestDataProviderTransformer implements IAnnotationTransformer {

    /* (non-Javadoc)
     * @see org.testng.IAnnotationTransformer#transform(org.testng.annotations.ITestAnnotation, java.lang.Class, java.lang.reflect.Constructor, java.lang.reflect.Method)
     */
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation testAnnotation, Class clazz, Constructor constructor, Method method) {
        if (testAnnotation.getDataProviderClass() == null) {
            if (testAnnotation instanceof TestAnnotation) {
                TestAnnotation annotation = (TestAnnotation) testAnnotation;
                annotation.setDataProviderClass(TestEnricherDataProvider.class);
                annotation.setDataProvider(TestEnricherDataProvider.PROVIDER_NAME);
            }
        }
    }
}
