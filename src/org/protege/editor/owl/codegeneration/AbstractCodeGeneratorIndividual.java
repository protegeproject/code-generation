package org.protege.editor.owl.codegeneration;

import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * @author z.khan
 * 
 */
public class AbstractCodeGeneratorIndividual extends OWLNamedIndividualImpl{
    
    private OWLOntology owlOntology;

    public AbstractCodeGeneratorIndividual(OWLDataFactory owlDataFactory, IRI iri, OWLOntology owlOntology) {
        super(owlDataFactory, iri);
        this.owlOntology = owlOntology;
    }

    /**
     * @return the owlOntology
     */
    public OWLOntology getOwlOntology() {
        return owlOntology;
    }
    
    protected void deleteIndividual() {
        OWLEntityRemover remover = new OWLEntityRemover(getOwlOntology().getOWLOntologyManager(), Collections
                .singleton(getOwlOntology()));
        accept(remover);
        getOwlOntology().getOWLOntologyManager().applyChanges(remover.getChanges());
    }
    
    /**
     * @param oldHasTopping
     * @param owlObjectProperty 
     */
    protected void removePropertyValue(OWLNamedIndividual oldHasTopping, OWLObjectProperty owlObjectProperty) {
        Set<OWLIndividual> values = getObjectPropertyValues(owlObjectProperty, getOwlOntology());
        if (values == null || values.isEmpty()) {
            return;
        }
        for (OWLIndividual owlIndividual : values) {
            if (owlIndividual.isNamed() && owlIndividual.asOWLNamedIndividual().getIRI().toString().equals(oldHasTopping.getIRI().toString())) {
                OWLObjectPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
                        owlObjectProperty, this, owlIndividual);
                getOwlOntology().getOWLOntologyManager().removeAxiom(getOwlOntology(), axiom);
                break;
            }
        }
    }

}
