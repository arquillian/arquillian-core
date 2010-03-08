package com.acme.ejb;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;

public @Stateless class TemperatureConverterBean
      implements TemperatureConverter {

   @Resource EJBContext ctx;

   @Override
   public double convertToCelcius(double f) {
      return ((f - 32) * 5 / 9);
   }

   @Override
   public double convertToFarenheight(double c) {
      return ((c * 9 / 5) + 32);
   }

   @Override
   public boolean isTransactional() {
      ctx.setRollbackOnly();
      return ctx.getRollbackOnly();
   }
}
