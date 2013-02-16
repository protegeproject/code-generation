package org.protege.owl.codegeneration.email_20130214;

import junit.framework.Assert;

import org.protege.owl.codegeneration.TestUtilities;
import org.protege.owl.codegeneration.inferred.febissue.ComplexFact;
import org.protege.owl.codegeneration.inferred.febissue.Fact;
import org.protege.owl.codegeneration.inferred.febissue.FebIssueFactory;
import org.protege.owl.codegeneration.inferred.febissue.Vocabulary;
import org.testng.annotations.Test;

public class TestIssue {
	public static final String TEMP_NS = "http://jamesnaish.wordpress.com/Temporary/TEMP0259948137.owl#";
	public static final String AUTO_NS = "http://jamesnaish.wordpress.com/Model/AutopilotSourceModel.owl#";
	
    @Test
    public void testInferredCanAs() throws Exception {
    	FebIssueFactory factory = TestUtilities.openFactory(TestUtilities.FEB_INDIVIDUALS_ONTOLOGY, FebIssueFactory.class, true);
    	Fact autoPlane = factory.getFact(AUTO_NS + "Plane");
    	Assert.assertNotNull(autoPlane);
    	Assert.assertTrue(factory.canAs(autoPlane, ComplexFact.class));
    	Assert.assertNotNull(factory.as(autoPlane, ComplexFact.class));
    	
    	Fact tempPlane = factory.getFact(TEMP_NS + "Plane");
    	Assert.assertNotNull(tempPlane);
    	Assert.assertTrue(factory.canAs(tempPlane, ComplexFact.class));
    	Assert.assertNotNull(factory.as(tempPlane, ComplexFact.class));
    	
    	Fact x = factory.createFact(TEMP_NS + "newfact");
    	factory.flushOwlReasoner();
    	Assert.assertTrue(factory.getAllFactInstances().contains(x));
    	Assert.assertTrue(factory.getInference().canAs(x.getOwlIndividual(), Vocabulary.CLASS_FACT));
    	Assert.assertFalse(factory.getAllComplexFactInstances().contains(x));
    	
    	ComplexFact y = factory.createComplexFact(TEMP_NS + "yFact");
    	factory.flushOwlReasoner();
    	Assert.assertTrue(factory.getAllFactInstances().contains(y));
    }
}
