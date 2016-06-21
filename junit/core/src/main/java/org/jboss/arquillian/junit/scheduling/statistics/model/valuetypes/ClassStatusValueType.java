package org.jboss.arquillian.junit.scheduling.statistics.model.valuetypes;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
