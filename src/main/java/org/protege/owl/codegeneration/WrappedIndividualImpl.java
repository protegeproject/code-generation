package org.protege.owl.codegeneration;

import java.util.Collections;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
    
    protected CodeGenerationHelper getDelegate() {
		return delegate;
	}
    
    /**
     * Asserts that the individual has a particular OWL type.
     */
    
    public void assertOwlType(OWLClassExpression type) {
        OWLOntologyManager manager = owlOntology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        manager.addAxiom(owlOntology, factory.getOWLClassAssertionAxiom(type, owlIndividual));
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
    
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof WrappedIndividual)) {
    		return false;
    	}
    	WrappedIndividual other = (WrappedIndividual) obj;
    	return other.getOwlOntology().equals(owlOntology) && other.getOwlIndividual().equals(owlIndividual);
    }
    
    @Override
    public int hashCode() {
    	return owlOntology.hashCode() + 42 * owlIndividual.hashCode();
    }

}
