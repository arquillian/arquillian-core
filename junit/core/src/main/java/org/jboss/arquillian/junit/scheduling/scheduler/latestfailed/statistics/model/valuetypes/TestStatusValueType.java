package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.valuetypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A marshal friendly {@link org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.TestStatus} class. 
 * <p>
 * Contains the name of a marshaled atomic test and the tets's passes and failures.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestStatusValueType {
	
	@XmlAttribute
	private String name;
	
	private int passed;
	private int failures;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPassed() {
		return passed;
	}

	public void setPassed(int passed) {
		this.passed = passed;
	}

	public int getFailures() {
		return failures;
	}

	public void setFailures(int failures) {
		this.failures = failures;
	}
	
	
}
