/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.spi;

/**
 * Exception class used when a proxied exception cannot be created. This
 * exception type is is thrown instead and contains information about the
 * proxied class and a hint about why it could not be thrown.
 * 
 * @author <a href="mailto:contact@andygibson.net">Andy Gibson</a>
 * 
 */
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
