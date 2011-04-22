package org.protege.owl.codegeneration.names;

import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class IriNames extends AbstractCodeGenerationNames {
	private OWLOntology ontology;
	private ShortFormProvider provider;

	public IriNames(OWLOntology ontology, CodeGenerationOptions options) {
		super(options);
		this.ontology = ontology;
		provider = new SimpleShortFormProvider();
	}
	
	
	
	public String getInterfaceName(OWLClass owlClass) {
		String name = provider.getShortForm(owlClass);
		name = NamingUtilities.convertToJavaIdentifier(name);
		name = NamingUtilities.convertInitialLetterToUpperCase(name);
		return name;
	}
	
	public String getClassName(OWLClass owlClass) {
		String name = provider.getShortForm(owlClass);
		name = NamingUtilities.convertToJavaIdentifier(name);
		return name;
	}

	
	public String getObjectPropertyName(OWLObjectProperty owlObjectProperty) {
		String name = provider.getShortForm(owlObjectProperty);
		name = NamingUtilities.convertToJavaIdentifier(name);
		return name;
	}
	
	
	public String getDataPropertyName(OWLDataProperty owlDataProperty) {
		String name = provider.getShortForm(owlDataProperty);
		name = NamingUtilities.convertToJavaIdentifier(name);
		return name;
	}
}
