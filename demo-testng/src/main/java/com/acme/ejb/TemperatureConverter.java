package com.acme.ejb;

import javax.ejb.Local;

public @Local interface TemperatureConverter {
   double convertToCelcius(double f);
   double convertToFarenheight(double c);
   boolean isTransactional();
}
