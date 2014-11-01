package org.protege.owl.codegeneration;

import org.protege.owl.codegeneration.inferred.generate03.A;
import org.protege.owl.codegeneration.inferred.generate03.Vocabulary;
import org.testng.Assert;
import org.testng.annotations.Test; 


public class TestIgnore {

	
	@Test
	public void testIgnore01() throws NoSuchFieldException, SecurityException {
		Assert.assertNotNull(Vocabulary.class.getField("OBJECT_PROPERTY_P"));
		try {
			Vocabulary.class.getField("OBJECT_PROPERTY_Q");
			Assert.fail("Property q should not appear in the vocabulary");
		}
		catch (NoSuchFieldException nsfe) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testIgnore02() throws NoSuchMethodException, SecurityException {
		Assert.assertNotNull(A.class.getMethod("hasP"));
		try {
			A.class.getMethod("hasQ");
			Assert.fail("Property q should not appear in the class declarations");
		}
		catch (NoSuchMethodException nsme) {
			Assert.assertTrue(true);
		}
	}
}
