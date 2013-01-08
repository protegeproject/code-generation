package org.protege.owl.codegeneration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.protege.owl.codegeneration.inferred.testSimple.A1;
import org.protege.owl.codegeneration.inferred.testSimple.A2;
import org.protege.owl.codegeneration.inferred.testSimple.B1;
import org.protege.owl.codegeneration.inferred.testSimple.B2;
import org.protege.owl.codegeneration.inferred.testSimple.IriA;
import org.protege.owl.codegeneration.inferred.testSimple.IriB;
import org.protege.owl.codegeneration.inferred.testSimple.MyInferredFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.testng.annotations.Test;

public class TestInferredCodeGeneration {
	
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
		TestUtilities.assertReturnsCollectionOf(A1.class.getMethod("getIriP", noArguments), WrappedIndividual.class);
		TestUtilities.assertReturnsCollectionOf(A2.class.getMethod("getIriP", noArguments), WrappedIndividual.class);
		TestUtilities.assertReturnsCollectionOf(IriB.class.getMethod("getIriQ", noArguments), Object.class);
		TestUtilities.assertReturnsCollectionOf(B1.class.getMethod("getIriQ", noArguments), String.class);
		TestUtilities.assertReturnsCollectionOf(B2.class.getMethod("getIriQ", noArguments), Integer.class);		
	}

	@Test
	public void testDataValues01() throws Exception {
		MyInferredFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MyInferredFactory.class, true);
		IriB y = factory.getIriB(TestUtilities.NS01 + "#y");
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
		MyInferredFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MyInferredFactory.class, true);
		B1 y1 = factory.getB1(TestUtilities.NS01 + "#y1");
		Collection<? extends String> values = y1.getIriQ();
		assertTrue(values.size() == 1);
		assertEquals(values.iterator().next(), "xyzzy");
	}
	
	@Test
	public void testDataValues03() throws Exception {
		MyInferredFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MyInferredFactory.class, true);
		B2 y2 = factory.getB2(TestUtilities.NS01 + "#y2");
		Collection<? extends Integer> values = y2.getIriQ();
		assertTrue(values.size() == 1);
		assertEquals(values.iterator().next(), new Integer(8));
	}
	
	@Test
	public void testObjectValues() throws Exception {
		MyInferredFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MyInferredFactory.class, true);
		IriA x = factory.getIriA(TestUtilities.NS01 + "#x");
		Collection<? extends WrappedIndividual> values = x.getIriP();
		assertTrue(values.size() == 1);
		assertEquals(values.iterator().next().getOwlIndividual().getIRI().toString(), TestUtilities.NS01 + "#y");
	}
	
	@Test
	public void testBadType() throws Exception {
		MyInferredFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MyInferredFactory.class, true);
		IriB x = factory.getIriB(TestUtilities.NS01 + "#x");
		assertNull(x);
	}

	@Test
	public void testCreate() throws SecurityException, NoSuchMethodException, OWLOntologyCreationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException {
		MyInferredFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MyInferredFactory.class, true);
		A1 a1 = factory.createA1("newA1");
		factory.flushOwlReasoner();
		assertTrue(factory.getAllA1Instances().contains(a1));
	}
}
