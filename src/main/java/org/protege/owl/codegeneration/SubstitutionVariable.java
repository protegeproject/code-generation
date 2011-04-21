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
	PROPERTY("owlProperty"), 
	CAPITALIZED_PROPERTY("OwlProperty"),
	UPPERCASE_PROPERTY("OWLProperty"),
	PROPERTY_RANGE("propertyRange"),
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
	
	public static String getTemplate(String resource) {
		String template = templateMap.get(resource);
		if (template == null) {
			try {
				URL u = SubstitutionVariable.class.getResource(resource);
				Reader reader = new InputStreamReader(u.openStream());
				StringBuffer buffer = new StringBuffer();
				int charsRead;
				char[] characters = new char[1024];
				while (true) {
					charsRead = reader.read(characters);
					if (charsRead < 0) {
						break;
					}
					buffer.append(characters, 0, charsRead);
				}
				template = buffer.toString();
				templateMap.put(resource, template);
				reader.close();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return template;
	}
	
	public static void fillTemplate(PrintWriter writer, String resource, Map<SubstitutionVariable, String> substitutions) {
		String result = getTemplate(resource);
		for (Entry<SubstitutionVariable, String> entry : substitutions.entrySet()) {
			SubstitutionVariable var = entry.getKey();
			String replacement = entry.getValue();
			result = result.replaceAll("\\$\\{" + var.getName() + "\\}", replacement);
		}
		writer.append(result);
	}
}
