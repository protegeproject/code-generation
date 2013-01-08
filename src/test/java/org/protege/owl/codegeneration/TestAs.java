package org.protege.owl.codegeneration;

import org.protege.owl.codegeneration.inferred.pizza.CheeseyPizza;
import org.protege.owl.codegeneration.inferred.pizza.FruitTopping;
import org.protege.owl.codegeneration.inferred.pizza.HamTopping;
import org.protege.owl.codegeneration.inferred.pizza.InterestingPizza;
import org.protege.owl.codegeneration.inferred.pizza.MyInferredPizzaFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAs {

    @Test
    public void testCanAsNoCast() throws Exception {
        MyInferredPizzaFactory factory = TestUtilities.openFactory(TestUtilities.PIZZA_ONTOLOGY, MyInferredPizzaFactory.class, false);
        CheeseyPizza myPizza = factory.createCheeseyPizza(TestUtilities.PIZZA_NS + "#myPizza");
        Assert.assertFalse(factory.canAs(myPizza, InterestingPizza.class));
        InterestingPizza myInterestingPizza = factory.as(myPizza, InterestingPizza.class);
        Assert.assertNull(myInterestingPizza);
        
        myPizza.assertOwlType(org.protege.owl.codegeneration.inferred.pizza.Vocabulary.CLASS_INTERESTINGPIZZA);
        Assert.assertTrue(factory.canAs(myPizza, InterestingPizza.class));
        myInterestingPizza = factory.as(myPizza, InterestingPizza.class);
        Assert.assertNotNull(myInterestingPizza);
        Assert.assertEquals(myPizza.getOwlIndividual(), myInterestingPizza.getOwlIndividual());
        Assert.assertEquals(myPizza.getOwlOntology(), myInterestingPizza.getOwlOntology());
    }
    
    @Test
    public void testInferredCanAs() throws Exception {
        MyInferredPizzaFactory factory = TestUtilities.openFactory(TestUtilities.PIZZA_ONTOLOGY, MyInferredPizzaFactory.class, true);
        CheeseyPizza myPizza = factory.createCheeseyPizza(TestUtilities.PIZZA_NS + "#myPizza");
        factory.flushOwlReasoner();
        Assert.assertFalse(factory.canAs(myPizza, InterestingPizza.class));
        
        FruitTopping myPineapple = factory.createFruitTopping(TestUtilities.PIZZA_NS + "#myPineappleTopping");
        myPizza.addHasTopping(myPineapple);
        HamTopping myHamTopping = factory.createHamTopping(TestUtilities.PIZZA_NS + "#myHamTopping");
        myPizza.addHasTopping(myHamTopping);
        factory.flushOwlReasoner();
        
        Assert.assertTrue(factory.canAs(myPizza, InterestingPizza.class));
        InterestingPizza myInterestingPizza = factory.as(myPizza, InterestingPizza.class);
        Assert.assertNotNull(myInterestingPizza);
        Assert.assertEquals(myPizza.getOwlIndividual(), myInterestingPizza.getOwlIndividual());
        Assert.assertEquals(myPizza.getOwlOntology(), myInterestingPizza.getOwlOntology());
    }
    
    
}
