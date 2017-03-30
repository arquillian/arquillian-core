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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Serializer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
final class Serializer {
    public static byte[] toByteArray(Object object) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream outObj = new ObjectOutputStream(out);
            outObj.writeObject(object);
            outObj.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize object: " + object, e);
        }
    }

    public static <T> T toObject(Class<T> type, byte[] objectArray) {
        try {
            ObjectInputStream outObj = new ObjectInputStream(new ByteArrayInputStream(objectArray));
            Object object = outObj.readObject();

            return type.cast(object);
        } catch (Exception e) {
            throw new RuntimeException("Could not deserialize object: " + objectArray, e);
        }
    }

    public static <T> T toObject(Class<T> type, InputStream input) {
        try {
            ObjectInputStream outObj = new ObjectInputStream(input);
            Object object = outObj.readObject();

            return type.cast(object);
        } catch (Exception e) {
            throw new RuntimeException("Could not deserialize object", e);
        } finally {
            try {
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
