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
package org.jboss.arquillian.protocol.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandService;

/**
 * JMXCommandService
 * 
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JMXCommandService implements CommandService {
    private static long TIMEOUT = 30000;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T execute(Command<T> command) {
        MBeanServer server = JMXTestRunner.localMBeanServer;
        try {
            ObjectName runner = new ObjectName(JMXTestRunner.OBJECT_NAME);
            server.invoke(runner, "send", new Object[] { command }, new String[] { Command.class.getName() });

            long timeoutTime = System.currentTimeMillis() + TIMEOUT;
            while (timeoutTime > System.currentTimeMillis()) {
                Command<?> newCommand = (Command<?>) server.invoke(runner, "receive", new Object[] {}, new String[] {});
                if (newCommand != null && newCommand.getResult() != null) {
                    return (T) newCommand.getResult();
                }
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            throw new RuntimeException("No command response within timeout of " + TIMEOUT + " ms.");
        } catch (Exception e) {
            throw new RuntimeException("Could not communicate with client side", e);
        }
    }

}
