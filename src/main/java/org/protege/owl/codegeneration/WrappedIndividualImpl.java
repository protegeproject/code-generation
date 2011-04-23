package org.protege.owl.codegeneration;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OWLEntityRemover;

/**
 * @author z.khan
 * 
 */
public class WrappedIndividualImpl implements WrappedIndividual {
    
    private OWLOntology owlOntology;
    private OWLNamedIndividual owlIndividual;
    private CodeGenerationHelper delegate;
    

    /**Constructor
     * @param owlDataFactory
     * @param iri
     * @param owlOntology
     */
    public WrappedIndividualImpl(OWLOntology owlOntology, IRI iri) {
        this.owlOntology = owlOntology;
        owlIndividual = owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri);
        delegate = new CodeGenerationHelper(owlOntology, owlIndividual);
    }

    
    
    /**
     * @return the owlOntology
     */
    public OWLOntology getOwlOntology() {
        return owlOntology;
    }
    
    public OWLNamedIndividual getOwlIndividual() {
		return owlIndividual;
	}
    
    public CodeGenerationHelper getDelegate() {
		return delegate;
	}
    
    /**
     * Deletes the individual from Ontology 
     */
    public void delete() {
        OWLEntityRemover remover = new OWLEntityRemover(getOwlOntology().getOWLOntologyManager(), Collections
                .singleton(getOwlOntology()));
        owlIndividual.accept(remover);
        getOwlOntology().getOWLOntologyManager().applyChanges(remover.getChanges());
    }
    

}
