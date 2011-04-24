package org.protege.owl.codegeneration.inference;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public interface CodeGenerationInference {

	Collection<OWLClass> getOwlClasses();
	
	Collection<OWLClass> getSuperClasses(OWLClass owlClass);
	
	Collection<OWLNamedIndividual> getIndividuals(OWLClass owlClass);
	
	boolean canAs(OWLNamedIndividual i, OWLClass c);
	
	boolean canAssert(OWLNamedIndividual i, OWLClass c);
	
	Collection<OWLClass> getTypes(OWLNamedIndividual i);
	
	Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass cls);
		
	Collection<OWLClass> getRange(OWLClass cls, OWLObjectProperty p);
	
	Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass cls);
		
	OWLDatatype getRange(OWLClass cls, OWLDataProperty p);
}
