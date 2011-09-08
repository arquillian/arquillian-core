package org.jboss.arquillian.config.descriptor.impl;

import org.jboss.shrinkwrap.descriptor.spi.Node;

public class NodePropertiesReplacer
{
   public static void walkAndReplace(Node node)
   {
      // check the own attributes for system properties replacement
      for (String key: node.getAttributes().keySet())
      {
         String attrValue = node.getAttribute(key);
         node.attribute(key, StringPropertyReplacer.replaceProperties(attrValue));
      }
      
      // walk through all child nodes
      for (Node child: node.getChildren())
      {
         for (String key: child.getAttributes().keySet())
         {
            String attrValue = child.getAttribute(key);
            child.attribute(key, StringPropertyReplacer.replaceProperties(attrValue));
         }
         
         String textValue = child.getText();
         if (textValue != null && !textValue.isEmpty())
         {
            child.text(StringPropertyReplacer.replaceProperties(textValue));
         }
      }      
   }

}
