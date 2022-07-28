/*
 * Copyright 2022 Red Hat Inc. and/or its affiliates and other contributors
 * identified by the Git commit log. 
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
package org.jboss.arquillian.protocol.rest.runner;

import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandService;

public class RESTCommandService implements CommandService {
    private static long TIMEOUT = 30000;

    @SuppressWarnings("unchecked")
    public <T> T execute(Command<T> command) {
        String currentId = RESTTestRunner.currentCall.get();
        RESTTestRunner.events.put(currentId, command);

        long timeoutTime = System.currentTimeMillis() + TIMEOUT;
        while (timeoutTime > System.currentTimeMillis()) {
            Command<?> newCommand = RESTTestRunner.events.get(currentId);
            if (newCommand != null) {
                if (newCommand.getThrowable() != null) {
                    throw new RuntimeException(newCommand.getThrowable());
                }
                if (newCommand.getResult() != null) {
                    return (T) newCommand.getResult();
                }
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("No command response within timeout of " + TIMEOUT + " ms.");
    }
}
