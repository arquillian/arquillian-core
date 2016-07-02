package org.jboss.arquillian.junit.scheduling.statistics.model;

//Keeps track of test failures and passes
public class TestStatus {

	private int passed;
	private int failures;

	public TestStatus() {
		passed = 0;
		failures = 0;
	}
	
	public void recordPass() {
		passed++;
	}
	
	public void removePass(){
		passed--;
	}
	
	public void recordFailure() {
		failures++;
	}
	
	public void removeFailure(){
		failures--;
	}
	
	public int getFailures() {
		return failures;
	}

	public int getPasses() {
		return passed;
	}

	public void setPassed(int passed) {
		this.passed = passed;
	}

	public void setFailures(int failures) {
		this.failures = failures;
	}
	
	

}