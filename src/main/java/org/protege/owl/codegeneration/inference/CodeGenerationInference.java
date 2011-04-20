package org.protege.owl.codegeneration.inference;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClass;

public interface CodeGenerationInference {

	Collection<OWLClass> getTopLevelClasses();
}
