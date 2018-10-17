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
package org.jboss.arquillian.core.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Inject.
 * <p>
 * This can be used to inject instances managed by an Arquillian context.
 * <p>
 * Fields annotated with this must be of type {@link org.jboss.arquillian.core.api.Instance} or
 * {@link org.jboss.arquillian.core.api.InstanceProducer}.
 * <p>
 * To provide an injected instance, it must be explicitly set on an injected
 * {@link org.jboss.arquillian.core.api.InstanceProducer}, and that field must also have a {@link Scope} annotated context
 * annotation on it, to indicate which context the instance is being produced for.
 * <p>
 * Note services provided by loadable extensions are not automatically available for injection. If an extension wishes to make
 * a service injected, it must observe an appropriate lifecycle event, look the service up from the {@code ServiceLoader}, and
 * provide the looked up service to a {@link org.jboss.arquillian.core.api.InstanceProducer} itself. Likewise, if an extension
 * wishes to make any other component available for injection, it should provide it to an instance producer that in an
 * appropriate lifecycle event observer.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {

}
