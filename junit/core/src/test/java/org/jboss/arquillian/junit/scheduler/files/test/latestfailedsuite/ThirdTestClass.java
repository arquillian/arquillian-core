package org.jboss.arquillian.junit.scheduler.files.test.latestfailedsuite;

import static org.junit.Assert.*;

import org.junit.Test;

public class ThirdTestClass {
	@Test
	public void test6(){
	}
	
	@Test
	public void test7(){
		fail();
	}
	
	@Test
	public void test8(){
	}

}
