/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
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
package org.jboss.arquillian.test.spi.execution;

import java.io.Serializable;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public final class ExecutionDecision implements Serializable {

    private static final long serialVersionUID = 1L;

    private Decision decision;

    private String reason;

    private ExecutionDecision() {
        this(null);
    }

    private ExecutionDecision(Decision decision) {
        this(decision, null);
    }

    private ExecutionDecision(Decision decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public static ExecutionDecision execute() {
        return new ExecutionDecision(Decision.EXECUTE);
    }

    public static ExecutionDecision execute(String reason) {
        return new ExecutionDecision(Decision.EXECUTE, reason);
    }

    public static ExecutionDecision dontExecute(String reason) {
        return new ExecutionDecision(Decision.DONT_EXECUTE, reason);
    }

    public Decision getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public enum Decision {
        EXECUTE,
        DONT_EXECUTE;
    }
}
