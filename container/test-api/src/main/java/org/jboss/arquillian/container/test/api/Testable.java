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
package org.jboss.arquillian.container.test.api;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;

/**
 * When testing an ear deployment containing multiple wars using the servlet protocol
 * this class allows to define the archive under test. This means that the test is running
 * in the context of this web module.
 *
 * @author robert.panzer
 * @version $Revision: $
 */
public final class Testable {

    public static final ArchivePath MARKER_FILE_PATH = ArchivePaths.create("META-INF/arquillian.ArchiveUnderTest");

    private Testable() {
    }

    /**
     * Mark the given archive as the archive under test so that the test are running in its context when using the Servlet
     * protocol.
     * <p>
     * <p>Usage Example:<br/>
     * <code><pre>
     * &#64;Deployment
     * public static EnterpriseArchive create() {
     *    EnterpriseArchive earArchive = ...
     *    WebArchive warArchive = ...
     *    earArchive.addAsModule( Testable.archiveToTest(warArchive) );
     *    return earArchive;
     * }
     * </pre></code>
     */
    public static <T extends Archive<T>> T archiveToTest(T archive) {
        return archive.add(EmptyAsset.INSTANCE, MARKER_FILE_PATH);
    }

    public static <T extends Archive<T>> boolean isArchiveToTest(T archive) {
        return archive.contains(MARKER_FILE_PATH);
    }
}
