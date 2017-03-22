package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed.statistics;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import org.jboss.arquillian.junit.scheduling.Statistics;

/**
 * File storage for test statistic information
 */
public class FileStatisticsStorage extends AbstractStatisticsStorage {

	private File statisticsFile;
	
	/**
	 * Constructs an object with the provided abstract path name.
	 * Note that all necessary and none existent directories will be created. 
	 * The text after the last '/' or on other operating systems '\\'
	 * is considered as the name of the statistics file. For example: 
	 * 
	 * <p>
	 *"storage/statistics.xml" creates a statistics.xml file in storage
	 *"storage/statistics" creates a file named statistics in storage
	 *"storage/statistics/" is also possible and again will create
	 * a file named statistics in storage
	 * 
	 * @param storagePath the abstract path name to a file
	 * @throws Exception - if <code> storagPath</code> is empty
	 * or <code>null</code> and if the file denoted by this abstract pathname
	 * doesn't have a parent.
	 */
	public FileStatisticsStorage(String storagePath) throws Exception{
		
		if(storagePath == null || storagePath.isEmpty()){
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
		if(!statisticsFile.exists() || statisticsFile.length() == 0){
			return null;
		}
		
		Reader fileReader = new FileReader(statisticsFile);
		
		// Unmarshal the stored statistics
		Statistics statistics = super.unmarshal(fileReader);
		fileReader.close();
		
		return statistics;
	}

}
