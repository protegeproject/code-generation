package org.protege.owl.codegeneration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public enum SubstitutionVariable {
	PACKAGE("package"),
	JAVA_CLASS_NAME("javaClass"),
	IMPLEMENTS_EXTENDS("extends"),
	INTERFACE_NAME("interfaceName"),
	IMPLEMENTATION_NAME("implementationName"),
	IRI("iri"),
	CLASS("owlClass"), 
	CAPITALIZED_CLASS("OwlClass"),
	UPPERCASE_CLASS("OWLClass"),
	PROPERTY("owlProperty"), 
	CAPITALIZED_PROPERTY("OwlProperty"),
	UPPERCASE_PROPERTY("OWLProperty"),
	PROPERTY_RANGE("propertyRange"),
	PROPERTY_RANGE_FOR_CLASS("propertyRangeForClass"),
	PROPERTY_RANGE_IMPLEMENTATION("propertyRangeImplementation"),
	DATE("date"),
	USER("user");
	
	private static Map<String, String> templateMap = new HashMap<String, String>();
	private String name;
	
	private SubstitutionVariable(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
