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
package org.jboss.arquillian.test.spi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Ignore;
import org.junit.Test;

/**
 * ObjectDeserialization
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ExceptionProxySerializationTestCase {

    @Test
    @Ignore // not ready for automation, uncomment ObjectInputStream override in ExceptionProxy.readExternal to run
    public void shouldBeAbleToDeserialize() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ObjectOutputStream out = new ObjectOutputStream(output);

        ExceptionProxy proxy;

        try {
            throw new RuntimeException("Test", new UnknownException(null));
        } catch (Exception e) {
            proxy = ExceptionProxy.createForException(e);
        }

        out.writeObject(proxy);
        out.flush();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        final URLClassLoader cl = new URLClassLoader(new URL[] {}) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (UnknownException.class.getName().equals(name)) {
                    return null;
                }
                if (UnknownObject.class.getName().equals(name)) {
                    throw new NoClassDefFoundError(name);
                }
                return super.loadClass(name, resolve);
            }
        };
        Thread.currentThread().setContextClassLoader(cl);

        ObjectInputStream in = new ObjectInputStream(input) {

            @Override
            protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
                return super.resolveProxyClass(interfaces);
            }

            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                return Class.forName(desc.getName(), false, cl);
            }

            @Override
            protected Object resolveObject(Object obj) throws IOException {
                return super.resolveObject(obj);
            }
        };

        ExceptionProxy readProxy = (ExceptionProxy) in.readObject();

        readProxy.createException().printStackTrace();
    }
}
