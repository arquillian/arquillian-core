/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.arquillian.bundle;

// $Id$

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A basic {@link Response} implementation. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2009
 */
public class BasicResponse implements Response
{
   private static final long serialVersionUID = 1L;

   private List<Failure> failures = new ArrayList<Failure>();

   public List<Failure> getFailures()
   {
      return Collections.unmodifiableList(failures);
   }

   public void addFailure(Failure failure)
   {
      failures.add(failure);
   }

   @Override
   public String toString()
   {
      StringBuffer msgs = new StringBuffer();
      for (int i = 0; i < failures.size(); i++)
      {
         Failure f = failures.get(i);
         msgs.append((i > 0 ? "," : "") + f.getMessage());
      }
      return "BasicResponse[" + msgs + "]";
   }
}
