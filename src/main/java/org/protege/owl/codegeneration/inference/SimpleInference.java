package org.protege.owl.codegeneration.inference;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class SimpleInference implements CodeGenerationInference {
	private OWLOntology ontology;
	
	public SimpleInference(OWLOntology ontology) {
		this.ontology = ontology;
	}
	
	public Collection<OWLClass> getOwlClasses() {
		return ontology.getClassesInSignature(true);
	}
	
	@Override
	public Collection<OWLNamedIndividual> getIndividuals(OWLClass owlClass) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public Collection<OWLClass> getSuperClasses(OWLClass owlClass) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public Collection<OWLClass> getTypes(OWLNamedIndividual i) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass cls) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public Collection<OWLClass> getRange(OWLClass cls, OWLObjectProperty p) {
		throw new UnsupportedOperationException("Not supported yet");
	}
	
	public Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass cls) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public OWLDatatype getRange(OWLClass cls, OWLDataProperty p) {
		throw new UnsupportedOperationException("Not supported yet");
	}
}
