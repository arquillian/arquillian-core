/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.core.impl;

import java.util.List;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * InjectorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class InjectorTestCase extends AbstractManagerTestBase {
    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(TestObserver.class);
    }

    @Test
    public void shouldBeAbleToDoStaticInjection() throws Exception {
        bind(ApplicationScoped.class, Object.class, new Object());

        fire("test event");

        Assert.assertTrue(getManager().getExtension(TestObserver.class).wasCalled);
    }

    private static class TestObserver {
        private boolean wasCalled;

        @Inject
        private Instance<Injector> injectorInstance;

        @SuppressWarnings("unused")
        public void on(@Observes String test) {
            TestStaticInjected target = new TestStaticInjected();
            injectorInstance.get().inject(target);

            target.check();

            wasCalled = true;
        }
    }

    private static class TestStaticInjected {
        @Inject
        private Instance<Object> objectnstance;

        public void check() {
            Assert.assertNotNull(objectnstance);
            Assert.assertNotNull(objectnstance.get());
        }
    }
}
