package org.protege.owl.codegeneration.impl;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.protege.owl.codegeneration.WrappedIndividual;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class ProtegeJavaMapping {
    private CodeGenerationInference inference;
    private OWLDataFactory dataFactory;
    private OWLOntology ontology;
    
    private Map<OWLClass, JavaAndOwlBean> protegeMap  = new HashMap<OWLClass, JavaAndOwlBean>();
    private Map<Class<?>, JavaAndOwlBean> interfaceMap = new HashMap<Class<?>, JavaAndOwlBean>();
    private Map<Class<?>, JavaAndOwlBean> implementationMap = new HashMap<Class<?>, JavaAndOwlBean>();

    public void initialize(OWLOntology ontology, CodeGenerationInference inference) {
        this.ontology = ontology;
        this.inference = inference;
        dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
    }
    
    public void add(String protegeClassName, 
                    Class<?> javaInterface, 
                    Class<? extends WrappedIndividualImpl> javaImplementation) {
        OWLClass protegeClass = OWLManager.getOWLDataFactory().getOWLClass(IRI.create(protegeClassName));
        add(protegeClass, javaInterface, javaImplementation);
    }
    
    public void add(OWLClass protegeClass, 
                    Class<?> javaInterface, 
                    Class<? extends WrappedIndividualImpl> javaImplementation) {
        JavaAndOwlBean entry = new JavaAndOwlBean(protegeClass, javaInterface, javaImplementation);
        protegeMap.put(protegeClass, entry);
        interfaceMap.put(javaInterface, entry);
        implementationMap.put(javaImplementation, entry);
    }
    
    public Class<?> getJavaInterfaceFromOwlClass(OWLClass cls) {
        JavaAndOwlBean bean = protegeMap.get(cls);
        return bean != null ? bean.getJavaInterface() : null;
    }
    
    public OWLClass getOwlClassFromJavaInterface(Class<?> javaInterface) {
        JavaAndOwlBean bean = interfaceMap.get(javaInterface);
        return bean != null ? bean.getProtegeClass() : null;
    }
    
    @SuppressWarnings("unchecked")
    public <X> X create(Class<? extends X> javaInterface, String name) {
        JavaAndOwlBean entry = interfaceMap.get(javaInterface);
        if (entry == null) {
            return null;
        }
        OWLClass cls = entry.getProtegeClass();
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(IRI.create(name));
        ontology.getOWLOntologyManager().addAxiom(ontology, dataFactory.getOWLClassAssertionAxiom(cls, individual));
        return constructImplementation((Class<? extends X>) entry.getJavaImplementation(), individual.getIRI());
    }
    
    public boolean canAs(WrappedIndividual resource, Class<? extends WrappedIndividual> javaInterface) {
        if (javaInterface.isAssignableFrom(resource.getClass())) {
            return true;
        }
        return getJavaImplementation(resource.getOwlIndividual(), javaInterface) != null;
    }
    
    public  <X extends WrappedIndividual> X as(WrappedIndividual resource, Class<? extends X> javaInterface) {
        if (javaInterface.isAssignableFrom(resource.getClass())) {
            return javaInterface.cast(resource);
        }
        Class<? extends X> type = getJavaImplementation(resource.getOwlIndividual(), javaInterface);
        return type != null ? constructImplementation(type, resource.getOwlIndividual().getIRI()) : null;
    }
    
    private <X> X constructImplementation(Class<? extends X> implType, IRI id) {
        try {
            Constructor<? extends X> con = implType.getConstructor(new Class[] { CodeGenerationInference.class, IRI.class});
            return con.newInstance(new Object[] { inference, id });
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
            JavaAndOwlBean entry = protegeMap.get(type);
            if (entry == null) {
                continue;
            }
            Class<? extends WrappedIndividualImpl> javaImplementationClass = entry.getJavaImplementation();
            if (javaInterface.isAssignableFrom(javaImplementationClass)) {
                return javaImplementationClass.asSubclass(javaInterface);
            }
        }
        return null;
    }
    
    private static class JavaAndOwlBean {
        private OWLClass protegeClass;
        private Class<?> javaInterface;
        private Class<? extends WrappedIndividualImpl> javaImplementation;
        
        public JavaAndOwlBean(OWLClass protegeClass,
                     Class<?> javaInterface,
                     Class<? extends WrappedIndividualImpl> javaImplementation) {
            this.protegeClass = protegeClass;
            this.javaInterface = javaInterface;
            this.javaImplementation = javaImplementation;
        }

        public OWLClass getProtegeClass() {
            return protegeClass;
        }

        public Class<?> getJavaInterface() {
            return javaInterface;
        }

        public Class<? extends WrappedIndividualImpl> getJavaImplementation() {
            return javaImplementation;
        }
        
        @Override
        public String toString() {
            return "<Class: " + protegeClass + ">";
        }
    }
    
}
