package org.protege.editor.owl.codegeneration;

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

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * @author z.khan
 * 
 */
public abstract class AbstractCodeGeneratorIndividual extends OWLNamedIndividualImpl{
    
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
    protected void removeObjectPropertyValue(OWLNamedIndividual oldHasTopping, OWLObjectProperty owlObjectProperty) {
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
    
    protected void removeDataPropertyValue(OWLLiteral owlLiteralToRemove, OWLDataProperty owlDataProperty) {
        Set<OWLLiteral> values = getDataPropertyValues(owlDataProperty, getOwlOntology());
        if (values == null || values.isEmpty()) {
            return;
        }
        for (OWLLiteral owlLiteral : values) {
            if(owlLiteral.getLiteral().equals(owlLiteralToRemove.getLiteral())){
                OWLDataPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLDataPropertyAssertionAxiom(
                        owlDataProperty, this, owlLiteral);
                getOwlOntology().getOWLOntologyManager().removeAxiom(getOwlOntology(), axiom);
                break;
            }
            
        }
    }

    protected boolean doesPropertyContainsLiteral(OWLDataProperty owlDataProperty, OWLLiteral owlLiteralToCheck) {
        Set<OWLLiteral> values = getDataPropertyValues(owlDataProperty, getOwlOntology());
        if (values == null || values.isEmpty()) {
            return false;
        }
        for (OWLLiteral owlLiteral : values) {
            if(owlLiteral.getLiteral().equals(owlLiteralToCheck.getLiteral())){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sets the Data Property value
     * 
     * @param owlDataProperty
     *            The data property whose value to set
     * @param literal
     */
    protected void setDataProperty(OWLDataProperty owlDataProperty, OWLLiteral literal) {
        OWLDataPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLDataPropertyAssertionAxiom(owlDataProperty,
                this, literal);
        getOwlOntology().getOWLOntologyManager().addAxiom(getOwlOntology(), axiom);
    }
}
