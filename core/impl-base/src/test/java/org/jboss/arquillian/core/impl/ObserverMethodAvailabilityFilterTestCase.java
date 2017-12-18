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
package org.jboss.arquillian.core.impl;

import java.util.List;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * ObserverMethodAvailabilityFilterTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ObserverMethodAvailabilityFilterTestCase extends AbstractManagerTestBase {
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(ObserverMultiArgument.class);
    }

    @Test
    public void shouldCallFilteredMethodsIfInContext() throws Exception {
        bind(ApplicationScoped.class, Integer.class, 10);

        fire(new String("_TEST_"));

        ObserverMultiArgument extension = getManager().getExtension(ObserverMultiArgument.class);

        Assert.assertTrue(
            "Non filtered method should have been called",
            extension.wasCalled);

        Assert.assertTrue(
            "Filtered method should not have been called, filter not in context",
            extension.filteredWasCalled);
    }

    @Test
    public void shouldNotCallFilteredMethodsIfNotInContext() throws Exception {
        fire(new String("_TEST_"));

        ObserverMultiArgument extension = getManager().getExtension(ObserverMultiArgument.class);

        Assert.assertTrue(
            "Non filtered method should have been called",
            extension.wasCalled);

        Assert.assertFalse(
            "Filtered method should not have been called, filter not in context",
            extension.filteredWasCalled);
    }

    public static class ObserverMultiArgument {
        private boolean wasCalled = false;
        private boolean filteredWasCalled = false;

        public void single(@Observes String test) {
            wasCalled = true;
        }

        public void filtered(@Observes String test, Integer filter) {
            Assert.assertNotNull(filter);
            filteredWasCalled = true;
        }
    }
}
