package com.acme.ejb;

import javax.ejb.Local;
import javax.ejb.Stateless;

@Local(GreetingManager.class)
@Stateless
public class GreetingManagerBean implements GreetingManager
{
   @Override
   public String greet(String userName)
   {
      return "Hello " + userName;
   }
}
