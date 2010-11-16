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

    /**Constructor
     * @param owlDataFactory
     * @param iri
     * @param owlOntology
     */
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
    
    /**
     * Deletes the individual from Ontology 
     */
    protected void deleteIndividual() {
        OWLEntityRemover remover = new OWLEntityRemover(getOwlOntology().getOWLOntologyManager(), Collections
                .singleton(getOwlOntology()));
        accept(remover);
        getOwlOntology().getOWLOntologyManager().applyChanges(remover.getChanges());
    }
    
    /**Removes the value from object property of the individual
     * @param owlObjectProperty The property from which the value is to be removed
     * @param owlNamedIndividual The value to be removed
     */
    protected void removeObjectPropertyValue(OWLObjectProperty owlObjectProperty, OWLNamedIndividual owlNamedIndividual) {
        Set<OWLIndividual> values = getObjectPropertyValues(owlObjectProperty, getOwlOntology());
        if (values == null || values.isEmpty()) {
            return;
        }
        for (OWLIndividual owlIndividual : values) {
            if (owlIndividual.isNamed() && owlIndividual.asOWLNamedIndividual().getIRI().toString().equals(owlNamedIndividual.getIRI().toString())) {
                OWLObjectPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
                        owlObjectProperty, this, owlIndividual);
                getOwlOntology().getOWLOntologyManager().removeAxiom(getOwlOntology(), axiom);
                break;
            }
        }
    }
    
    /**Removes the value from data property of the individual
     * @param owlDataProperty The property from which the value is to be removed
     * @param owlLiteralToRemove The owl literal to be removed
     */
    protected void removeDataPropertyValue(OWLDataProperty owlDataProperty, OWLLiteral owlLiteralToRemove ) {
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

    /**Checks whether the individual contains the owl literal 
     * @param owlDataProperty The property to check from 
     * @param owlLiteralToCheck The owl literal to check for
     * @return the result
     */
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
