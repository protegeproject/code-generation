package org.protege.owl.codegeneration;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertEquals;

import java.util.Collection;

import org.protege.owl.codegeneration.testSimple.A1;
import org.protege.owl.codegeneration.testSimple.A2;
import org.protege.owl.codegeneration.testSimple.B1;
import org.protege.owl.codegeneration.testSimple.B2;
import org.protege.owl.codegeneration.testSimple.IriA;
import org.protege.owl.codegeneration.testSimple.IriB;
import org.protege.owl.codegeneration.testSimple.MyFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.testng.annotations.Test;

public class TestCodeGeneration {
	public static String NS01="http://protege.org/ontologies/CodeGeneration001.owl";

	@Test
	public void testPropertyByDomain() throws SecurityException, NoSuchMethodException {
		Class<IriA> interfaceA = IriA.class;
		Class<IriB> interfaceB = IriB.class;

		assertNotNull(interfaceA.getMethod("getIriP", new Class<?>[0]));
		TestUtilities.assertMethodNotFound(interfaceB, "getIriP", new Class<?>[0]);

		TestUtilities.assertMethodNotFound(interfaceA, "getIriQ", new Class<?>[0]);
		assertNotNull(interfaceB.getMethod("getIriQ", new Class<?>[0]));
	}
	
	@Test
	public void testGenericListTypes() throws SecurityException, NoSuchMethodException {
		Class<?>[] noArguments = new Class<?>[0];
		TestUtilities.assertReturnsCollectionOf(IriA.class.getMethod("getIriP", noArguments), WrappedIndividual.class);
		TestUtilities.assertReturnsCollectionOf(A1.class.getMethod("getIriP", noArguments), B1.class);
		TestUtilities.assertReturnsCollectionOf(A2.class.getMethod("getIriP", noArguments), B2.class);
		TestUtilities.assertReturnsCollectionOf(IriB.class.getMethod("getIriQ", noArguments), Object.class);
		TestUtilities.assertReturnsCollectionOf(B1.class.getMethod("getIriQ", noArguments), String.class);
		TestUtilities.assertReturnsCollectionOf(B2.class.getMethod("getIriQ", noArguments), Integer.class);		
	}

	@Test
	public void testDataValues01() throws Exception {
		MyFactory factory = TestUtilities.openFactory("CodeGeneration001.owl", MyFactory.class);
		IriB y = factory.getIriB(NS01 + "#y");
		boolean hasBoolean = false;
		boolean hasRational = false;
		boolean hasFloat = false;
		for (Object o : y.getIriQ()) {
			if (o instanceof Boolean) {
				hasBoolean = true;
			}
			else if (o instanceof OWLLiteral) {
				OWLLiteral literal = (OWLLiteral) o;
				assertTrue(literal.getDatatype().getIRI().equals(OWL2Datatype.OWL_RATIONAL.getIRI()));
				hasRational = true;
			}
			else if (o instanceof Float) {
				hasFloat = true;
			}
			else {
				fail("Unexpected return type");
			}
		}
		assertTrue(hasBoolean && hasRational && hasFloat);
	}
	
	@Test
	public void testDataValues02() throws Exception {
		MyFactory factory = TestUtilities.openFactory("CodeGeneration001.owl", MyFactory.class);
		B1 y1 = factory.getB1(NS01 + "#y1");
		Collection<? extends String> values = y1.getIriQ();
		assertTrue(values.size() == 1);
		assertEquals(values.iterator().next(), "xyzzy");
	}
	
	@Test
	public void testDataValues03() throws Exception {
		MyFactory factory = TestUtilities.openFactory("CodeGeneration001.owl", MyFactory.class);
		B2 y2 = factory.getB2(NS01 + "#y2");
		Collection<? extends Integer> values = y2.getIriQ();
		assertTrue(values.size() == 1);
		assertEquals(values.iterator().next(), new Integer(8));
	}
	
	@Test
	public void testObjectValues() throws Exception {
		MyFactory factory = TestUtilities.openFactory("CodeGeneration001.owl", MyFactory.class);
		IriA x = factory.getIriA(NS01 + "#x");
		Collection<? extends WrappedIndividual> values = x.getIriP();
		assertTrue(values.size() == 1);
		assertEquals(values.iterator().next().getOwlIndividual().getIRI().toString(), NS01 + "#y");
	}
	
	@Test
	public void testBadType() throws Exception {
		MyFactory factory = TestUtilities.openFactory("CodeGeneration001.owl", MyFactory.class);
		IriB x = factory.getIriB(NS01 + "#x");
		assertNull(x);
	}
}
