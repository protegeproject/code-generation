package org.protege.owl.codegeneration;

import static org.testng.Assert.assertNotNull;

import org.protege.owl.codegeneration.testSimple.IriA;
import org.protege.owl.codegeneration.testSimple.IriB;
import org.testng.annotations.Test;

public class TestCodeGeneration {

	@Test
	public void testPropertyByDomain() throws SecurityException, NoSuchMethodException {
		Class<IriA> interfaceA = IriA.class;
		Class<IriB> interfaceB = IriB.class;

		assertNotNull(interfaceA.getMethod("getIriP", new Class<?>[0]));
		TestUtilities.assertMethodNotFound(interfaceB, "getIriP", new Class<?>[0]);

		TestUtilities.assertMethodNotFound(interfaceA, "getIriQ", new Class<?>[0]);
		assertNotNull(interfaceB.getMethod("getIriQ", new Class<?>[0]));
	}
}
