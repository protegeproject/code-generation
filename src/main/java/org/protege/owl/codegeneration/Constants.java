package org.protege.owl.codegeneration;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;

public class Constants {
    public static final String CODE_GENERATION_NS = "http://protege.org/code";
    
    
    public static final OWLAnnotationProperty JAVADOC;
    public static final OWLAnnotationProperty JAVANAME;
    /*
     * This allows one to ignore OWL properties in the generation of the output code.
     */
    public static final OWLAnnotationProperty IGNORE;
    
    static {
    	OWLDataFactory factory = OWLManager.getOWLDataFactory();
    
    	JAVADOC  = factory.getOWLAnnotationProperty(IRI.create(CODE_GENERATION_NS + "#javadoc"));
    	JAVANAME = factory.getOWLAnnotationProperty(IRI.create(CODE_GENERATION_NS + "#javaname"));
    	IGNORE   = factory.getOWLAnnotationProperty(IRI.create(CODE_GENERATION_NS + "#ignore"));
        
    	
    }
    
	public static final String UKNOWN_CODE_GENERATED_INTERFACE = "WrappedIndividual";
	public static final String ABSTRACT_CODE_GENERATOR_INDIVIDUAL_CLASS = "WrappedIndividualImpl";
	public static final String UNKNOWN_JAVA_DATA_TYPE = "Object";

	public static final String VOCABULARY_CLASS_NAME = "Vocabulary";
	public static final String FACTORY_CLASS_NAME = "MyFactory";

	private Constants() {}
}
