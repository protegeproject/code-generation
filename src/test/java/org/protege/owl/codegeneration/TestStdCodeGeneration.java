package org.protege.owl.codegeneration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.protege.owl.codegeneration.std.testSimple02.A1;
import org.protege.owl.codegeneration.std.testSimple02.B1;
import org.protege.owl.codegeneration.std.testSimple02.IriA;
import org.protege.owl.codegeneration.std.testSimple02.IriB;
import org.protege.owl.codegeneration.std.testSimple02.MySimpleStdFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.testng.annotations.Test;

public class TestStdCodeGeneration {
	
	@Test
	public void testSimpleCreate() throws Exception {
		MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);
		String b1Value = "hello";
		A1 a1 = factory.createA1(TestUtilities.ONTOLOGY_NEW_A1);
		assertEquals(factory.getA1(TestUtilities.ONTOLOGY_NEW_A1), a1);
		B1 b1 = factory.createB1(TestUtilities.ONTOLOGY_NEW_B1);
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
		IriA x = factory.getIriA(TestUtilities.ONTOLOGY01X);
		IriB y = factory.getIriB(TestUtilities.ONTOLOGY01Y);
		assertNotNull(x);
		assertEquals(x.getIriP().size(), 1);
		assertTrue(x.getIriP().contains(y));
	}
	
	@Test
	public void testStringBasic() throws Exception {
	       MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);
	        String b1Value = "hello";
	        A1 a1 = factory.createA1(TestUtilities.ONTOLOGY_NEW_A1);
	        assertEquals(factory.getA1(TestUtilities.ONTOLOGY_NEW_A1), a1);
	        B1 b1 = factory.createB1(TestUtilities.ONTOLOGY_NEW_B1);
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
        A1 a1 = factory.createA1(TestUtilities.ONTOLOGY_NEW_A1);
        a1.assertOwlType(org.protege.owl.codegeneration.std.testSimple02.Vocabulary.CLASS_B1);
        assertTrue(a1.toString().startsWith("[A1, B1]("));
	}
	
	@Test
	public void testStringNoTypes() throws Exception {
        MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);
        A1 a1 = factory.createA1(TestUtilities.ONTOLOGY_NEW_A1);
        OWLOntologyManager manager = a1.getOwlOntology().getOWLOntologyManager();
        OWLDataFactory owlApiFactory = manager.getOWLDataFactory();
        OWLAxiom typeAxiom = owlApiFactory.getOWLClassAssertionAxiom(org.protege.owl.codegeneration.std.testSimple02.Vocabulary.CLASS_A1, a1.getOwlIndividual());
        manager.removeAxiom(a1.getOwlOntology(), typeAxiom);
        
        assertTrue(a1.toString().startsWith("Untyped("));
	}
	
	@Test
	public void testChangeSave() throws Exception {
		MySimpleStdFactory factory = TestUtilities.openFactory(TestUtilities.ONTOLOGY01, MySimpleStdFactory.class, false);		
        A1 a1 = factory.createA1(TestUtilities.ONTOLOGY_NEW_A1);
		IriB y = factory.getIriB(TestUtilities.ONTOLOGY01Y);

        a1.addIriP(y);;
		assertEquals(a1.getIriP().size(), 1);
		assertTrue(a1.getIriP().contains(y));
		
		File savedLocation = save(factory);
		MySimpleStdFactory reloadedFactory = load(savedLocation);
		
        A1 reloaded_a1 = reloadedFactory.createA1(TestUtilities.ONTOLOGY_NEW_A1);
		IriB reloaded_y = reloadedFactory.getIriB(TestUtilities.ONTOLOGY01Y);
		assertEquals(reloaded_a1.getIriP().size(), 1);
		assertTrue(reloaded_a1.getIriP().contains(reloaded_y));
	}
	
	
	public static File save(MySimpleStdFactory factory) throws IOException, OWLOntologyStorageException {
		File saveFile = File.createTempFile("SaveTest", "owl");
		OWLOntology ontology = factory.getOwlOntology();
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		manager.setOntologyDocumentIRI(ontology, IRI.create(saveFile));
		
		factory.saveOwlOntology();
		
		return saveFile;
	}
	
	public static MySimpleStdFactory load(File ontologyFile) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
		return new MySimpleStdFactory(ontology);
	}
}
