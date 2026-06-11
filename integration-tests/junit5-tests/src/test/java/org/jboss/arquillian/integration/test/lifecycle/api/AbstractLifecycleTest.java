/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.arquillian.integration.test.lifecycle.api;

import org.jboss.arquillian.integration.test.common.lifecycle.RunsWhere;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceFileManager;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceStep;
import org.jboss.arquillian.integration.test.common.lifecycle.TraceValidator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public abstract class AbstractLifecycleTest {

    protected static JavaArchive createBaseDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(AbstractLifecycleTest.class, FileWriterExtension.class,
                        ArquillianIntegrationTest.class, TraceStep.class, RunsWhere.class,
                        TraceFileManager.class, TraceValidator.class)
                .addAsResource(new StringAsset(TraceFileManager.getTmpFilePath().toString()),
                        TraceFileManager.TMP_FILE_ASSET_NAME);
    }
}
