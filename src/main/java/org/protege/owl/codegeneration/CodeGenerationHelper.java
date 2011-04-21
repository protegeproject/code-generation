package org.protege.owl.codegeneration;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class CodeGenerationHelper {
    private OWLOntology owlOntology;
    private OWLNamedIndividual owlIndividual;
    private OWLDataFactory owlDataFactory;
    
    
    public CodeGenerationHelper(OWLOntology owlOntology, OWLNamedIndividual individual) {
        this.owlOntology = owlOntology;
        owlDataFactory = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        owlIndividual = individual;
    }
    
    public OWLOntology getOwlOntology() {
        return owlOntology;
    }
    
    public OWLNamedIndividual getOwlIndividual() {
		return owlIndividual;
	}
    
    public <X> Collection<X> getPropertyValues(OWLNamedIndividual i, OWLObjectProperty p, Class<X> c) {
    	try {
    		Constructor<X> constructor = c.getConstructor(OWLOntology.class, IRI.class);
    		Set<X> results = new HashSet<X>();
    		for (OWLOntology imported : owlOntology.getImportsClosure()) {
    			for (OWLIndividual j : i.getObjectPropertyValues(p, imported)) {
    				if (!j.isAnonymous()) {
    					results.add(constructor.newInstance(owlOntology, j.asOWLNamedIndividual().getIRI()));
    				}
    			}
    		}
    		return results;
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
 
    public void addPropertyValue(OWLNamedIndividual i, OWLObjectProperty p, WrappedIndividual j) {
    	OWLAxiom axiom = owlDataFactory.getOWLObjectPropertyAssertionAxiom(p, i, j.getOwlIndividual());
    	owlOntology.getOWLOntologyManager().addAxiom(owlOntology, axiom);
    }
    
}
