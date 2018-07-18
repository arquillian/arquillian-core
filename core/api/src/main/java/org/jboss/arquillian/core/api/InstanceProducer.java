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
package org.jboss.arquillian.core.api;

/**
 * An instance producer.
 * <p>
 * This is the only mechanism for providing instances to be injected.
 * {@link org.jboss.arquillian.core.api.annotation.Inject} annotated instance producer fields must also declare a
 * {@link org.jboss.arquillian.core.api.annotation.Scope} annotation, to indicate which context the instance will be
 * produced for.
 * <p>
 * Typically, instances will be provided to the {@link #set(Object)} method during an appropriate lifecycle event.
 * For example, application scoped instances may be set in an observer of the
 * {@link org.jboss.arquillian.core.api.event.ManagerStarted} event. Remote loadable extensions for example may decide
 * to register application scoped instances in an observer of the {@code BeforeSuite} event.
 * <p>
 * <pre>
 * {@code @Inject @ApplicationScoped
 * private InstanceProducer<MyObject> myObjectInst;
 *
 * public void listen(@Observes OtherEvent otherEvent) {
 *     // do something..
 *     myObjectInst.set(new MyObject());
 *     // do something..
 * }
 * }
 * </pre>
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public interface InstanceProducer<T> extends Instance<T> {
    void set(T value);
}
