/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.junit.standalone;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.LifecycleEvent;

public class AllLifecycleEventExecutor {

    public void on(@Observes(precedence = -100) BeforeClass event) throws Throwable {
        execute(event);
    }

    public void on(@Observes(precedence = 100) AfterClass event) throws Throwable {
        execute(event);
    }

    public void on(@Observes(precedence = -100) BeforeTestLifecycleEvent event) throws Throwable {
        execute(event);
    }

    public void on(@Observes(precedence = 100) AfterTestLifecycleEvent event) throws Throwable {
        execute(event);
    }

    private void execute(LifecycleEvent event) throws Throwable {
        event.getExecutor().invoke();
    }
}
