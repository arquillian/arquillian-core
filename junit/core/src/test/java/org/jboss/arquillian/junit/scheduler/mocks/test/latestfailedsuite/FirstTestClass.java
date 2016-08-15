package org.jboss.arquillian.junit.scheduler.mocks.test.latestfailedsuite;

import static org.junit.Assert.*;

import org.junit.Test;

public class FirstTestClass {

	@Test
	public void test1(){
		fail();
	}
	
	@Test
	public void test2(){
		fail();
	}
	
	@Test
	public void test3(){
		fail();
	}

}
