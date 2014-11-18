package org.protege.owl.codegeneration;


public enum SubstitutionVariable {
	PACKAGE("package"),
	FACTORY_CLASS_NAME("factoryClass"),
	FACTORY_EXTRA_IMPORT("factoryExtraImport"),
	FACTORY_PACKAGE("factoryPackage"),
    INTERFACE_LIST("superInterfaces"),
	INTERFACE_NAME("interfaceName"),
	IMPLEMENTATION_EXTRA_IMPORT("implementationExtraImport"),
	IMPLEMENTATION_NAME("implementationName"),
	CLASS_IRI("classIri"),
    PROPERTY_IRI("propertyIri"),
	CAPITALIZED_CLASS("OwlClass"),
	UPPERCASE_CLASS("OWLClass"),
	VOCABULARY_CLASS("VocabClass"),
	PROPERTY("owlProperty"), 
	CAPITALIZED_PROPERTY("OwlProperty"),
	UPPERCASE_PROPERTY("OWLProperty"),
	VOCABULARY_PROPERTY("VocabProperty"),
	PROPERTY_RANGE("propertyRange"),
	PROPERTY_RANGE_FOR_CLASS("propertyRangeForClass"),
	PROPERTY_RANGE_IMPLEMENTATION("propertyRangeImplementation"),
	JAVADOC("javadoc"),
	DATE("date"),
	USER("user");
	
	private String name;
	
	private SubstitutionVariable(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
