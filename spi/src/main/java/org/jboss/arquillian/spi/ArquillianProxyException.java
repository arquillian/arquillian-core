package org.jboss.arquillian.spi;

public class ArquillianProxyException extends RuntimeException {

	private static final long serialVersionUID = -4703822636139101499L;

	public ArquillianProxyException() {
		super();
	}

	public ArquillianProxyException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArquillianProxyException(String message) {
		super(message);
	}

	public ArquillianProxyException(Throwable cause) {
		super(cause);
	}

	/**
	 * ArquillianProxyException constructor based on an underlying exception
	 * that cannot be recreated.
	 * 
	 * @param message
	 *            Message from the proxied Exception
	 * @param exceptionClassName
	 *            Class name of the proxied class type
	 * @param reason
	 *            reason that the exception couldn't be re-created
	 * @param cause
	 *            cause from the original exception
	 */
	public ArquillianProxyException(String message, String exceptionClassName,
			String reason, Throwable cause) {
		this(String.format("%s : %s [Proxied because : %s]", exceptionClassName,message, 
				reason),cause);
	}

}
