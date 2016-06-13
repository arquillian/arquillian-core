package org.jboss.arquillian.junit.scheduling;

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
	
	public int getPasses() {
		return passed;
	}

	public int getFailures() {
		return failures;
	}

}