package org.protege.owl.codegeneration.names;

import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class PrefixNames extends AbstractCodeGenerationNames {
	
	public PrefixNames(OWLOntology ontology, CodeGenerationOptions options) {
		super(options);
	}
	
	@Override
	public String getInterfaceName(OWLClass owlClass) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	@Override
	public String getObjectPropertyName(OWLObjectProperty owlObjectProperty) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	@Override
	public String getDataPropertyName(OWLDataProperty owlDataProperty) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
