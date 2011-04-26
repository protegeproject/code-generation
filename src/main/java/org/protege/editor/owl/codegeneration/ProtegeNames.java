package org.protege.editor.owl.codegeneration;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.names.AbstractCodeGenerationNames;
import org.protege.owl.codegeneration.names.NamingUtilities;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class ProtegeNames extends AbstractCodeGenerationNames {
	private OWLModelManager manager;

	public ProtegeNames(OWLModelManager manager, CodeGenerationOptions options) {
		super(options);
		this.manager = manager;
	}

	public String getInterfaceName(OWLClass owlClass) {
		String name = manager.getRendering(owlClass);
		name = NamingUtilities.convertToJavaIdentifier(name);
		name = NamingUtilities.convertInitialLetterToUpperCase(name);
		return name;
	}
	
	public String getClassName(OWLClass owlClass) {
		String name = manager.getRendering(owlClass);
		name = NamingUtilities.convertToJavaIdentifier(name);
		return name;
	}

	
	public String getObjectPropertyName(OWLObjectProperty owlObjectProperty) {
		String name = manager.getRendering(owlObjectProperty);
		name = NamingUtilities.convertToJavaIdentifier(name);
		return name;
	}
	
	
	public String getDataPropertyName(OWLDataProperty owlDataProperty) {
		String name = manager.getRendering(owlDataProperty);
		name = NamingUtilities.convertToJavaIdentifier(name);
		return name;
	}
	
}
