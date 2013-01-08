package org.protege.owl.codegeneration;

import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inferred.pizza.CheeseyPizza;
import org.protege.owl.codegeneration.inferred.pizza.MyInferredPizzaFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSimpleInference {

    @Test
    public void testInferredSubclasses() throws Exception {
        MyInferredPizzaFactory factory = TestUtilities.openFactory(TestUtilities.PIZZA_ONTOLOGY, MyInferredPizzaFactory.class, false);
        CodeGenerationInference inference = factory.getInference();
        OWLClass pizza = org.protege.owl.codegeneration.inferred.pizza.Vocabulary.CLASS_PIZZA;
        OWLClass cheeseyPizza = org.protege.owl.codegeneration.inferred.pizza.Vocabulary.CLASS_CHEESEYPIZZA;
        
        Assert.assertTrue(inference.getSubClasses(pizza).contains(cheeseyPizza));
        OWLOntology ontology = factory.getOwlOntology();
        OWLDataFactory owlapiFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLAxiom subClassAxiom = owlapiFactory.getOWLSubClassOfAxiom(pizza, cheeseyPizza);
        Assert.assertFalse(factory.getOwlOntology().containsAxiom(subClassAxiom));
    }
    
    @Test
    public void testInferredSuperclasses() throws Exception {
        MyInferredPizzaFactory factory = TestUtilities.openFactory(TestUtilities.PIZZA_ONTOLOGY, MyInferredPizzaFactory.class, false);
        CodeGenerationInference inference = factory.getInference();
        OWLClass pizza = org.protege.owl.codegeneration.inferred.pizza.Vocabulary.CLASS_PIZZA;
        OWLClass cheeseyPizza = org.protege.owl.codegeneration.inferred.pizza.Vocabulary.CLASS_CHEESEYPIZZA;
        
        Assert.assertTrue(inference.getSuperClasses(cheeseyPizza).contains(pizza));
        OWLOntology ontology = factory.getOwlOntology();
        OWLDataFactory owlapiFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLAxiom subClassAxiom = owlapiFactory.getOWLSubClassOfAxiom(pizza, cheeseyPizza);
        Assert.assertFalse(factory.getOwlOntology().containsAxiom(subClassAxiom));
    }
    
    @Test
    public void testIndirectTypes() throws Exception {
        MyInferredPizzaFactory factory = TestUtilities.openFactory(TestUtilities.PIZZA_ONTOLOGY, MyInferredPizzaFactory.class, false);
        CodeGenerationInference inference = factory.getInference();
        CheeseyPizza cp = factory.createCheeseyPizza(TestUtilities.PIZZA_NS + "#myVeryCheeseyPizza");
        OWLClass domain = org.protege.owl.codegeneration.inferred.pizza.Vocabulary.CLASS_DOMAINCONCEPT;
        Assert.assertTrue(inference.canAs(cp.getOwlIndividual(), domain));
    }
    
    
}
