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
package org.jboss.arquillian.test.impl.enricher.resource;

import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.test.AbstractTestTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.VarargMatcher;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * ArquillianTestEnricherTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Vineet Reynolds
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ArquillianResourceTestEnricherTestCase extends AbstractTestTestBase {

    private static Logger log = Logger.getLogger(ArquillianResourceTestEnricher.class.getName());
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;

    @Inject
    private Instance<Injector> injector;

    @Mock
    private ServiceLoader serviceLoader;

    @Mock
    private ResourceProvider resourceProvider;

    @Mock
    private ResourceProvider resourceProvider1;

    @Mock
    private Object resource;

    private List<ResourceProvider> resourceProviders = new ArrayList<ResourceProvider>();

    @Before
    public void addServiceLoaderAndLogCapturer() throws Exception {
        resourceProviders.add(resourceProvider);
        Mockito.when(serviceLoader.all(ResourceProvider.class)).thenReturn(resourceProviders);
        Mockito.when(resourceProvider.canProvide(Object.class)).thenReturn(true);
        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        attachLogCapturer();
    }

    @After
    public void detachLogCapturer() {
        log.removeHandler(customLogHandler);
        customLogHandler = null;
        try {
            logCapturingStream.close();
        } catch (IOException e) {
            throw new IllegalStateException("Potential memory leak as log capturing stream could not be closed");
        }
        logCapturingStream = null;
    }

    private void attachLogCapturer() {
        logCapturingStream = new ByteArrayOutputStream();
        Handler[] handlers = log.getParent().getHandlers();
        customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
        log.addHandler(customLogHandler);
    }

    private String getTestCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

    @Test
    public void shouldBeAbleToInjectBaseContext() throws Exception {
        Mockito.when(resourceProvider.lookup(
            (ArquillianResource) Mockito.any(),
            Mockito.argThat(new ClassInjectionAnnotationMatcher())))
            .thenReturn(resource);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        ObjectClass test = new ObjectClass();
        enricher.enrich(test);

        Assert.assertEquals(resource, test.resource);
    }

    @Test
    public void shouldBeAbleToInjectBaseContextOnMethod() throws Exception {
        Method resourceMethod = ObjectClass.class.getMethod("test", Object.class);

        Mockito.when(
            resourceProvider.lookup(
                (ArquillianResource) Mockito.any(),
                Mockito.argThat(new MethodInjectionAnnotationMatcher())))
            .thenReturn(resource);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        Object[] result = enricher.resolve(resourceMethod);

        Assert.assertEquals(resource, result[0]);
    }

    @Test
    public void shouldBeAbleToInjectBaseContextWithQualifier() throws Exception {
        Field resource2Field = ObjectClass2.class.getField("resource2");

        Mockito.when(
            resourceProvider.lookup(
                (ArquillianResource) Mockito.any(),
                Mockito.argThat(
                    new CustomAnnotationMatcher(
                        resource2Field.getAnnotation(ArquillianTestQualifier.class),
                        ResourceProvider.ClassInjection.class))))
            .thenReturn(resource);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        ObjectClass2 test = new ObjectClass2();
        enricher.enrich(test);

        Assert.assertEquals(resource, test.resource2);
    }

    @Test
    public void shouldBeAbleToInjectBaseContextWithNonNullLookupFromOneProviderOutOfMultipleResourceProviders() throws Exception {
        resourceProviders.add(resourceProvider1);
        Mockito.when(resourceProvider1.canProvide(Object.class)).thenReturn(true);

        Mockito.when(resourceProvider.lookup(
            (ArquillianResource) Mockito.any(),
            Mockito.argThat(new ClassInjectionAnnotationMatcher())))
            .thenReturn(null);

        Mockito.when(resourceProvider1.lookup(
            (ArquillianResource) Mockito.any(),
            Mockito.argThat(new ClassInjectionAnnotationMatcher())))
            .thenReturn(resource);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        ObjectClass test = new ObjectClass();
        enricher.enrich(test);

        Assert.assertEquals(resource, test.resource);
        Assert.assertTrue(getTestCapturedLog().contains("Provider for type class java.lang.Object returned a null value: resourceProvider"));
    }

    @Test
    public void shouldBeAbleToThrowExceptionWithMultipleResourceProvidersWhichProvidesNullLookUp() throws Exception {
        resourceProviders.add(resourceProvider1);
        Mockito.when(resourceProvider1.canProvide(Object.class)).thenReturn(true);

        Mockito.when(resourceProvider.lookup(
            (ArquillianResource) Mockito.any(),
            Mockito.argThat(new ClassInjectionAnnotationMatcher())))
            .thenReturn(null);

        Mockito.when(resourceProvider1.lookup(
            (ArquillianResource) Mockito.any(),
            Mockito.argThat(new ClassInjectionAnnotationMatcher())))
            .thenReturn(null);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        ObjectClass test = new ObjectClass();
        Throwable cause = null;
        try {
            enricher.enrich(test);
        } catch (RuntimeException ex) {
            cause = ex.getCause();
        }

        Assert.assertEquals(RuntimeException.class, cause.getClass());

        final String capturedLog = getTestCapturedLog();

        Assert.assertTrue(capturedLog.contains("WARNING: Provider for type class java.lang.Object returned a null value: resourceProvider"));
        Assert.assertTrue(capturedLog.contains("WARNING: Provider for type class java.lang.Object returned a null value: resourceProvider1"));
    }

    @Test
    public void shouldBeAbleToInjectBaseContextOnMethodWithQualifier() throws Exception {
        Method resourceMethod = ObjectClass.class.getMethod("testWithQualifier", Object.class);

        Mockito.when(
            resourceProvider.lookup(
                (ArquillianResource) Mockito.any(),
                Mockito.argThat(
                    new CustomAnnotationMatcher(
                        resourceMethod.getParameterAnnotations()[0][1],
                        ResourceProvider.MethodInjection.class))))
            .thenReturn(resource);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        Object[] result = enricher.resolve(resourceMethod);

        Assert.assertEquals(resource, result[0]);
    }

    @Test
    public void shouldThrowExceptionWhenUsedMethodScopeInjectionOnArquillianResource() throws Exception {
        Method resourceMethod = ObjectClass3.class.getMethod("testWithInjectionQualifier", Object.class);

        Mockito.when(
            resourceProvider.lookup(
                (ArquillianResource) Mockito.any(),
                Mockito.argThat(
                    new CustomAnnotationMatcher(
                        resourceMethod.getParameterAnnotations()[0][1],
                        ResourceProvider.MethodInjection.class))))
            .thenReturn(resource);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        Throwable cause = null;

        try {
            enricher.resolve(resourceMethod);
        } catch (Exception ex) {
            cause = ex;
        }

        Assert.assertEquals(IllegalStateException.class, cause.getClass());
    }

    @Test
    public void shouldThrowExceptionWhenUsedClassScopeInjectionOnArquillianResource() {

        Mockito.when(
            resourceProvider.lookup(
                (ArquillianResource) Mockito.any(),
                Mockito.argThat(
                    new ClassInjectionAnnotationMatcher())))
            .thenReturn(resource);

        TestEnricher enricher = new ArquillianResourceTestEnricher();
        injector.get().inject(enricher);

        ObjectClass3 test = new ObjectClass3();

        Throwable cause = null;

        try {
            enricher.enrich(test);
        } catch (RuntimeException ex) {
            cause = ex.getCause();
        }

        Assert.assertEquals(IllegalStateException.class, cause.getClass());
    }

    @Retention(RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public @interface ArquillianTestQualifier {
    }

    public class ObjectClass {
        @ArquillianResource
        public Object resource;

        public void test(@ArquillianResource Object resource) {
        }

        public void testWithQualifier(@ArquillianResource @ArquillianTestQualifier Object resource) {
        }
    }

    public class ObjectClass2 {
        @ArquillianResource
        @ArquillianTestQualifier
        public Object resource2;
    }

    public class ObjectClass3 {
        @ArquillianResource
        @ResourceProvider.ClassInjection
        public Object resource;

        public void testWithInjectionQualifier(@ArquillianResource @ResourceProvider.MethodInjection Object resource) {
        }
    }

    private class ClassInjectionAnnotationMatcher extends ArgumentMatcher<Annotation[]> implements VarargMatcher {
        private static final long serialVersionUID = 7468313136186740343L;

        @Override
        public boolean matches(Object varargArgument) {
            Class<?> qualifier = ((Annotation) varargArgument).annotationType();

            return qualifier.equals(ResourceProvider.ClassInjection.class);
        }
    }

    private class MethodInjectionAnnotationMatcher extends ArgumentMatcher<Annotation[]> implements VarargMatcher {
        private static final long serialVersionUID = 7468313136186740343L;

        @Override
        public boolean matches(Object varargArgument) {
            Class<?> qualifier = ((Annotation) varargArgument).annotationType();

            return qualifier.equals(ResourceProvider.MethodInjection.class);
        }
    }

    private class CustomAnnotationMatcher extends ArgumentMatcher<Annotation[]> implements VarargMatcher {
        private static final long serialVersionUID = 7468313136186740343L;

        private Annotation additionalQualifier;

        private Class<? extends Annotation> injectionScope;

        public CustomAnnotationMatcher(Annotation additionalQualifier, Class<? extends Annotation> injectionScope) {
            this.additionalQualifier = additionalQualifier;
            this.injectionScope = injectionScope;
        }

        @Override
        public boolean matches(Object varargArgument) {
            Annotation[] annotations = (Annotation[]) varargArgument;

            return annotations.length == 2
                && annotations[0].annotationType().equals(additionalQualifier.annotationType())
                && annotations[1].annotationType().equals(injectionScope);
        }
    }
}
