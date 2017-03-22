package org.jboss.arquillian.junit.scheduling.scheduler.latestfailed;

/**
 * A storage class for <code>LatestFailedScheduler</code>'s parameters
 * 
 */
public class LatestFailedSchedulerParamValues {
	private String storageDir;
	private boolean storeLongTermFlag;
	
	public LatestFailedSchedulerParamValues(boolean storeLongTerm, String storageDir){
		this.storageDir = storageDir;
		storeLongTermFlag = storeLongTerm;
	}

	public String getStorageDir() {
		return storageDir;
	}

	public boolean isStoredLongTerm() {
		return storeLongTermFlag;
	}
}
