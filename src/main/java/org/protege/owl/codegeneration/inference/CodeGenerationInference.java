package org.protege.owl.codegeneration.inference;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public interface CodeGenerationInference {

	Collection<OWLClass> getClasses();
	
	Collection<OWLClass> getTypes(OWLNamedIndividual i);
	
	Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass cls);
	
	boolean isFunctional(OWLClass cls, OWLObjectProperty p);
	
	Collection<OWLClass> getRange(OWLClass cls, OWLObjectProperty p);
	
	Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass cls);
	
	boolean isFunctional(OWLClass cls, OWLDataProperty p);
	
	OWLDatatype getRange(OWLClass cls, OWLDataProperty p);
}
