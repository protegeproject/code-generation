package org.protege.owl.codegeneration.property;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.names.CodeGenerationNames;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * This class provides a java property declarations object for any class and property.  The 
 * primary responsibility of this class is to ensure that if
 * <ul>
 * <li> A class or interface X has a method m,
 * <li> A class or interface Y extends/implements the class X
 * </ul>
 * then it must be true that the class or interface Y has a method that specializes the method m.  By 
 * putting the code to ensure this property here we release other classes (such as the code generation inference)
 * from concerning themselves with this issue.
 * <p/>
 * 
 */
public class JavaPropertyDeclarationCache {
	private CodeGenerationInference inference;
	private CodeGenerationNames names;;
	private Map<OWLClass, Map<OWLEntity, JavaPropertyDeclaration>> class2Property2DeclarationMap
				= new HashMap<OWLClass, Map<OWLEntity, JavaPropertyDeclaration>>();
	
	public JavaPropertyDeclarationCache(CodeGenerationInference inference, CodeGenerationNames names) {
		this.inference = inference;
		this.names     = names;
		generateCache();
	}

	private void generateCache() {
		OWLDataFactory factory = inference.getOWLOntology().getOWLOntologyManager().getOWLDataFactory();
		Set<OWLClass> explored = new HashSet<OWLClass>();
		OWLClass thing = factory.getOWLThing();
		for (JavaPropertyDeclaration declarations : inference.getJavaPropertyDeclarations(thing, names)) {
			add(thing, declarations.getOwlProperty(), declarations);
		}
		generateChildren(thing, explored);
	}
	
	private void generateChildren(OWLClass parent, Set<OWLClass> explored) {
		if (explored.contains(parent)) {
			return;
		}
		explored.add(parent);
		Map<OWLEntity, JavaPropertyDeclaration> parentProperty2DeclarationMap = class2Property2DeclarationMap.get(parent);
		if (parentProperty2DeclarationMap == null) {
			parentProperty2DeclarationMap = Collections.emptyMap();
		}
		for (OWLClass child : inference.getSubClasses(parent)) {
			copyDeclarations(parentProperty2DeclarationMap, parent, child);
			generateChildren(child, explored);
		}		
	}
	
	private void copyDeclarations(Map<OWLEntity, JavaPropertyDeclaration> parentProperty2DeclarationMap, 
								  OWLClass parent,
								  OWLClass child) {
		for (Entry<OWLEntity, JavaPropertyDeclaration> entry : parentProperty2DeclarationMap.entrySet()) {
			OWLEntity property = entry.getKey();
			JavaPropertyDeclaration declarations = entry.getValue();
			add(child, property,  declarations.specializeTo(child));
		}
		for (JavaPropertyDeclaration childDeclarations : inference.getJavaPropertyDeclarations(child, names)) {
			OWLEntity property = childDeclarations.getOwlProperty();
			if (!parentProperty2DeclarationMap.containsKey(property)) {
				add(child, property, childDeclarations);
			}
		}
	}
	
	private void add(OWLClass owlClass, OWLEntity property, JavaPropertyDeclaration declarations) {
		Map<OWLEntity, JavaPropertyDeclaration> property2DeclarationsMap = class2Property2DeclarationMap.get(owlClass);
		if (property2DeclarationsMap == null) {
			property2DeclarationsMap = new HashMap<OWLEntity, JavaPropertyDeclaration>();
			class2Property2DeclarationMap.put(owlClass, property2DeclarationsMap);
		}
		property2DeclarationsMap.put(property, declarations);
	}
	
	
	public JavaPropertyDeclaration get(OWLClass clazz, OWLEntity property) {
		Map<OWLEntity, JavaPropertyDeclaration> property2DeclarationMap = class2Property2DeclarationMap.get(clazz);
		JavaPropertyDeclaration decls = null;
		if (property2DeclarationMap != null) {
			decls = property2DeclarationMap.get(property);
		}
		return decls;
	}
	
	public Set<OWLObjectProperty> getObjectPropertiesForClass(OWLClass owlClass) {
		return getPropertiesForClass(owlClass, OWLObjectProperty.class);
	}
	
	public Set<OWLDataProperty> getDataPropertiesForClass(OWLClass owlClass) {
		return getPropertiesForClass(owlClass, OWLDataProperty.class);
	}
	
	private <X extends Comparable<OWLObject>> Set<X> getPropertiesForClass(OWLClass owlClass, Class<? extends X> javaClass) {
		Map<OWLEntity, JavaPropertyDeclaration> property2DeclarationMap = class2Property2DeclarationMap.get(owlClass);
		if (property2DeclarationMap == null) {
			return Collections.emptySet();
		}
		Set<X> properties = new TreeSet<X>();
		for (OWLEntity property : property2DeclarationMap.keySet()) {
			if (javaClass.isAssignableFrom(property.getClass())) {
				properties.add(javaClass.cast(property));
			}
		}
		return properties;
	}

}
