package org.protege.owl.codegeneration.impl;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.protege.owl.codegeneration.CodeGenerationRuntimeException;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class FactoryHelper {
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private CodeGenerationInference inference;
	
	public FactoryHelper(OWLOntology ontology, CodeGenerationInference inference) {
		this.ontology = ontology;
		this.inference = inference;
		manager = ontology.getOWLOntologyManager();
		factory = manager.getOWLDataFactory();
	}
	
	public void flushOwlReasoner() {
	    inference.flush();
	}
	
	public <X extends WrappedIndividualImpl> X createWrappedIndividual(String name, OWLClass type, Class<X> c) {
		OWLNamedIndividual i = factory.getOWLNamedIndividual(IRI.create(name));
		manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(type, i));
		if (!inference.canAs(i, type)) {
			return null;
		}
		return getWrappedIndividual(name, c);
	}
	
	public <X extends WrappedIndividualImpl> X getWrappedIndividual(String name, OWLClass type, Class<X> c) {
		IRI iri = IRI.create(name);
		OWLNamedIndividual i = factory.getOWLNamedIndividual(iri);
		if (!inference.canAs(i, type)) {
			return null;
		}
		return getWrappedIndividual(name, c);
	}
	
	private <X extends WrappedIndividualImpl> X getWrappedIndividual(String name, Class<X> c) {
		try {
    		Constructor<X> constructor = c.getConstructor(OWLOntology.class, IRI.class);
    		return constructor.newInstance(ontology, IRI.create(name));
		}
		catch (Exception e) {
			throw new CodeGenerationRuntimeException(e);
		}
	}
	
	public <X extends WrappedIndividualImpl> Collection<X> getWrappedIndividuals(OWLClass owlClass, Class<X> c) {
		Set<X> wrappers = new HashSet<X>();
		for (OWLNamedIndividual i : inference.getIndividuals(owlClass)) {
			wrappers.add(getWrappedIndividual(i.getIRI().toString(), c));
		}
		return wrappers;
	}
	
}
