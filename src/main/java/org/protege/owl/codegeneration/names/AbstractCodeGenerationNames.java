package org.protege.owl.codegeneration.names;

import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.semanticweb.owlapi.model.OWLClass;


public abstract class AbstractCodeGenerationNames implements CodeGenerationNames {
	private CodeGenerationOptions options;
	
	
	public AbstractCodeGenerationNames(CodeGenerationOptions options) {
		this.options = options;
	}

    /** 
     *    ****** I don't yet understand what this is about ********
     *    
     *  Generates and returns the interface name of provided owlClass. 
     *  The function appends a prefix to the interface name if abstract 
     *  mode is set to true.
     * @param owlClass
     * @return
     */
    public String getInterfaceNamePossiblyAbstract(OWLClass owlClass) {
        String interfaceName = getInterfaceName(owlClass);
        if (options.getAbstractMode()) {
            interfaceName += "_";
        }
        return interfaceName;
    }
    
    public String getImplementationName(OWLClass owlClass) {
        return "Default" + getInterfaceName(owlClass);
    }
    
    public String getImplementationNamePossiblyAbstract(OWLClass owlClass) {
        return "Default" + getInterfaceNamePossiblyAbstract(owlClass);
    }
}
