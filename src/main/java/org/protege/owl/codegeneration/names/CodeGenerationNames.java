package org.protege.owl.codegeneration.names;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public interface CodeGenerationNames {
	
	String getImplementationName(OWLClass owlClass);
	
	String getImplementationNamePossiblyAbstract(OWLClass owlClass);
	
	String getInterfaceName(OWLClass owlClass);
	
	String getInterfaceNamePossiblyAbstract(OWLClass owlClass);
	
	String getObjectPropertyName(OWLObjectProperty owlObjectProperty);
	
	String getDataPropertyName(OWLDataProperty owlDataProperty);
}
