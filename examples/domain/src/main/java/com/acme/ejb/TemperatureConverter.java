package com.acme.ejb;

import javax.ejb.Local;

public @Local
interface TemperatureConverter
{
   double convertToCelsius(double f);

   double convertToFarenheit(double c);

   boolean isTransactional();
}
