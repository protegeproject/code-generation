package org.protege.owl.codegeneration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.protege.owl.codegeneration.std.testSimple02.A1;
import org.protege.owl.codegeneration.std.testSimple02.B1;
import org.protege.owl.codegeneration.std.testSimple02.IriA;
import org.protege.owl.codegeneration.std.testSimple02.IriB;
import org.protege.owl.codegeneration.std.testSimple02.MySimpleStdFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.testng.annotations.Test;

public class TestStdCodeGeneration {
	
	@Test
	public void testSimpleCreate() throws Exception {
		MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);
		String newA1 = TestUtilities.NS01 + "#aNewA1";
		String newB1 = TestUtilities.NS01 + "#aNewB1";
		String b1Value = "hello";
		A1 a1 = factory.createA1(newA1);
		assertEquals(factory.getA1(newA1), a1);
		B1 b1 = factory.createB1(newB1);
		a1.addIriP(b1);
		b1.addIriQ(b1Value);
		
		assertEquals(a1.getIriP().size(), 1);
		assertTrue(a1.getIriP().contains(b1));
		
		assertEquals(b1.getIriQ().size(), 1);
		assertTrue(b1.getIriQ().contains(b1Value));
		
		assertTrue(factory.getAllA1Instances().contains(a1));
		assertTrue(factory.getAllB1Instances().contains(b1));
	}
	
	@Test
	public void testSimpleGet() throws Exception {
		MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);
		IriA x = factory.getIriA(TestUtilities.NS01 + "#x");
		IriB y = factory.getIriB(TestUtilities.NS01 + "#y");
		assertNotNull(x);
		assertEquals(x.getIriP().size(), 1);
		assertTrue(x.getIriP().contains(y));
	}
	
	@Test
	public void testStringBasic() throws Exception {
	       MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);
	        String newA1 = TestUtilities.NS01 + "#aNewA1";
	        String newB1 = TestUtilities.NS01 + "#aNewB1";
	        String b1Value = "hello";
	        A1 a1 = factory.createA1(newA1);
	        assertEquals(factory.getA1(newA1), a1);
	        B1 b1 = factory.createB1(newB1);
	        a1.addIriP(b1);
	        b1.addIriQ(b1Value);
	        
	        assertTrue(a1.toString().contains("iriP: aNewB1;"));
	        assertTrue(a1.toString().startsWith("A1("));
	        assertTrue(b1.toString().contains("iriQ: hello;"));
	        assertTrue(b1.toString().startsWith("B1("));
	}
	
	@Test
	public void testStringMultipleTypes() throws Exception {
        MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);
        String newA1 = TestUtilities.NS01 + "#aNewA1";
        A1 a1 = factory.createA1(newA1);
        a1.assertOwlType(org.protege.owl.codegeneration.std.testSimple02.Vocabulary.CLASS_B1);
        assertTrue(a1.toString().startsWith("[A1, B1]("));
	}
	
	@Test
	public void testStringNoTypes() throws Exception {
        MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);
        String newA1 = TestUtilities.NS01 + "#aNewA1";
        A1 a1 = factory.createA1(newA1);
        OWLOntologyManager manager = a1.getOwlOntology().getOWLOntologyManager();
        OWLDataFactory owlApiFactory = manager.getOWLDataFactory();
        OWLAxiom typeAxiom = owlApiFactory.getOWLClassAssertionAxiom(org.protege.owl.codegeneration.std.testSimple02.Vocabulary.CLASS_A1, a1.getOwlIndividual());
        manager.removeAxiom(a1.getOwlOntology(), typeAxiom);
        
        assertTrue(a1.toString().startsWith("Untyped("));
	}
	
}
