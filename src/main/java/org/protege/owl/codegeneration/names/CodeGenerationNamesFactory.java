package org.protege.owl.codegeneration.names;

import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.semanticweb.owlapi.model.OWLOntology;

public class CodeGenerationNamesFactory {
	private OWLOntology ontology;
	private CodeGenerationOptions options;
	
	public CodeGenerationNamesFactory(OWLOntology ontology,
            			              CodeGenerationOptions options) {
		this.ontology = ontology;
		this.options = options;
	}
	
	public CodeGenerationNames getCGNames() {
		return options.getPrefixMode() ? new PrefixNames(ontology, options) : new IriNames(ontology, options);
	}
}
