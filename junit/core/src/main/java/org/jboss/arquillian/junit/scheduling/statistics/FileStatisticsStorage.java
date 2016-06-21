package org.jboss.arquillian.junit.scheduling.statistics;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import org.jboss.arquillian.junit.scheduling.Statistics;

public class FileStatisticsStorage extends AbstractStatisticsStorage {
	public static final String FILENAME = "statistics.xml";
	public static final String DIR = "";
	
	private File statisticsFile;

	public FileStatisticsStorage() {
		statisticsFile = new File(FILENAME);
	}
	
	@Override
	public void store(Statistics stats) throws Exception {
		Writer fileWriter = new FileWriter(statisticsFile);
		
		// Marshal the statistics using jaxb
		super.marshal(stats, fileWriter);
		fileWriter.close();
	}

	@Override
	public Statistics retrieve() throws Exception {
		if(!statisticsFile.exists() || statisticsFile.length()  == 0){
			return null;
		}
		
		Reader fileReader = new FileReader(statisticsFile);
		// Unmarshal the stored statistics
		Statistics statistics = super.unmarshal(fileReader);
		fileReader.close();
		
		return statistics;
	}

}
