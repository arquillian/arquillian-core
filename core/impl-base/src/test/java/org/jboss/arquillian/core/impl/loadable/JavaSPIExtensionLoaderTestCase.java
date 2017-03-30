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
package org.jboss.arquillian.core.impl.loadable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.jboss.arquillian.core.impl.loadable.util.FakeService;
import org.jboss.arquillian.core.impl.loadable.util.ShouldBeExcluded;
import org.jboss.arquillian.core.impl.loadable.util.ShouldBeIncluded;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * JavaSPIExtensionLoaderTestCase
 *
 * @author Davide D'Alto
 * @version $Revision: $
 */
public class JavaSPIExtensionLoaderTestCase {

    private static final String NEW_LINE = System.getProperty("line.separator");

    @Test
    public void shouldBeAbleToAddSelectedProvider() throws Exception {
        Collection<FakeService> all = new JavaSPIExtensionLoader().all(
            JavaSPIExtensionLoaderTestCase.class.getClassLoader(), FakeService.class);

        Assert.assertEquals("Unexpected number of provider loaded", 1, all.size());
        Assert.assertEquals("Wrong provider loaded", ShouldBeIncluded.class, all.iterator().next().getClass());
    }

    @Test
    public void shouldBeAbleToAddSelectedProviderFromClassLoader() throws Exception {
        Archive<JavaArchive> jarWithDefaultServiceImpl = createJarWithDefaultServiceImpl();
        Archive<JavaArchive> jarThatReplaceServiceImpl = createJarThatReplaceServiceImpl();

        ClassLoader emptyParent = null;
        ShrinkWrapClassLoader swClassloader =
            new ShrinkWrapClassLoader(emptyParent, jarThatReplaceServiceImpl, jarWithDefaultServiceImpl);

        ClassLoader emptyClassLoader = new ClassLoader(null) {
        };
        ClassLoader originalClassLoader = SecurityActions.getThreadContextClassLoader();

        Collection<?> providers = null;
        Class<?> expectedImplClass = null;
        try {
            Thread.currentThread().setContextClassLoader(emptyClassLoader);

            Class<?> serviceClass = swClassloader.loadClass("org.jboss.arquillian.core.impl.loadable.util.FakeService");
            expectedImplClass = swClassloader.loadClass("org.jboss.arquillian.core.impl.loadable.util.ShouldBeIncluded");

            providers = new JavaSPIExtensionLoader().all(swClassloader, serviceClass);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }

        Assert.assertEquals("Unexpected number of providers loaded", 1, providers.size());
        Assert.assertEquals("Wrong provider loaded", expectedImplClass, providers.iterator().next().getClass());
    }

    @Test
    public void shouldBeAbleToLoadVetoedClasses() throws Exception {
        Archive<JavaArchive> jarWithVetoedServiceImpl = createJarWithVetoedServices();

        ClassLoader emptyParent = null;
        ShrinkWrapClassLoader swClassloader = new ShrinkWrapClassLoader(emptyParent, jarWithVetoedServiceImpl);

        ClassLoader emptyClassLoader = new ClassLoader(null) {
        };
        ClassLoader originalClassLoader = SecurityActions.getThreadContextClassLoader();

        Map<Class<?>, Set<Class<?>>> vetoed = null;
        Class<?> service;
        try {
            Thread.currentThread().setContextClassLoader(emptyClassLoader);

            service = swClassloader.loadClass("org.jboss.arquillian.core.impl.loadable.util.FakeService");
            swClassloader.loadClass("org.jboss.arquillian.core.impl.loadable.util.ShouldBeIncluded");
            swClassloader.loadClass("org.jboss.arquillian.core.impl.loadable.util.ShouldBeExcluded");

            vetoed = new JavaSPIExtensionLoader().loadVetoed(swClassloader);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }

        Assert.assertEquals("Unexpected number of vetoed services", 1, vetoed.size());
        Assert.assertEquals("Unexpected number of vetoed services impl", 2, vetoed.get(service).size());
    }

    private Archive<JavaArchive> createJarWithVetoedServices() {
        StringAsset exclusions = new StringAsset(""
            +
            "org.jboss.arquillian.core.impl.loadable.util.FakeService=org.jboss.arquillian.core.impl.loadable.util.ShouldBeIncluded, "
            +
            "org.jboss.arquillian.core.impl.loadable.util.ShouldBeExcluded");

        Archive<JavaArchive> archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(FakeService.class, ShouldBeIncluded.class, ShouldBeExcluded.class)
            .addAsManifestResource(exclusions, "exclusions");

        return archive;
    }

    private Archive<JavaArchive> createJarWithDefaultServiceImpl() {
        Archive<JavaArchive> archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(FakeService.class, ShouldBeExcluded.class)
            .addAsServiceProvider(FakeService.class, ShouldBeExcluded.class);
        return archive;
    }

    private Archive<JavaArchive> createJarThatReplaceServiceImpl() {
        StringAsset serviceConfig = new StringAsset(
            "!org.jboss.arquillian.core.impl.loadable.util.ShouldBeExcluded"
                + NEW_LINE +
                "org.jboss.arquillian.core.impl.loadable.util.ShouldBeIncluded");

        Archive<JavaArchive> archive2 = ShrinkWrap.create(JavaArchive.class)
            .addClasses(ShouldBeIncluded.class)
            .addAsManifestResource(serviceConfig, "/services/org.jboss.arquillian.core.impl.loadable.util.FakeService");
        return archive2;
    }
}
