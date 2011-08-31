package org.protege.owl.codegeneration;

import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public interface CodeGenerationFactory {

    OWLOntology getOwlOntology();

    void saveOwlOntology() throws OWLOntologyStorageException;

    void flushOwlReasoner();

    boolean canAs(WrappedIndividual resource, Class<? extends WrappedIndividual> javaInterface);

    <X extends WrappedIndividual> X as(WrappedIndividual resource, Class<? extends X> javaInterface);

    Class<?> getJavaInterfaceFromOwlClass(OWLClass cls);

    OWLClass getOwlClassFromJavaInterface(Class<?> javaInterface);

    CodeGenerationInference getInference();
}
