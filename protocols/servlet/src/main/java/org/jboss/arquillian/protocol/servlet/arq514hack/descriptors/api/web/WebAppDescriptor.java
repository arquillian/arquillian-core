package org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web;

import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * DSL Grammar to construct / alter Web Application XML Descriptors
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public interface WebAppDescriptor extends Descriptor, WebAppDescriptorReader
{

   WebAppDescriptor version(String version);

   WebAppDescriptor displayName(String displayName);
   
   ServletDef servlet(Class<? extends javax.servlet.Servlet> clazz, String... urlPatterns);

   ServletDef servlet(String clazz, String... urlPatterns);

   ServletDef servlet(String name, Class<? extends javax.servlet.Servlet> clazz, String[] urlPatterns);

   ServletDef servlet(String name, String clazz, String[] urlPatterns);
}