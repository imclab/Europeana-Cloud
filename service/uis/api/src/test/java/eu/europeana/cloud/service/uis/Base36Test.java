/* Base36Test.java - created on Jan 31, 2014, Copyright (c) 2013 Europeana Foundation, all rights reserved */
package eu.europeana.cloud.service.uis;

import org.junit.Assert;
import org.junit.Test;

import eu.europeana.cloud.service.uis.encoder.Base36;

/**
 * Base36 Unit tests
 * 
 * @author Yorgos Mamakis (Yorgos.Mamakis@ europeana.eu)
 * @since Jan 31, 2014
 */
public class Base36Test {

	/**
	 * Creates a new instance of this class.
	 */
	public Base36Test() {
		
	}
	
	/**
	 * Base36 unit test
	 */
	@Test
	public void testEncode(){
		String testStr = "123456789012345";
		String id1 = Base36.encode(testStr);
		String id2 = Base36.encode(testStr);
		Assert.assertEquals(id1.length(), 11);
		Assert.assertEquals(id2.length(), 11);
		Assert.assertNotSame("The generated ids where the same", id1 , id2);
	}
	
	/**
	 * Base36 time Encode
	 */
	@Test
	public void testTimeEncode(){
		String testStr = "123456789012345";
		String id1 = Base36.timeEncode(testStr);
		String id2 = Base36.timeEncode(testStr);
		Assert.assertEquals(id1.length(), 11);
		Assert.assertEquals(id2.length(), 11);
		Assert.assertNotSame("The generated ids where the same", id1 , id2);
	}

}
