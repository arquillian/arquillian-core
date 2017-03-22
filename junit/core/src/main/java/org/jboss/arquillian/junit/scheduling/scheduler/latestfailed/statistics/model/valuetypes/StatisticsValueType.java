package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.valuetypes;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A marshal friendly {@link org.jboss.arquillian.junit.scheduling.Statistics} class. 
 * <p>
 * Contains the statistics data for a test class.
 * 
 */
@XmlRootElement(name="statistics")
@XmlAccessorType(XmlAccessType.FIELD)
public class StatisticsValueType {
	
	@XmlElement(name="class")
	private List<ClassStatusValueType> classes;

	public List<ClassStatusValueType> getClasses() {
		return classes;
	}

	public void setClasses(List<ClassStatusValueType> classes) {
		this.classes = classes;
	}
}
