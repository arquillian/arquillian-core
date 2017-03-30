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
package org.jboss.arquillian.container.test.impl.enricher.resource;

import javax.naming.Context;
import javax.naming.InitialContext;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * ArquillianTestEnricherTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class InitialContextProviderTestCase extends OperatesOnDeploymentAwareProviderBase {
    @Override
    protected ResourceProvider getResourceProvider() {
        return new InitialContextProvider();
    }

    @Test
    public void shouldBeAbleToInjectContext() throws Exception {
        Context context = new InitialContext();
        ContextClass test = execute(ContextClass.class, Context.class, context);

        Assert.assertEquals(context, test.context);
    }

    @Test
    public void shouldBeAbleToInjectContextQualified() throws Exception {
        Context contextZ = new InitialContext();
        Context contextX = new InitialContext();
        ContextClassQualifed test = execute(ContextClassQualifed.class, Context.class, contextZ, contextX);

        Assert.assertEquals(contextX, test.context);
    }

    @Test
    public void shouldBeAbleToInjectInitialContext() throws Exception {
        Context context = new InitialContext();
        InitialContextClass test = execute(InitialContextClass.class, Context.class, context);

        Assert.assertEquals(context, test.context);
    }

    @Test
    public void shouldBeAbleToInjectInitialContextContextQualified() throws Exception {
        Context contextZ = new InitialContext();
        Context contextX = new InitialContext();
        InitialContextClassQualifed test = execute(InitialContextClassQualifed.class, Context.class, contextZ, contextX);

        Assert.assertEquals(contextX, test.context);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMissingContainerRegistry() throws Exception {
        execute(
            false,
            true,
            InitialContextClassQualifed.class,
            Context.class,
            new InitialContext(),
            new InitialContext());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMissingDeploymentScenario() throws Exception {
        execute(
            true,
            false,
            InitialContextClassQualifed.class,
            Context.class,
            new InitialContext(),
            new InitialContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnUnKnownDeployment() throws Exception {
        execute(
            InitialContextClassQualifedMissing.class,
            Context.class,
            new InitialContext(),
            new InitialContext());
    }

    public static class ContextClass {
        @ArquillianResource
        public Context context;
    }

    public static class ContextClassQualifed {
        @ArquillianResource
        @OperateOnDeployment("X")
        public Context context;
    }

    public static class InitialContextClass {
        @ArquillianResource
        public InitialContext context;
    }

    public static class InitialContextClassQualifed {
        @ArquillianResource
        @OperateOnDeployment("X")
        public InitialContext context;
    }

    public static class InitialContextClassQualifedMissing {
        @ArquillianResource
        @OperateOnDeployment("MISSING")
        public InitialContext context;
    }
}
