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
package org.jboss.arquillian.testng.container;

import java.lang.reflect.Method;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;

public class ShouldProvideVariousTestResultsToTestRunner {
    @DataProvider(name = "xx")
    public static Object[][] getCurrentMethod(Method m) {
        return new Object[][] {new Object[] {m}};
    }

    @org.testng.annotations.Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldProvideExpectedExceptionToRunner() throws Exception {
        throw new IllegalArgumentException();
    }

    @org.testng.annotations.Test
    public void shouldProvidePassingTestToRunner() throws Exception {
        Assert.assertTrue(true);
    }

    @org.testng.annotations.Test
    public void shouldProvideFailingTestToRunner() throws Exception {
        Assert.fail("Failing by design");
    }

    @org.testng.annotations.Test
    public void shouldProvideSkippingTestToRunner() throws Exception {
        throw new SkipException("Skipping test", new Throwable("Skip exception"));
    }

    @org.testng.annotations.Test(dataProvider = "xx")
    public void shouldBeAbleToUseOtherDataProviders(Method m) throws Exception {
        Assert.assertNotNull(m);
    }
}
