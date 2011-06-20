package org.protege.owl.codegeneration.inference;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

public class SimpleInference extends ReasonerBasedInference {
	private OWLOntology ontology;
	
	public SimpleInference(OWLOntology ontology) {
		super(ontology, getSimpleReasoner(ontology));
		this.ontology = ontology;
	}
	
	private static OWLReasoner getSimpleReasoner(OWLOntology ontology) {
		StructuralReasonerFactory factory = new StructuralReasonerFactory();
		return factory.createReasoner(ontology);
	}
	
	@Override
	public boolean canAs(OWLNamedIndividual i, OWLClass c) {
		return i.getTypes(ontology.getImportsClosure()).contains(c);
	}
	
}
