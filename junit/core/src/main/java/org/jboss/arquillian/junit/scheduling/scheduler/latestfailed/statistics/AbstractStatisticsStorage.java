package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics;

import java.io.Reader;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.ModelTransitioner;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.valuetypes.StatisticsValueType;

public abstract class AbstractStatisticsStorage implements StatisticsStorage {
	private final ModelTransitioner modelTransitioner = new ModelTransitioner();
	
	public void marshal(Statistics stats, Writer writer) throws Exception {
		
		// Converts the common classes to the marshaling model classes
		StatisticsValueType statisticsValueType = modelTransitioner.toStatisticsValueType(stats);
		
		// Marshal the filled StatisticsValueType model
		JAXBContext jaxbContext = JAXBContext.newInstance(StatisticsValueType.class);
		
		Marshaller marshaler = jaxbContext.createMarshaller();
		marshaler.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaler.marshal(statisticsValueType, writer);
	}
	
	public Statistics unmarshal(Reader reader) throws Exception{
		
		// Unmarshal the the StatisticsValueType from the xml file
		JAXBContext jaxbContext = JAXBContext.newInstance(StatisticsValueType.class);
		
		Unmarshaller unmarshaler = jaxbContext.createUnmarshaller();
		StatisticsValueType statisticsValueType = (StatisticsValueType) unmarshaler.unmarshal(reader);
		
		return modelTransitioner.toStatistics(statisticsValueType);
	}
}
