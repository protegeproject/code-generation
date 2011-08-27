package org.protege.owl.codegeneration;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;

import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.ReasonerBasedInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.testng.Assert;

public class TestUtilities {

	public static String NS01="http://protege.org/ontologies/CodeGeneration001.owl";
    public static String ONTOLOGY01="CodeGeneration001.owl";
    
    public static String PIZZA_ONTOLOGY = "pizza.owl";

    private TestUtilities() { }
	
	public static void assertMethodNotFound(Class<?> c, String method, Class<?>...arguments) {
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
	
	public static void assertReturnsCollectionOf(Method m, Class<?> c) {
		ParameterizedType returnType = (ParameterizedType) m.getGenericReturnType();
		assertTrue(returnType.getRawType().equals(Collection.class));
		Type[] typeArgs = returnType.getActualTypeArguments();
		assertTrue(typeArgs.length == 1);
		WildcardType wildCardCollectedType = (WildcardType) typeArgs[0];
		Type[] upperBounds = wildCardCollectedType.getUpperBounds();
		assertTrue(upperBounds.length == 1);
		assertTrue(upperBounds[0].equals(c));
	}
	
	public static <X> X openFactory(String ontologyName, Class<X> factoryClass, boolean useInference) throws Exception {
		Constructor<? extends X> constructor = factoryClass.getConstructor(OWLOntology.class, CodeGenerationInference.class);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/" + ontologyName));
		
		CodeGenerationInference inference;
		if (useInference) {
			OWLReasonerFactory rFactory = (OWLReasonerFactory) Class.forName("org.semanticweb.HermiT.Reasoner$ReasonerFactory").newInstance();
			OWLReasoner reasoner = rFactory.createNonBufferingReasoner(ontology);
			inference = new ReasonerBasedInference(ontology, reasoner);
		}
		else {
			inference = new SimpleInference(ontology);
		}
        return constructor.newInstance(ontology, inference);
	}
}
