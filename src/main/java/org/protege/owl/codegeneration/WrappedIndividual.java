package org.protege.owl.codegeneration;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author z.khan
 * 
 */
public interface WrappedIndividual extends Comparable<WrappedIndividual> {
 
	OWLOntology getOwlOntology();
	
	OWLNamedIndividual getOwlIndividual();
	
	void assertOwlType(OWLClassExpression type);
}
