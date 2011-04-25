package org.protege.owl.codegeneration.inference;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class SimpleInference implements CodeGenerationInference {
	private OWLOntology ontology;
	private Set<OWLObjectProperty> objectProperties;
	private Set<OWLDataProperty> dataProperties;
	
	public SimpleInference(OWLOntology ontology) {
		this.ontology = ontology;
		objectProperties = new HashSet<OWLObjectProperty>();
		dataProperties = new HashSet<OWLDataProperty>();
		for (OWLOntology inImportsClosure : ontology.getImportsClosure()) {
			objectProperties.addAll(inImportsClosure.getObjectPropertiesInSignature());
			dataProperties.addAll(inImportsClosure.getDataPropertiesInSignature());
		}
	}
	
	public Collection<OWLClass> getOwlClasses() {
		return ontology.getClassesInSignature(true);
	}
	
	public Collection<OWLNamedIndividual> getIndividuals(OWLClass owlClass) {
		Collection<OWLNamedIndividual> individuals = new HashSet<OWLNamedIndividual>();
		for (OWLIndividual individual : owlClass.getIndividuals(ontology.getImportsClosure())) {
			if (individual.isNamed()) {
				individuals.add(individual.asOWLNamedIndividual());
			}
		}
		return individuals;
	}
	
	public boolean canAs(OWLNamedIndividual i, OWLClass c) {
		return i.getTypes(ontology.getImportsClosure()).contains(c);
	}
	
	public boolean canAssert(OWLNamedIndividual i, OWLClass c) {
		for (OWLOntology inImportsClosure : ontology.getImportsClosure()) {
			if (inImportsClosure.containsEntityInSignature(i)) {
				return false;
			}
		}
		return true;
	}
	
	public Collection<OWLClass> getSuperClasses(OWLClass owlClass) {
		Collection<OWLClass> superClasses = new HashSet<OWLClass>();
		for (OWLClassExpression superClass : owlClass.getSuperClasses(ontology.getImportsClosure())) {
			if (!superClass.isAnonymous()) {
				superClasses.add(superClass.asOWLClass());
			}
		}
		return superClasses;
	}
	
	public Collection<OWLClass> getTypes(OWLNamedIndividual i) {
		Collection<OWLClass> types = new HashSet<OWLClass>();
		for (OWLClassExpression type : i.getTypes(ontology.getImportsClosure())) {
			if (!type.isAnonymous()) {
				types.add(type.asOWLClass());
			}
		}
		return types;
	}
	
	public Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass cls) {
		Set<OWLObjectProperty> propertiesForClass = new HashSet<OWLObjectProperty>();
		for (OWLObjectProperty property : objectProperties) {
			if (property.getDomains(ontology.getImportsClosure()).contains(cls)) {
				propertiesForClass.add(property);
			}
		}
		return propertiesForClass;
	}
	
	public Collection<OWLClass> getRange(OWLClass cls, OWLObjectProperty p) {
		throw new UnsupportedOperationException("Not supported yet");
	}
	
	public Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass cls) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public OWLDatatype getRange(OWLClass cls, OWLDataProperty p) {
		throw new UnsupportedOperationException("Not supported yet");
	}
}
