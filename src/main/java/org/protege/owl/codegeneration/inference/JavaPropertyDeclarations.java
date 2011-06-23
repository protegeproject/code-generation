package org.protege.owl.codegeneration.inference;

import java.util.Map;

import org.protege.owl.codegeneration.SubstitutionVariable;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

public interface JavaPropertyDeclarations {

	OWLEntity getOwlProperty();
	
	JavaPropertyDeclarations specializeTo(OWLClass subclass);
	
	void configureSubstitutions(Map<SubstitutionVariable, String> substitutions);
}
