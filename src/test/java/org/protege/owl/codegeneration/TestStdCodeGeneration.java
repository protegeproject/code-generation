package org.protege.owl.codegeneration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.protege.owl.codegeneration.std.testSimple02.A1;
import org.protege.owl.codegeneration.std.testSimple02.B1;
import org.protege.owl.codegeneration.std.testSimple02.IriA;
import org.protege.owl.codegeneration.std.testSimple02.IriB;
import org.protege.owl.codegeneration.std.testSimple02.MyFactory;
import org.testng.annotations.Test;

public class TestStdCodeGeneration {
	public static final String NS01 = TestInferredCodeGeneration.NS01;
	public static final String ONTOLOGY01 = TestInferredCodeGeneration.ONTOLOGY01;
	
	@Test
	public void testSimpleCreate() throws Exception {
		MyFactory factory = TestUtilities.openFactory(ONTOLOGY01, MyFactory.class, false);
		String newA1 = NS01 + "#aNewA1";
		String newB1 = NS01 + "#aNewB1";
		String b1Value = "hello";
		A1 a1 = factory.createA1(newA1);
		assertEquals(factory.getA1(newA1), a1);
		B1 b1 = factory.createB1(newB1);
		a1.addIriP(b1);
		b1.addIriQ(b1Value);
		
		assertEquals(a1.getIriP().size(), 1);
		assertTrue(a1.getIriP().contains(b1));
		assertTrue(a1.getIriQ().isEmpty());
		
		assertTrue(b1.getIriP().isEmpty());
		assertEquals(b1.getIriQ().size(), 1);
		assertTrue(b1.getIriQ().contains(b1Value));
	}
	
	@Test
	public void testSimpleGet() throws Exception {
		MyFactory factory = TestUtilities.openFactory(ONTOLOGY01, MyFactory.class, false);
		IriA x = factory.getIriA(NS01 + "#x");
		IriB y = factory.getIriB(NS01 + "#y");
		assertNotNull(x);
		assertEquals(x.getIriP().size(), 1);
		assertTrue(x.getIriP().contains(y));
	}
}
