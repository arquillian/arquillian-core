/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.container.test.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Config class enables users to use fluent API for creating a list of
 * properties which should be overridden in the existing arquillian
 * configuration. It holds a map of properties that can be retrieved via
 * {@link Config#map()} and the results should be passed e.g. to
 * {@link ContainerController#start(String, Map)} 
 * 
 * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
 * @version $Revision: $
 */
public class Config
{
   private Map<String, String> props;

   public Config()
   {
      this.props = new HashMap<String, String>();
   }

   public Map<String, String> getProperties()
   {
      return props;
   }

   public Config add(String name, String value)
   {
      props.put(name, value);
      return this;
   }

   public Map<String, String> map()
   {
      return props;
   }
}
