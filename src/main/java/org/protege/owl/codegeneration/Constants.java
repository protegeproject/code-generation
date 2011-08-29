package org.protege.owl.codegeneration;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

public class Constants {
    public static final String CODE_GENERATION_NS = "http://protege.org/code";
    public static final OWLAnnotationProperty JAVADOC = OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(CODE_GENERATION_NS + "#javadoc"));
	
	public static final String UKNOWN_CODE_GENERATED_INTERFACE = "WrappedIndividual";
	public static final String ABSTRACT_CODE_GENERATOR_INDIVIDUAL_CLASS = "WrappedIndividualImpl";
	public static final String UNKNOWN_JAVA_DATA_TYPE = "Object";

	public static final String VOCABULARY_CLASS_NAME = "Vocabulary";
	public static final String FACTORY_CLASS_NAME = "MyFactory";

	private Constants() {}
}
