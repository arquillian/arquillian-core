package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.valuetypes;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A marshal friendly {@link org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.ClassStatus} class. 
 * <p>
 * Contains the name of a marshaled class and a
 * marshal friendly list of atomic tests mapped to that name.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassStatusValueType {
	@XmlAttribute
	private String name;
	
	@XmlElement(name="test")
	private List<TestStatusValueType> tests;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TestStatusValueType> getTests() {
		return tests;
	}

	public void setTests(List<TestStatusValueType> tests) {
		this.tests = tests;
	}
	
}
