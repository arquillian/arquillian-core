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
package org.jboss.arquillian.junit;

import java.util.Comparator;
import org.junit.runners.model.FrameworkMethod;

/**
 * InOrderSorter
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
class InSequenceSorter implements Comparator<FrameworkMethod> {
    @Override
    public int compare(FrameworkMethod o1, FrameworkMethod o2) {
        int i1Order = 0;
        int i2Order = 0;
        InSequence i1 = o1.getAnnotation(InSequence.class);
        if (i1 != null) {
            i1Order = i1.value();
        }
        InSequence i2 = o2.getAnnotation(InSequence.class);
        if (i2 != null) {
            i2Order = i2.value();
        }
        return (i1Order < i2Order ? -1 : (i1Order == i2Order ? 0 : 1));
    }
}
