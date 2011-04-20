package org.protege.owl.codegeneration.inference;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

public class SimpleInference implements CodeGenerationInference {
	private OWLOntology ontology;
	
	public SimpleInference(OWLOntology ontology) {
		this.ontology = ontology;
	}
	
	public Collection<OWLClass> getTopLevelClasses() {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
