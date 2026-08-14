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
package org.jboss.arquillian.integration.test.common.lifecycle;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Indicates whether a lifecycle step executes on the client (test runner JVM)
 * or on the server (inside the container). Used by {@link TraceStep} to
 * declare expected execution location, and detected at runtime via a JNDI lookup.
 */
public enum RunsWhere {
    CLIENT,
    SERVER;

    public static boolean isRunningOnServer() {
        return getCurrentRunningLocation() == SERVER;
    }

    public static boolean isRunningOnClient() {
        return getCurrentRunningLocation() == CLIENT;
    }

    public static RunsWhere getCurrentRunningLocation() {
        try {
            new InitialContext().lookup("java:comp/env");
            return SERVER;
        } catch (NamingException ex) {
            return CLIENT;
        }
    }
}
