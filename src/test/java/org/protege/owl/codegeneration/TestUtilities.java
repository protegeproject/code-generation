package org.protege.owl.codegeneration;

import org.testng.Assert;

public class TestUtilities {

	private TestUtilities() { }
	
	public static void assertMethodNotFound(Class c, String method, Class...arguments) {
		boolean success = false;
		try {
			c.getMethod(method, arguments);
			success = true;
		}
		catch (NoSuchMethodException nsme) {
			success = false;
		}
		Assert.assertFalse(success);
	}
}
