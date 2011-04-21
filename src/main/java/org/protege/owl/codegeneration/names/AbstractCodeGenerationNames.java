package org.protege.owl.codegeneration.names;

import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.semanticweb.owlapi.model.OWLClass;


public abstract class AbstractCodeGenerationNames implements CodeGenerationNames {
	private CodeGenerationOptions options;
	
	
	public AbstractCodeGenerationNames(CodeGenerationOptions options) {
		this.options = options;
	}
    
    public String getImplementationName(OWLClass owlClass) {
        return "Default" + getInterfaceName(owlClass);
    }

}
