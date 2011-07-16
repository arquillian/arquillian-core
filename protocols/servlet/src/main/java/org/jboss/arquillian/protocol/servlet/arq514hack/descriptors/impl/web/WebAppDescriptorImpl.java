/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.impl.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.application.ProjectStage;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.webapp.FacesServlet;

import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.SecurityRole;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.AuthMethodType;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.CookieConfigDef;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.ErrorPage;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.FacesProjectStage;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.FacesStateSavingMethod;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.FilterDef;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.FilterMappingDef;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.LoginConfig;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.SecurityConstraintDef;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.ServletDef;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.ServletMappingDef;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.TrackingModeType;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.web.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.spi.DescriptorExporter;
import org.jboss.shrinkwrap.descriptor.spi.Node;
import org.jboss.shrinkwrap.descriptor.spi.NodeProviderImplBase;
import org.jboss.shrinkwrap.descriptor.spi.xml.dom.XmlDomExporter;

/**
 * @author Dan Allen
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WebAppDescriptorImpl extends NodeProviderImplBase implements WebAppDescriptor
{
   // -------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * Node names
    */
   private static final String NODE_NAME_FILTER = "filter";
   private static final String NODE_NAME_FILTER_MAPPINGS = "filter-mapping";
   private static final String NODE_NAME_FILTER_NAME = "filter-name";
   private static final String NODE_NAME_SERVLET = "servlet";
   private static final String NODE_NAME_SERVLET_MAPPINGS = "servlet-mapping";
   private static final String NODE_NAME_SERVLET_NAME = "servlet-name";

   // -------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   private final Node model;

   // -------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   public WebAppDescriptorImpl(String descriptorName)
   {
      this(descriptorName, new Node("web-app")
            .attribute("xmlns", "http://java.sun.com/xml/ns/javaee")
            .attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            .attribute("xsi:schemaLocation",
                  "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"));
      version("3.0");
   }

   public WebAppDescriptorImpl(String descriptorName, Node model)
   {
      super(descriptorName);
      this.model = model;
   }

   // -------------------------------------------------------------------------------------||
   // API --------------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||
   
   @Override
   public WebAppDescriptor version(final String version)
   {
      if (version == null || version.length() == 0)
      {
         throw new IllegalArgumentException("Version must be specified");
      }
      model.attribute("xsi:schemaLocation",
            "http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_" + version.replace(".", "_")
                  + ".xsd");
      model.attribute("version", version);
      return this;
   }

   @Override
   public WebAppDescriptor metadataComplete(boolean value)
   {
      model.attribute("metadata-complete", value);
      return this;
   }

   @Override
   public WebAppDescriptor moduleName(String name)
   {
      model.getOrCreate("module-name").text(name);
      return this;
   }

   @Override
   public WebAppDescriptor description(String description)
   {
      model.create("description").text(description);
      return this;
   }

   @Override
   public WebAppDescriptor displayName(String displayName)
   {
      model.getOrCreate("display-name").text(displayName);
      return this;
   }

   @Override
   public WebAppDescriptor distributable()
   {
      model.getOrCreate("distributable");
      return this;
   }

   @Override
   public WebAppDescriptor contextParam(String name, Object value)
   {
      List<Node> params = model.get("context-param");
      
      Node param = null;
      for (Node node : params)
      {
         param = node.getSingle("param-name=" + name);
         if(param != null)
         {
            param.parent().getOrCreate("param-value").text(value);
            break;
         }
      }

      if(param == null)
      {
         param = model.create("context-param");
         param.create("param-name").text(name);
         param.create("param-value").text(value);
      }
      
      return this;
   }

   @Override
   public WebAppDescriptor facesProjectStage(FacesProjectStage stage)
   {
      return contextParam(ProjectStage.PROJECT_STAGE_PARAM_NAME, stage.getStage());
   }

   @Override
   public WebAppDescriptor facesStateSavingMethod(FacesStateSavingMethod method)
   {
      return contextParam(StateManager.STATE_SAVING_METHOD_PARAM_NAME, method.name());
   }

   @Override
   public WebAppDescriptor facesConfigFiles(String... paths)
   {
      if (paths == null || paths.length == 0)
      {
         return this;
      }
      return contextParam(FacesServlet.CONFIG_FILES_ATTR, Strings.join(Arrays.asList(paths), ","));
   }

   @Override
   public WebAppDescriptor listener(Class<? extends EventListener> clazz)
   {
      return listener(clazz.getName());
   }

   @Override
   public WebAppDescriptor listener(String clazz)
   {
      model.create("listener").create("listener-class").text(clazz);
      return this;
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor#getFilters()
    */
   @Override
   public List<FilterDef> getFilters()
   {
      final List<FilterDef> filters = new ArrayList<FilterDef>();
      for (final Node filterNode : model.get(NODE_NAME_FILTER))
      {
         final FilterDef filter = new FilterDefImpl(getDescriptorName(), model, filterNode);
         filters.add(filter);
      }
      return filters;
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor#getFilterMappings()
    */
   @Override
   public List<FilterMappingDef> getFilterMappings()
   {
      final List<FilterMappingDef> mappings = new ArrayList<FilterMappingDef>();
      for (final Node mappingNode : model.get(NODE_NAME_FILTER_MAPPINGS))
      {
         final String filterName = mappingNode.getSingle(NODE_NAME_FILTER_NAME).text();

         FilterDef filterDef = null;
         List<FilterDef> filters = getFilters();
         for (FilterDef filter : filters)
         {
            if (Strings.areEqualTrimmed(filter.getName(), filterName))
            {
               filterDef = filter;
            }
         }

         final FilterMappingDef filterMapping = new FilterMappingDefImpl(getDescriptorName(), getRootNode(),
                  ((FilterDefImpl) filterDef).getNode(), mappingNode);
         mappings.add(filterMapping);
      }
      return mappings;
   }

   @Override
   public FilterDef filter(Class<? extends javax.servlet.Filter> clazz, String... urlPatterns)
   {
      return filter(clazz.getSimpleName(), clazz.getName(), urlPatterns);
   }

   @Override
   public FilterDef filter(String clazz, String... urlPatterns)
   {
      return filter(getSimpleName(clazz), clazz, urlPatterns);
   }

   @Override
   public FilterDef filter(String name, Class<? extends javax.servlet.Filter> clazz, String[] urlPatterns)
   {
      return filter(name, clazz.getName(), urlPatterns);
   }

   @Override
   public FilterDef filter(String name, String clazz, String[] urlPatterns)
   {
      Node filter = model.create("filter");
      filter.create("filter-name").text(name);
      filter.create("filter-class").text(clazz);

      FilterDef f = new FilterDefImpl(getDescriptorName(), model, filter).mapping().urlPatterns(urlPatterns);
      return f;
   }

   @Override
   public ServletDef servlet(Class<? extends javax.servlet.Servlet> clazz, String... urlPatterns)
   {
      return servlet(clazz.getSimpleName(), clazz.getName(), urlPatterns);
   }

   @Override
   public ServletDef servlet(String clazz, String... urlPatterns)
   {
      return servlet(getSimpleName(clazz), clazz, urlPatterns);
   }

   @Override
   public ServletDef servlet(String name, Class<? extends javax.servlet.Servlet> clazz, String[] urlPatterns)
   {
      return servlet(name, clazz.getName(), urlPatterns);
   }

   @Override
   public ServletDef servlet(String name, String clazz, String[] urlPatterns)
   {
      Node servletNode = model.create("servlet");
      servletNode.create("servlet-name").text(name);
      servletNode.create("servlet-class").text(clazz);
      ServletDef servlet = new ServletDefImpl(getDescriptorName(), model, servletNode);

      servlet.mapping().urlPatterns(urlPatterns);
      return servlet;
   }

   @Override
   public WebAppDescriptor facesServlet()
   {
      return servlet(FacesServlet.class, "*.jsf");
   }

   @Override
   public WebAppDescriptor welcomeFiles(String... servletPaths)
   {
      for (String p : servletPaths)
      {
         model.getOrCreate("welcome-file-list").create("welcome-file").text(p);
      }
      return this;
   }

   @Override
   public WebAppDescriptor welcomeFile(String servletPath)
   {
      return welcomeFiles(servletPath);
   }

   @Override
   public WebAppDescriptor sessionTimeout(int timeout)
   {
      model.getOrCreate("session-config").getOrCreate("session-timeout").text(timeout);
      return this;
   }

   @Override
   public WebAppDescriptor sessionTrackingModes(TrackingModeType... sessionTrackingModes)
   {
      for (TrackingModeType m : sessionTrackingModes)
      {
         model.getOrCreate("session-config").create("tracking-mode").text(m.name());
      }
      return this;
   }

   @Override
   public CookieConfigDef sessionCookieConfig()
   {
      return new CookieConfigDefImpl(getDescriptorName(), model);
   }

   @Override
   public WebAppDescriptor errorPage(int errorCode, String location)
   {
      Node error = model.create("error-page");
      error.create("error-code").text(errorCode);
      error.create("location").text(location);
      return this;
   }

   @Override
   public WebAppDescriptor errorPage(String exceptionClass, String location)
   {
      Node error = model.create("error-page");
      error.create("exception-type").text(exceptionClass);
      error.create("location").text(location);

      return this;
   }

   @Override
   public WebAppDescriptor errorPage(Class<? extends Throwable> exceptionClass, String location)
   {
      return errorPage(exceptionClass.getName(), location);
   }

   @Override
   public WebAppDescriptor loginConfig(AuthMethodType authMethod, String realmName)
   {
      return loginConfig(authMethod.toString(), realmName);
   }

   @Override
   public WebAppDescriptor loginConfig(String authMethod, String realmName)
   {
      Node login = model.create("login-config");
      login.create("auth-method").text(authMethod);
      login.create("realm-name").text(realmName);

      return this;
   }

   @Override
   public WebAppDescriptor formLoginConfig(String loginPage, String errorPage)
   {
      Node login = model.create("login-config");
      login.create("auth-method").text(AuthMethodType.FORM);

      Node form = model.create("form-login-config");
      form.create("form-login-page").text(loginPage);
      form.create("form-error-page").text(errorPage);

      return this;
   }

   @Override
   public SecurityConstraintDef securityConstraint()
   {
      return securityConstraint(null);
   }

   @Override
   public SecurityConstraintDef securityConstraint(String displayName)
   {
      Node security = model.create("security-constraint");
      if (displayName != null)
      {
         security.create("display-name").text(displayName);
      }
      return new SecurityConstraintDefImpl(getDescriptorName(), model, security);
   }

   @Override
   public WebAppDescriptor securityRole(String roleName)
   {
      return securityRole(roleName, null);
   }

   @Override
   public WebAppDescriptor securityRole(String roleName, String description)
   {
      Node security = model.create("security-role");
      if (roleName != null)
      {
         security.create("role-name").text(roleName);
      }
      if (description != null)
      {
         security.create("description").text(description);
      }
      return this;
   }

   @Override
   public WebAppDescriptor absoluteOrdering(String... names)
   {
      return absoluteOrdering(false, names);
   }

   @Override
   public WebAppDescriptor absoluteOrdering(boolean others, String... names)
   {
      Node ordering = model.getOrCreate("absolute-ordering");
      if (names != null)
      {
         for (String name : names)
         {
            ordering.create("name").text(name);
         }
      }
      if (others)
      {
         ordering.getOrCreate("others");
      }
      return this;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.spi.NodeProvider#getRootNode()
    */
   @Override
   public Node getRootNode()
   {
      return model;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.shrinkwrap.descriptor.impl.base.NodeProviderImplBase#getExporter()
    */
   @Override
   protected DescriptorExporter getExporter()
   {
      return new XmlDomExporter();
   }

   // -------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /*
    * org.test.MyClass -> MyClass
    */
   private String getSimpleName(String fqcn)
   {
      if (fqcn.indexOf('.') >= 0)
      {
         return fqcn.substring(fqcn.lastIndexOf('.') + 1);
      }
      return fqcn;
   }

   @Override
   public String getVersion()
   {
      return model.attributes().get("version");
   }

   @Override
   public String getModuleName()
   {
      return model.attributes().get("module-name");
   }

   @Override
   public String getDescription()
   {
      return model.attributes().get("description");
   }

   @Override
   public String getDisplayName()
   {
      return model.attributes().get("display-name");
   }

   @Override
   public boolean isDistributable()
   {
      return model.attributes().get("distributable") != null;
   }

   @Override
   public boolean isMetadataComplete()
   {
      String complete = model.attributes().get("metadata-complete");
      if (complete == null)
      {
         complete = "";
      }
      return "true".equalsIgnoreCase(complete);
   }

   @Override
   public String getContextParam(String name)
   {
      Map<String, String> params = getContextParams();
      for (Entry<String, String> e : params.entrySet())
      {
         if (e.getKey() != null && e.getKey().equals(name))
         {
            return e.getValue();
         }
      }
      return null;
   }

   @Override
   public Map<String, String> getContextParams()
   {
      Map<String, String> result = new HashMap<String, String>();

      List<Node> params = model.get("context-param");
      for (Node p : params)
      {
         String name = p.textValue("param-name");
         String value = p.textValue("param-value");
         result.put(name, value);
      }

      return result;
   }

   @Override
   public List<String> getFaceletsDefaultSuffixes()
   {
      List<String> suffixes = new ArrayList<String>();
      
      String value = getContextParam(ViewHandler.FACELETS_SUFFIX_PARAM_NAME);
      if(value != null)
      {
         suffixes = Arrays.asList(value.split("\\s+"));
      }
      else
      {
         suffixes = Arrays.asList(ViewHandler.DEFAULT_FACELETS_SUFFIX.split("\\s+"));
      }
      
      return suffixes;
   }
   
   @Override
   public WebAppDescriptor faceletsDefaultSuffixes(String... suffixes)
   {
      contextParam(ViewHandler.FACELETS_SUFFIX_PARAM_NAME, Strings.join(Arrays.asList(suffixes), " "));
      
      return this;
   }

   @Override
   public List<String> getFacesDefaultSuffixes()
   {
      List<String> suffixes = new ArrayList<String>();
      
      String value = getContextParam(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
      if(value != null)
      {
         suffixes.addAll(Arrays.asList(value.split("\\s+")));
      }
      else
      {
         suffixes.addAll(Arrays.asList(ViewHandler.DEFAULT_SUFFIX.split("\\s+")));
      }
      
      return suffixes;
   }

   @Override
   public WebAppDescriptor facesDefaultSuffixes(String... suffixes)
   {
      contextParam(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME, Strings.join(Arrays.asList(suffixes), " "));
      return this;
   }

   @Override
   public List<String> getFaceletsViewMappings()
   {
      List<String> mappings = new ArrayList<String>();
      
      String value = getContextParam(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME);
      if(value != null)
      {
         mappings.addAll(Arrays.asList(value.split("\\s*;\\s*")));
      }
      
      return mappings;
   }

   @Override
   public WebAppDescriptor faceletsViewMappings(String... mappings)
   {
      contextParam(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME, Strings.join(Arrays.asList(mappings), ";"));
      return null;
   }

   @Override
   public FacesProjectStage getFacesProjectStage()
   {
      // JSF uses strict case-sensitive parsing, so we need to be rediculously sensitive here
      String value = getContextParam(ProjectStage.PROJECT_STAGE_PARAM_NAME);
      for (FacesProjectStage stage : FacesProjectStage.values())
      {
         if(stage.getStage().equals(value))
         {
            return stage;
         }
      }
      return FacesProjectStage.PRODUCTION;
   }

   @Override
   public FacesStateSavingMethod getFacesStateSavingMethod()
   {
      return FacesStateSavingMethod.valueOf(getContextParam(StateManager.STATE_SAVING_METHOD_PARAM_NAME));
   }

   @Override
   public List<String> getFacesConfigFiles()
   {
      String value = getContextParam(FacesServlet.CONFIG_FILES_ATTR);
      return value == null ? new ArrayList<String>() : Arrays.asList(value.split(","));
   }

   @Override
   public List<String> getListeners()
   {
      return model.textValues("listener/listener-class");
   }

   @Override
   public List<ServletDef> getServlets()
   {
      final List<ServletDef> servlets = new ArrayList<ServletDef>();
      for (final Node servlet : model.get(NODE_NAME_SERVLET))
      {
         final ServletDef filter = new ServletDefImpl(getDescriptorName(), model, servlet);
         servlets.add(filter);
      }
      return servlets;
   }

   @Override
   public List<ServletMappingDef> getServletMappings()
   {
      final List<ServletMappingDef> mappings = new ArrayList<ServletMappingDef>();
      for (final Node mappingNode : model.get(NODE_NAME_SERVLET_MAPPINGS))
      {
         final String servletName = mappingNode.getSingle(NODE_NAME_SERVLET_NAME).text();

         ServletDef servletDef = null;
         List<ServletDef> servlets = getServlets();
         for (ServletDef servlet : servlets)
         {
            if (Strings.areEqualTrimmed(servlet.getName(), servletName))
            {
               servletDef = servlet;
            }
         }

         final ServletMappingDef servletMapping = new ServletMappingDefImpl(getDescriptorName(), getRootNode(),
                  ((ServletDefImpl) servletDef).getNode(), mappingNode);
         mappings.add(servletMapping);
      }
      return mappings;
   }

   @Override
   public boolean hasFacesServlet()
   {
      List<Node> list = model.get("servlet/servlet-class=javax.faces.webapp.FacesServlet");
      return !list.isEmpty();
   }

   @Override
   public List<String> getWelcomeFiles()
   {
      List<String> results = new ArrayList<String>();
      List<Node> list = model.get("welcome-file-list/welcome-file");
      for (Node file : list)
      {
         results.add(file.text());
      }
      return results;
   }

   @Override
   public int getSessionTimeout()
   {
      Node single = model.getSingle("session-config/session-timeout");
      if(single != null)
      {
         try
         {
            return Integer.parseInt(single.text().trim());
         }
         catch (NumberFormatException e)
         {
            throw new RuntimeException("Unable to parse session-timeout from value ["+single.text().trim()+"]");
         }
      }
      return 0;
   }

   @Override
   public List<TrackingModeType> getSessionTrackingModes()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<ErrorPage> getErrorPages()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<LoginConfig> getLoginConfigs()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<SecurityConstraintDef> getSecurityConstraints()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<SecurityRole> getSecurityRoles()
   {
      // TODO Auto-generated method stub
      return null;
   }
}