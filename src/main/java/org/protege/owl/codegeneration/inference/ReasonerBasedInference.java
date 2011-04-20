package org.protege.owl.codegeneration.inference;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class ReasonerBasedInference implements CodeGenerationInference {
	private OWLReasoner reasoner;

	public ReasonerBasedInference(OWLReasoner reasoner) {
		this.reasoner = reasoner;
	}
	
	public Collection<OWLClass> getTopLevelClasses() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
