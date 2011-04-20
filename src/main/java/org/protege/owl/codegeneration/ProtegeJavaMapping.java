package org.protege.owl.codegeneration;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class ProtegeJavaMapping {
    private CodeGenerationInference inference;
    private OWLDataFactory dataFactory;
    private OWLOntology ontology;
    
    private Map<String, Entry> protegeMap  = new HashMap<String, Entry>();
    private Map<Class, Entry> interfaceMap = new HashMap<Class, Entry>();
    private Map<Class, Entry> implementationMap = new HashMap<Class, Entry>();

    public ProtegeJavaMapping(OWLOntology ontology) {
    	inference = new SimpleInference(ontology);
    	dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
    }
    
    public ProtegeJavaMapping(OWLOntology ontology, CodeGenerationInference inference) {
    	this.inference = inference;
    }
    
    public void add(String protegeClassName, 
                           Class<?> javaInterface, 
                           Class<? extends AbstractCodeGeneratorIndividual> javaImplementation) {
        Entry entry = new Entry(protegeClassName, javaInterface, javaImplementation);
        protegeMap.put(protegeClassName, entry);
        interfaceMap.put(javaInterface, entry);
        implementationMap.put(javaImplementation, entry);
    }
    
    @SuppressWarnings("unchecked")
    public <X> X create(Class<? extends X> javaInterface, String name) {
        Entry entry = interfaceMap.get(javaInterface);
        if (entry == null) {
            return null;
        }
        OWLClass cls = dataFactory.getOWLClass(IRI.create(entry.getProtegeClass()));
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(IRI.create(name));
        ontology.getOWLOntologyManager().addAxiom(ontology, dataFactory.getOWLClassAssertionAxiom(cls, individual));
        return constructImplementation((Class<? extends X>) entry.getJavaImplementation(), individual.getIRI());
    }
    
    public boolean canAs(OWLNamedIndividual resource, Class<? extends OWLNamedIndividual> javaInterface) {
        if (javaInterface.isAssignableFrom(resource.getClass())) {
            return true;
        }
        return getJavaImplementation(resource, javaInterface) != null;
    }
    
    public  <X extends OWLNamedIndividual> X as(OWLNamedIndividual resource, Class<? extends X> javaInterface) {
        if (javaInterface.isAssignableFrom(resource.getClass())) {
            return javaInterface.cast(resource);
        }
        Class<? extends X> type = getJavaImplementation(resource, javaInterface);
        return constructImplementation(type, resource.getIRI());
    }
    
    private <X> X constructImplementation(Class<? extends X> implType, IRI id) {
        try {
            Constructor<? extends X> con = implType.getConstructor(new Class[] { OWLOntology.class, IRI.class});
            return con.newInstance(new Object[] { ontology, id });
        }
        catch (Throwable t) {
            ClassCastException classcast = new ClassCastException("Resource " + id.toString() + " could not be cast to type " + implType);
            classcast.initCause(t);
            throw classcast;
        }
    }
    
    private <X> Class<? extends X> getJavaImplementation(OWLNamedIndividual resource, Class<? extends X> javaInterface) {
    	Collection<OWLClass> protegeTypes = inference.getTypes(resource);
    	for (OWLClass type  : protegeTypes) {
            Entry entry = protegeMap.get(type.getIRI().toString());
            if (entry == null) {
                continue;
            }
            Class<? extends AbstractCodeGeneratorIndividual> javaImplementationClass = entry.getJavaImplementation();
            if (javaInterface.isAssignableFrom(javaImplementationClass)) {
                return javaImplementationClass.asSubclass(javaInterface);
            }
        }
        return null;
    }
    
    private static class Entry {
        private String protegeClass;
        private Class<?> javaInterface;
        private Class<? extends AbstractCodeGeneratorIndividual> javaImplementation;
        
        public Entry(String protegeClass,
                     Class<?> javaInterface,
                     Class<? extends AbstractCodeGeneratorIndividual> javaImplementation) {
            this.protegeClass = protegeClass;
            this.javaInterface = javaInterface;
            this.javaImplementation = javaImplementation;
        }

        public String getProtegeClass() {
            return protegeClass;
        }

        public Class<?> getJavaInterface() {
            return javaInterface;
        }

        public Class<? extends AbstractCodeGeneratorIndividual> getJavaImplementation() {
            return javaImplementation;
        }        
    }
    
}
