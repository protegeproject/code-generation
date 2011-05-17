package org.protege.owl.codegeneration.inference;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

public class SimpleInference extends ReasonerBasedInference {

	
	public SimpleInference(OWLOntology ontology) {
		super(ontology, getSimpleReasoner(ontology));
	}
	
	private static OWLReasoner getSimpleReasoner(OWLOntology ontology) {
		StructuralReasonerFactory factory = new StructuralReasonerFactory();
		return factory.createReasoner(ontology);
	}
	
}
