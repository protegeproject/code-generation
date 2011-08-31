package org.protege.owl.codegeneration;

import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.codegeneration.inferred.pizza.CheeseyPizza;
import org.protege.owl.codegeneration.inferred.pizza.MyPizzaFactory;
import org.protege.owl.codegeneration.inferred.pizza.Pizza;
import org.protege.owl.codegeneration.listener.CodeGenerationListener;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestListener {

    @Test
    public void testListener() throws Exception {
        MyPizzaFactory factory = TestUtilities.openFactory(TestUtilities.PIZZA_ONTOLOGY, MyPizzaFactory.class, false);
        TestCodeGenerationListener listener = new TestCodeGenerationListener(factory);
        factory.getOwlOntology().getOWLOntologyManager().addOntologyChangeListener(listener);
        
        CheeseyPizza cp = factory.createCheeseyPizza(TestUtilities.PIZZA_NS + "#sundaySpecial");
        Assert.assertEquals(listener.getCreated().size(), 1);
        Assert.assertTrue(listener.getCreated().contains(cp));
        Assert.assertTrue(listener.getModified().isEmpty());
        
    }
    
    private static class TestCodeGenerationListener extends CodeGenerationListener<Pizza> {
        
        private Set<Pizza> created = new TreeSet<Pizza>();
        private Set<Pizza> modified = new TreeSet<Pizza>();


        public TestCodeGenerationListener(MyPizzaFactory factory) {
            super(factory, Pizza.class);
        }
        
        @Override
        public void individualCreated(Pizza individual) {
            created.add(individual);
        }

        @Override
        public void individualModified(Pizza individual) {
            modified.add(individual);
        }
        
        public Set<Pizza> getCreated() {
            return created;
        }
        
        public Set<Pizza> getModified() {
            return modified;
        }
        
        public void clear() {
            created.clear();
            modified.clear();
        }
        
    }
}
