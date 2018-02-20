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
package org.jboss.arquillian.container.test.impl.client.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.arquillian.config.descriptor.impl.ContainerDefImpl;
import org.jboss.arquillian.container.impl.LocalContainerRegistry;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.container.test.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.container.test.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observer;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DeploymentGeneratorTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class DeploymentGeneratorTestCase extends AbstractContainerTestTestBase {
    public static final String PROTOCOL_NAME_1 = "TEST_DEFAULT_1";
    public static final String PROTOCOL_NAME_2 = "TEST_DEFAULT_2";
    public static final String CONTAINER_NAME_1 = "CONTAINER_NAME_1";
    public static final String CONTAINER_NAME_2 = "CONTAINER_NAME_2";
    @Inject
    private Instance<Injector> injectorInst;
    @Mock
    private ServiceLoader serviceLoader;
    private ContainerRegistry containerRegistry;
    private ProtocolRegistry protocolRegistry;
    @Mock
    @SuppressWarnings("rawtypes")
    private DeployableContainer deployableContainer;
    @Mock
    private DeploymentPackager packager;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(DeploymentGenerator.class);
    }

    @Before
    public void prepare() {
        Injector injector = injectorInst.get();

        final List<DeploymentScenarioGenerator> deploymentScenarioGenerators = new ArrayList<DeploymentScenarioGenerator>();
        deploymentScenarioGenerators.add(new AnnotationDeploymentScenarioGenerator());
        when(serviceLoader.all(DeploymentScenarioGenerator.class))
            .thenReturn(deploymentScenarioGenerators);
        when(serviceLoader.onlyOne(eq(DeployableContainer.class))).thenReturn(deployableContainer);
        when(deployableContainer.getDefaultProtocol()).thenReturn(new ProtocolDescription(PROTOCOL_NAME_1));

        when(serviceLoader.all(eq(AuxiliaryArchiveAppender.class)))
            .thenReturn(create(AuxiliaryArchiveAppender.class, injector.inject(new TestAuxiliaryArchiveAppender())));
        when(serviceLoader.all(eq(AuxiliaryArchiveProcessor.class)))
            .thenReturn(create(AuxiliaryArchiveProcessor.class, injector.inject(new TestAuxiliaryArchiveProcessor())));
        when(serviceLoader.all(eq(ApplicationArchiveProcessor.class)))
            .thenReturn(create(ApplicationArchiveProcessor.class, injector.inject(new TestApplicationArchiveAppender())));

        containerRegistry = new LocalContainerRegistry(injector);
        protocolRegistry = new ProtocolRegistry();

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ContainerRegistry.class, containerRegistry);
        bind(ApplicationScoped.class, ProtocolRegistry.class, protocolRegistry);
        bind(ApplicationScoped.class, CallMap.class, new CallMap());
    }

    @Test
    public void shouldUseDefaultDefinedProtocolIfFound() {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        addProtocol(PROTOCOL_NAME_1, true);

        fire(createEvent(DeploymentWithDefaults.class));

        verify(deployableContainer, times(0)).getDefaultProtocol();
    }

    @Test
    public void shouldAddAdditionalObserverClasses() {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        addProtocol(PROTOCOL_NAME_1, true);

        fire(createEvent(DeploymentWithObserver.class));

        DeploymentScenario scenario = getManager().resolve(DeploymentScenario.class);
        Archive<?> archive = scenario.deployments().get(0).getDescription().getArchive();
        verifyThatIsContainedInArchive(archive, DeploymentWithObserver.class);
        verifyThatIsContainedInArchive(archive, ObserverClass.class);
        verifyThatIsContainedInArchive(archive, SecondObserverClass.class);
    }

    private void verifyThatIsContainedInArchive(Archive<?> archive, Class<?> clazz) {
        String classPath = clazz.getName().replace(".", "/") + ".class";
        Assert.assertTrue(String.format("archive %s should contain the path %s", archive.toString(true), classPath),
            archive.contains(classPath));
    }

    @Test
    public void shouldUseContainerDefaultProtocolIfNonDefaultDefined() {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        addProtocol(PROTOCOL_NAME_1, false);
        addProtocol(PROTOCOL_NAME_2, false);

        fire(createEvent(DeploymentWithDefaults.class));

        verify(deployableContainer, times(1)).getDefaultProtocol();
        verifyScenario("_DEFAULT_");
    }

    @Test
    public void shouldCallPackagingSPIsOnTestableArchive() throws Exception {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        addProtocol(PROTOCOL_NAME_1, true);

        fire(createEvent(DeploymentWithDefaults.class));

        CallMap spi = getManager().resolve(CallMap.class);
        Assert.assertTrue(spi.wasCalled(ApplicationArchiveProcessor.class));
        Assert.assertTrue(spi.wasCalled(AuxiliaryArchiveAppender.class));
        Assert.assertTrue(spi.wasCalled(AuxiliaryArchiveProcessor.class));

        verifyScenario("_DEFAULT_");
    }

    @Test
    public void shouldNotCallPackagingSPIsOnNonTestableArchive() throws Exception {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        addProtocol(PROTOCOL_NAME_1, true);

        fire(createEvent(DeploymentNonTestableWithDefaults.class));

        CallMap spi = getManager().resolve(CallMap.class);
        Assert.assertFalse(spi.wasCalled(ApplicationArchiveProcessor.class));
        Assert.assertFalse(spi.wasCalled(AuxiliaryArchiveAppender.class));
        Assert.assertFalse(spi.wasCalled(AuxiliaryArchiveProcessor.class));

        verifyScenario("_DEFAULT_");
    }

    @Test
    public void shouldAllowNonManagedDeploymentOnCustomContainer() throws Exception {
        addContainer(CONTAINER_NAME_1).getContainerConfiguration().setMode("custom");
        fire(createEvent(DeploymentNonManagedWithCustomContainerReference.class));

        verifyScenario("DeploymentNonManagedWithCustomContainerReference");
    }

    @Test
    public void shouldAllowMultipleSameNamedArchiveDeploymentWithDifferentTargets() throws Exception {
        addContainer(CONTAINER_NAME_1).getContainerConfiguration().setMode("suite");
        addContainer(CONTAINER_NAME_2).getContainerConfiguration().setMode("suite");
        addProtocol(PROTOCOL_NAME_1, true);

        fire(createEvent(DeploymentMultipleSameNameArchiveDifferentTarget.class));

        verifyScenario("X", "Y");
    }

    @Test // ARQ-971
    @SuppressWarnings("unchecked")
    public void shouldFilterNullAuxiliaryArchiveAppenderResulsts() throws Exception {
        when(serviceLoader.all(eq(AuxiliaryArchiveAppender.class)))
            .thenReturn(
                create(AuxiliaryArchiveAppender.class, injectorInst.get().inject(new NullAuxiliaryArchiveAppender())));

        addContainer(CONTAINER_NAME_1);
        addProtocol(PROTOCOL_NAME_1, true);

        fire(createEvent(DeploymentWithDefaults.class));

        CallMap spi = getManager().resolve(CallMap.class);
        Assert.assertTrue(spi.wasCalled(AuxiliaryArchiveAppender.class));

        DeploymentScenario scenario = getManager().resolve(DeploymentScenario.class);
        Assert.assertEquals(1, scenario.deployments().size());

        ArgumentCaptor<TestDeployment> captor = ArgumentCaptor.forClass(TestDeployment.class);
        verify(packager).generateDeployment(captor.capture(), Mockito.any(Collection.class));

        Assert.assertEquals(0, captor.getValue().getAuxiliaryArchives().size());
    }

    @Test(expected = ValidationException.class)
    public void shouldThrowExceptionOnMissingContainerReference() throws Exception {
        try {
            fire(createEvent(DeploymentWithContainerReference.class));
        } catch (Exception e) {
            Assert.assertTrue("Validate correct error message",
                e.getMessage().contains("Please include at least 1 Deployable Container on your Classpath"));
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void shouldThrowExceptionOnWrongContainerReference() throws Exception {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        try {
            fire(createEvent(DeploymentWithContainerReference.class));
        } catch (Exception e) {
            Assert.assertTrue("Validate correct error message",
                e.getMessage().contains("does not match any found/configured Containers"));
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void shouldThrowExceptionOnMissingProtocolReference() throws Exception {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        try {
            fire(createEvent(DeploymentWithProtocolReference.class));
        } catch (Exception e) {
            Assert.assertTrue("Validate correct error message",
                e.getMessage().contains("not maching any defined Protocol"));
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMultipleNoNamedDeployments() throws Exception {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        try {
            fire(createEvent(DeploymentMultipleNoNamed.class));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnMultipleSameNamedArchiveDeployments() throws Exception {
        addContainer("test-contianer").getContainerConfiguration().setMode("suite");
        try {
            fire(createEvent(DeploymentMultipleSameNameArchive.class));
        } catch (Exception e) {
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void shouldThrowExceptionOnManagedDeploymentOnCustomContainer() throws Exception {
        addContainer(CONTAINER_NAME_1).getContainerConfiguration().setMode("custom");
        try {
            fire(createEvent(DeploymentManagedWithCustomContainerReference.class));
        } catch (Exception e) {
            Assert.assertTrue("Validate correct error message",
                e.getMessage().contains("This container is set to mode custom "));
            throw e;
        }
    }

    private void verifyScenario(String... names) {
        DeploymentScenario scenario = getManager().resolve(DeploymentScenario.class);
        Assert.assertEquals(names.length, scenario.deployments().size());

        for (int i = 0; i < names.length; i++) {
            contains(scenario.deployments(), names[i]);
        }
    }

    private void contains(Collection<org.jboss.arquillian.container.spi.client.deployment.Deployment> deployments,
        String name) {
        if (deployments == null || deployments.size() == 0) {
            Assert.fail("No deployment by name " + name + " found in scenario. Scenario is empty");
        }
        for (org.jboss.arquillian.container.spi.client.deployment.Deployment deployment : deployments) {
            if (name.equals(deployment.getDescription().getName())) {
                return;
            }
        }
        Assert.fail("No deployment by name " + name + " found in scenario. " + deployments);
    }

    private Container addContainer(String name) {
        return containerRegistry.create(
            new ContainerDefImpl("arquillian.xml")
                .container(name),
            serviceLoader);
    }

    private ProtocolDefinition addProtocol(String name, boolean shouldBeDefault) {
        Protocol<?> protocol = mock(Protocol.class);
        when(protocol.getPackager()).thenReturn(packager);
        when(protocol.getDescription()).thenReturn(new ProtocolDescription(name));

        Map<String, String> config = Collections.emptyMap();
        return protocolRegistry.addProtocol(new ProtocolDefinition(protocol, config, shouldBeDefault))
            .getProtocol(new ProtocolDescription(name));
    }

    private <T> Collection<T> create(Class<T> type, T... instances) {
        List<T> list = new ArrayList<T>();
        Collections.addAll(list, instances);
        return list;
    }

    private GenerateDeployment createEvent(Class<?> testClass) {
        return new GenerateDeployment(new TestClass(testClass));
    }

    private static class DeploymentWithDefaults {
        @SuppressWarnings("unused")
        @Deployment
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class);
        }
    }

    @Observer({ObserverClass.class, SecondObserverClass.class})
    private static class DeploymentWithObserver {
        @SuppressWarnings("unused")
        @Deployment
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class);
        }
    }

    private static class ObserverClass {
    }

    private static class SecondObserverClass {
    }

    private static class DeploymentMultipleNoNamed {
        @SuppressWarnings("unused")
        @Deployment
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class);
        }

        @SuppressWarnings("unused")
        @Deployment
        public static JavaArchive deploy2() {
            return ShrinkWrap.create(JavaArchive.class);
        }
    }

    private static class DeploymentMultipleSameNameArchive {
        @SuppressWarnings("unused")
        @Deployment(name = "Y")
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class, "test.jar");
        }

        @SuppressWarnings("unused")
        @Deployment(name = "X")
        public static JavaArchive deploy2() {
            return ShrinkWrap.create(JavaArchive.class, "test.jar");
        }
    }

    private static class DeploymentMultipleSameNameArchiveDifferentTarget {
        @SuppressWarnings("unused")
        @Deployment(name = "Y")
        @TargetsContainer(CONTAINER_NAME_1)
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class, "test.jar");
        }

        @SuppressWarnings("unused")
        @Deployment(name = "X")
        @TargetsContainer(CONTAINER_NAME_2)
        public static JavaArchive deploy2() {
            return ShrinkWrap.create(JavaArchive.class, "test.jar");
        }
    }

    private static class DeploymentNonTestableWithDefaults {
        @SuppressWarnings("unused")
        @Deployment(testable = false)
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class);
        }
    }

    private static class DeploymentWithContainerReference {
        @SuppressWarnings("unused")
        @Deployment
        @TargetsContainer("DOES_NOT_EXIST")
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class);
        }
    }

    private static class DeploymentWithProtocolReference {
        @SuppressWarnings("unused")
        @Deployment
        @OverProtocol("DOES_NOT_EXIST")
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class);
        }
    }

    private static class DeploymentManagedWithCustomContainerReference {
        @SuppressWarnings("unused")
        @Deployment(managed = true, testable = false)
        @TargetsContainer(CONTAINER_NAME_1)
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class);
        }
    }

    private static class DeploymentNonManagedWithCustomContainerReference {
        @SuppressWarnings("unused")
        @Deployment(name = "DeploymentNonManagedWithCustomContainerReference", managed = false, testable = false)
        @TargetsContainer(CONTAINER_NAME_1)
        public static JavaArchive deploy() {
            return ShrinkWrap.create(JavaArchive.class);
        }
    }

    private static class CallMap {
        private Set<Class<?>> calls = new HashSet<Class<?>>();

        public void add(Class<?> called) {
            calls.add(called);
        }

        public boolean wasCalled(Class<?> called) {
            return calls.contains(called);
        }
    }

    private static class TestMaker {
        @Inject
        private Instance<CallMap> callmap;

        protected void called() {
            callmap.get().add(super.getClass().getInterfaces()[0]);
        }
    }

    private static class TestAuxiliaryArchiveAppender extends TestMaker implements AuxiliaryArchiveAppender {
        @Override
        public Archive<?> createAuxiliaryArchive() {
            called();
            return ShrinkWrap.create(JavaArchive.class, this.getClass().getSimpleName() + ".jar");
        }
    }

    private static class NullAuxiliaryArchiveAppender extends TestMaker implements AuxiliaryArchiveAppender {
        @Override
        public Archive<?> createAuxiliaryArchive() {
            called();
            return null;
        }
    }

    private static class TestAuxiliaryArchiveProcessor extends TestMaker implements AuxiliaryArchiveProcessor {
        @Override
        public void process(Archive<?> auxiliaryArchive) {
            called();
        }
    }

    private static class TestApplicationArchiveAppender extends TestMaker implements ApplicationArchiveProcessor {
        @Override
        public void process(Archive<?> applicationArchive, TestClass testClass) {
            called();
        }
    }
}
