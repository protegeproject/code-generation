package org.protege.owl.codegeneration;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;


public interface Worker {
	
	OWLOntology getOwlOntology();
	
	void initialize();
	
	Collection<OWLClass> getOwlClasses();
	
	Collection<OWLObjectProperty> getOwlObjectProperties();
	
	Collection<OWLDataProperty> getOwlDataProperties();
	
	Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass owlClass);
	
	Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass owlClass);
	
	File getInterfaceFile(OWLClass c);
	
	File getImplementationFile(OWLClass c);
	
	File getVocabularyFile();
	
	File getFactoryFile();
	
	void configureSubstitutions(CodeGenerationPhase phase,
			                    Map<SubstitutionVariable, String> substitutions,
			                    OWLClass owlClass,
			                    OWLEntity owlProperty);
	
	String getTemplate(CodeGenerationPhase phase, OWLClass owlClass, Object owlProperty);
}
