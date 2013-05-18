package org.protege.owl.codegeneration;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import junit.framework.Assert;

import org.protege.owl.codegeneration.inferred.propertyValues.A;
import org.protege.owl.codegeneration.inferred.propertyValues.B;
import org.protege.owl.codegeneration.inferred.propertyValues.InferredPropertyValuesFactory;
import org.protege.owl.codegeneration.inferred.propertyValues.Vocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.testng.annotations.Test;

public class TestInferredPropertyValues {
    public static final OWLAxiom A_HAS_I_AXIOM;
    public static final OWLNamedIndividual I;
    static {
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        I = factory.getOWLNamedIndividual(IRI.create(TestUtilities.PROPERTY_VALUES_NS + "i"));
        A_HAS_I_AXIOM = factory.getOWLSubClassOfAxiom(Vocabulary.CLASS_A, 
                                                      factory.getOWLObjectHasValue(Vocabulary.OBJECT_PROPERTY_P, I));
    }

    @Test
    public void testInferredPropertyValues() throws SecurityException, OWLOntologyCreationException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
        InferredPropertyValuesFactory factory = TestUtilities.openFactory(TestUtilities.PROPERTY_VALUES_ONTOLOGY, InferredPropertyValuesFactory.class, true);
        A i = factory.getA(TestUtilities.PROPERTY_VALUES_NS + "i");
        B j = factory.getB(TestUtilities.PROPERTY_VALUES_NS + "j");
        Assert.assertTrue(i.getP().contains(j));
        Assert.assertTrue(j.getQ().contains(new Integer(9)));
    }
    
    @Test
    public void testAssertedPropertyValues() throws SecurityException, OWLOntologyCreationException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
        InferredPropertyValuesFactory factory = TestUtilities.openFactory(TestUtilities.PROPERTY_VALUES_ONTOLOGY, InferredPropertyValuesFactory.class, false);
        A i = factory.getA(TestUtilities.PROPERTY_VALUES_NS + "i");
        B j = factory.getB(TestUtilities.PROPERTY_VALUES_NS + "j");
        Assert.assertFalse(i.getP().contains(j));
        Assert.assertFalse(j.getQ().contains(new Integer(9)));
    }
    
    @Test
    public void testInferenceUpdate() throws SecurityException, OWLOntologyCreationException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
        InferredPropertyValuesFactory factory = TestUtilities.openFactory(TestUtilities.PROPERTY_VALUES_ONTOLOGY, InferredPropertyValuesFactory.class, true);
        A i = factory.getA(TestUtilities.PROPERTY_VALUES_NS + "i");
        Assert.assertFalse(i.getP().contains(i));
        OWLOntologyManager manager = factory.getOwlOntology().getOWLOntologyManager();
        manager.applyChanges(Collections.singletonList(new AddAxiom(factory.getOwlOntology(), A_HAS_I_AXIOM)));
        Assert.assertFalse(i.getP().contains(i));
        factory.flushOwlReasoner();
        Assert.assertTrue(i.getP().contains(i));
    }
}
