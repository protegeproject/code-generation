package org.protege.owl.codegeneration;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author z.khan
 * 
 */
public interface WrappedIndividual {
 
	OWLOntology getOwlOntology();
	
	OWLNamedIndividual getOwlIndividual();
}
