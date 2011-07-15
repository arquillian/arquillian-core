package org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web;

import java.util.EventListener;

import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * DSL Grammar to construct / alter Web Application XML Descriptors
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public interface WebAppDescriptor extends Descriptor, WebAppDescriptorReader
{

   WebAppDescriptor version(String version);

   WebAppDescriptor metadataComplete(boolean value);

   WebAppDescriptor moduleName(String name);

   WebAppDescriptor description(String description);

   WebAppDescriptor displayName(String displayName);

   WebAppDescriptor distributable();

   WebAppDescriptor contextParam(String name, Object value);


   /**
    * Set the suffixes for Faces jsp files (default .jsp)
    */
   WebAppDescriptor facesDefaultSuffixes(String... suffix);

   /**
    * Set the suffixes for Facelet files (default .xhtml)
    */
   WebAppDescriptor faceletsDefaultSuffixes(String... suffix);
   
   /**
    * Set the list of Facelet files that don't use the default facelets suffix. 
    * <p>Mappings may be in the form of:<br>
    * <ul>
    *  <li>*.suffix</li>
    *  <li>/path/to/facelet.file</li>
    *  <li>/prefix/*</li>
    * </ul>
    */
   WebAppDescriptor faceletsViewMappings(String... mappings);

   WebAppDescriptor facesProjectStage(FacesProjectStage stage);

   WebAppDescriptor facesStateSavingMethod(FacesStateSavingMethod method);

   WebAppDescriptor facesConfigFiles(String... paths);

   WebAppDescriptor listener(Class<? extends EventListener> clazz);

   WebAppDescriptor listener(String clazz);

   FilterDef filter(Class<? extends javax.servlet.Filter> clazz, String... urlPatterns);

   FilterDef filter(String clazz, String... urlPatterns);

   FilterDef filter(String name, Class<? extends javax.servlet.Filter> clazz, String[] urlPatterns);

   FilterDef filter(String name, String clazz, String[] urlPatterns);

   ServletDef servlet(Class<? extends javax.servlet.Servlet> clazz, String... urlPatterns);

   ServletDef servlet(String clazz, String... urlPatterns);

   ServletDef servlet(String name, Class<? extends javax.servlet.Servlet> clazz, String[] urlPatterns);

   ServletDef servlet(String name, String clazz, String[] urlPatterns);

   WebAppDescriptor facesServlet();

   WebAppDescriptor welcomeFiles(String... servletPaths);

   WebAppDescriptor welcomeFile(String servletPath);

   WebAppDescriptor sessionTimeout(int timeout);

   WebAppDescriptor sessionTrackingModes(TrackingModeType... sessionTrackingModes);

   CookieConfigDef sessionCookieConfig();

   WebAppDescriptor errorPage(int errorCode, String location);

   WebAppDescriptor errorPage(String exceptionClass, String location);

   WebAppDescriptor errorPage(Class<? extends Throwable> exceptionClass, String location);

   WebAppDescriptor loginConfig(AuthMethodType authMethod, String realmName);

   WebAppDescriptor loginConfig(String authMethod, String realmName);

   WebAppDescriptor formLoginConfig(String loginPage, String errorPage);

   SecurityConstraintDef securityConstraint();

   SecurityConstraintDef securityConstraint(String displayName);

   WebAppDescriptor securityRole(String roleName);

   WebAppDescriptor securityRole(String roleName, String description);

   WebAppDescriptor absoluteOrdering(boolean others, String... names);

   WebAppDescriptor absoluteOrdering(String... names);
}