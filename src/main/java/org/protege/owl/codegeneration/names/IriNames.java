package org.protege.owl.codegeneration.names;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.Constants;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class IriNames extends AbstractCodeGenerationNames {
	private ShortFormProvider provider;

	public IriNames(OWLOntology ontology, CodeGenerationOptions options) {
		super(options);
		
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		List<OWLAnnotationProperty> annotationProperties = Collections.singletonList(Constants.JAVANAME);
		Map<OWLAnnotationProperty, List<String>> preferredLanguageMap = new HashMap<OWLAnnotationProperty, List<String>>();
		OWLOntologySetProvider ontologySetProvider = new OWLOntologyImportsClosureSetProvider(manager, ontology);
		provider = new AnnotationValueShortFormProvider(annotationProperties, preferredLanguageMap, ontologySetProvider);
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
