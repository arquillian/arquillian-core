package org.jboss.arquillian.container.test.impl.enricher.resource;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;
import org.jboss.arquillian.test.api.ArquillianResource;

public class RemoteResourceCommand extends AbstractCommand<Object> {

	private static final long serialVersionUID = 1L;

	private Class<?> type;
	private ArquillianResource resource;
	private Annotation[] annotations;

	public RemoteResourceCommand(Class<?> type, ArquillianResource resource, Annotation[] annotations) {
		this.type = type;
		this.resource = resource;
		this.annotations = annotations;
	}

	public Class<?> getType() {
		return type;
	}

	public ArquillianResource getResource() {
		return resource;
	}

	public Annotation[] getAnnotations() {
		return annotations;
	}
}
