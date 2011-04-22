package org.protege.owl.codegeneration;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	
	public <X extends WrappedIndividual> X createWrappedIndividual(String name, Class<X> c) {
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLNamedIndividual(IRI.create(name))));
		return getWrappedIndividual(name, c);
	}
	
	public <X extends WrappedIndividual> X getWrappedIndividual(String name, Class<X> c) {
		try {
    		Constructor<X> constructor = c.getConstructor(OWLOntology.class, IRI.class);
    		return constructor.newInstance(ontology, IRI.create(name));
		}
		catch (Exception e) {
			throw new CodeGenerationRuntimeException(e);
		}
	}
	
	public <X extends WrappedIndividual> Collection<X> getWrappedIndividuals(String name, Class<X> c) {
		Set<X> wrappers = new HashSet<X>();
		OWLClass owlClass = factory.getOWLClass(IRI.create(name));
		for (OWLNamedIndividual i : inference.getIndividuals(owlClass)) {
			wrappers.add(getWrappedIndividual(i.getIRI().toString(), c));
		}
		return wrappers;
	}
	
}
