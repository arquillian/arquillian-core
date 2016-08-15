package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics;

import java.io.Reader;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.arquillian.junit.scheduling.Statistics;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.ModelTransitioner;
import org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics.model.valuetypes.StatisticsValueType;

/**
 * 
 * An abstraction for storing and retrieving test statistic data
 *
 */
public abstract class AbstractStatisticsStorage implements StatisticsStorage {
	
	/**
	 * A model transitioner.
	 * Used to transition from and to a jaxb marshal friendly model.
	 * 
	 */
	private final ModelTransitioner modelTransitioner = new ModelTransitioner();
	
	/**
	 * Marshals the test statistics from <code>stats</code> to the specified <code>Writer</code>.
	 * This method uses the <code>modelTransitioner</code> as a model
	 * transition is necessary in order to marshal the data properly.
	 * The data is stored in <code>JAXB_FORMATTED_OUTPUT</code>.
	 * 
	 * @param stats the test statistics to marshal
	 * @param writer the writer into which to marshal the statistics
	 * @throws Exception
	 * @see Statistics, StatisticsValueType, JAXBContext, Marshaller
	 */
	
	public void marshal(Statistics stats, Writer writer) throws Exception {
		
		// Converts the common classes to the marshaling model classes
		StatisticsValueType statisticsValueType = modelTransitioner.toStatisticsValueType(stats);
		
		// Marshal the filled StatisticsValueType model
		JAXBContext jaxbContext = JAXBContext.newInstance(StatisticsValueType.class);
		
		Marshaller marshaler = jaxbContext.createMarshaller();
		marshaler.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaler.marshal(statisticsValueType, writer);
	}
	
	/**
	 * Unmarshals test statistics from the specified <code>Reader</code>.
	 * This method uses the <code>modelTransitioner</code> in order to switch back to the original
	 * marshal unfriendly model.
	 * 
	 * @param reader a reader to read stored data
	 * @return the statistic data encapsulated in the original model
	 * @throws Exception
	 */
	public Statistics unmarshal(Reader reader) throws Exception{
		
		// Unmarshal the the StatisticsValueType from the xml file
		JAXBContext jaxbContext = JAXBContext.newInstance(StatisticsValueType.class);
		
		Unmarshaller unmarshaler = jaxbContext.createUnmarshaller();
		StatisticsValueType statisticsValueType = (StatisticsValueType) unmarshaler.unmarshal(reader);
		
		return modelTransitioner.toStatistics(statisticsValueType);
	}
}
