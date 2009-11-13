package org.jboss.arquillian.api;

/**
 * Thrown if an exception was expected, but non occurred
 * 
 * @author Pete Muir
 *
 */
public class ExpectedException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public ExpectedException()
   {
      super();
   }

   public ExpectedException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ExpectedException(String message)
   {
      super(message);
   }

   public ExpectedException(Throwable cause)
   {
      super(cause);
   }
   
   
   
}
