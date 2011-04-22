package org.protege.owl.codegeneration;

public enum CodeGenerationPhase {
	CREATE_INTERFACE_HEADER("interface.header"),
	CREATE_OBJECT_PROPERTY_INTERFACE("interface.object.property"),
	CREATE_DATA_PROPERTY_INTERFACE("interface.data.property"),
	CREATE_INTERFACE_TAIL("interface.tail"),
	CREATE_IMPLEMENTATION_HEADER("implementation.header"),
	CREATE_OBJECT_PROPERTY_IMPLEMENTATION("implementation.object.property"),
	CREATE_DATA_PROPERTY_IMPLEMENTATION("implementation.data.property"),
	CREATE_IMPLEMENTATION_TAIL("implementation.tail"),
	CREATE_VOCABULARY_HEADER("vocabulary.header"),
	CREATE_CLASS_VOCABULARY("vocabulary.owlclass"),
	CREATE_OBJECT_PROPERTY_VOCABULARY("vocabulary.object.property"),
	CREATE_DATA_PROPERTY_VOCABULARY("vocabulary.data.property"),
	CREATE_VOCABULARY_TAIL("vocabulary.tail"),
	CREATE_FACTORY_HEADER("factory.header"),
	CREATE_FACTORY_TAIL("factory.tail"),
	CREATE_FACTORY_CLASS("factory.owlclass")
	;
	
	private String templateName;
	
	private CodeGenerationPhase(String templateName) {
		this.templateName = templateName;
	}
	
	public String getTemplateName() {
		return templateName;
	}
}
