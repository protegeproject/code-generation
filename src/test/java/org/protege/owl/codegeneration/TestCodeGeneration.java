package org.protege.owl.codegeneration;

import org.protege.owl.codegeneration.testSimple.IriA;
import org.protege.owl.codegeneration.testSimple.IriB;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import org.testng.annotations.Test;

public class TestCodeGeneration {

	@Test
	public void testObjectPropertyByDomain() throws SecurityException, NoSuchMethodException {
		Class<IriA> interfaceA = IriA.class;
		Class<IriB> interfaceB = IriB.class;
		assertNotNull(interfaceA.getMethod("getIriP", new Class<?>[0]));
		boolean success = false;
		try {
			assertNull(interfaceB.getMethod("getIriP", new Class<?>[0]));
			success = true;
		}
		catch (Exception e) {
			
		}
		assertFalse(success);
	}
}
