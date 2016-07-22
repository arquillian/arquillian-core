package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import org.jboss.arquillian.junit.scheduling.Statistics;

public class FileStatisticsStorage extends AbstractStatisticsStorage {

	private File statisticsFile;
	
	public FileStatisticsStorage(String storagePath) throws Exception{
		
		if(storagePath.isEmpty()){
			throw new Exception("No storagePath is specified!");
		}
		
		File storagePathFile = new File(storagePath);
		statisticsFile = storagePathFile.getAbsoluteFile();
		
		if(!statisticsFile.exists()){
			File directoryFile = statisticsFile.getParentFile();
			if(directoryFile == null){
				throw new Exception("No parent directory found!");
			}
			
			directoryFile.mkdirs();
		}
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
