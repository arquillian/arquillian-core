/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.packager.osgi;

import java.util.Map;

import org.jboss.osgi.spi.util.BundleInfo;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.formatter.Formatter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

@SuppressWarnings("rawtypes")
public class BundleArchive implements Archive
{
   private Archive delegate;
   private BundleInfo bundleInfo;

   public BundleArchive(Archive<JavaArchive> archive, BundleInfo info)
   {
      this.delegate = archive;
      this.bundleInfo = info;
   }

   public BundleInfo getBundleInfo()
   {
      return bundleInfo;
   }

   public <TYPE extends Assignable> TYPE as(Class<TYPE> clazz)
   {
      return delegate.as(clazz);
   }

   public String getName()
   {
      return delegate.getName();
   }

   public Archive add(Asset asset, ArchivePath target) throws IllegalArgumentException
   {
      return delegate.add(asset, target);
   }

   public Archive add(Asset asset, ArchivePath target, String name) throws IllegalArgumentException
   {
      return delegate.add(asset, target, name);
   }

   public Archive add(Asset asset, String target) throws IllegalArgumentException
   {
      return delegate.add(asset, target);
   }

   public Archive addDirectory(String path) throws IllegalArgumentException
   {
      return delegate.addDirectory(path);
   }

   public Archive addDirectories(String... paths) throws IllegalArgumentException
   {
      return delegate.addDirectories(paths);
   }

   public Archive addDirectory(ArchivePath path) throws IllegalArgumentException
   {
      return delegate.addDirectory(path);
   }

   public Archive addDirectories(ArchivePath... paths) throws IllegalArgumentException
   {
      return delegate.addDirectories(paths);
   }

   public Node get(ArchivePath path) throws IllegalArgumentException
   {
      return delegate.get(path);
   }

   public Node get(String path) throws IllegalArgumentException
   {
      return delegate.get(path);
   }

   public boolean contains(ArchivePath path) throws IllegalArgumentException
   {
      return delegate.contains(path);
   }

   public boolean delete(ArchivePath path) throws IllegalArgumentException
   {
      return delegate.delete(path);
   }

   public Map getContent()
   {
      return delegate.getContent();
   }

   @SuppressWarnings("unchecked")
   public Map getContent(Filter filter)
   {
      return delegate.getContent(filter);
   }

   @SuppressWarnings("unchecked")
   public Archive add(Archive archive, ArchivePath path) throws IllegalArgumentException
   {
      return delegate.add(archive, path);
   }

   @SuppressWarnings("unchecked")
   public Archive merge(Archive source) throws IllegalArgumentException
   {
      return delegate.merge(source);
   }

   @SuppressWarnings("unchecked")
   public Archive merge(Archive source, Filter filter) throws IllegalArgumentException
   {
      return delegate.merge(source, filter);
   }

   @SuppressWarnings("unchecked")
   public Archive merge(Archive source, ArchivePath path) throws IllegalArgumentException
   {
      return delegate.merge(source, path);
   }

   @SuppressWarnings("unchecked")
   public Archive merge(Archive source, ArchivePath path, Filter filter) throws IllegalArgumentException
   {
      return delegate.merge(source, path, filter);
   }

   public String toString()
   {
      return delegate.toString();
   }

   public String toString(boolean verbose)
   {
      return delegate.toString(verbose);
   }

   public String toString(Formatter formatter) throws IllegalArgumentException
   {
      return delegate.toString(formatter);
   }
}