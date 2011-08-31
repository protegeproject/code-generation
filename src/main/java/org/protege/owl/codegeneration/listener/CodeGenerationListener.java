package org.protege.owl.codegeneration.listener;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.codegeneration.CodeGenerationFactory;
import org.protege.owl.codegeneration.WrappedIndividual;
import org.protege.owl.codegeneration.impl.WrappedIndividualImpl;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;

public abstract class CodeGenerationListener<X extends WrappedIndividual> implements OWLOntologyChangeListener {
    private Set<OWLNamedIndividual> signature;
    private Set<OWLNamedIndividual> handledForCreation = new TreeSet<OWLNamedIndividual>();
    private Set<OWLNamedIndividual> handledForModification = new TreeSet<OWLNamedIndividual>();
    private CodeGenerationFactory factory;
    private CodeGenerationInference inference;
    private Class<? extends X> javaInterface;
    private OWLClass type;

    public CodeGenerationListener(CodeGenerationFactory factory, Class<? extends X> javaInterface) {
        this.signature = factory.getOwlOntology().getIndividualsInSignature();
        this.factory = factory;
        this.javaInterface = javaInterface;
        inference = factory.getInference();
        type = factory.getOwlClassFromJavaInterface(javaInterface);
    }
   
    public abstract void individualCreated(X individual);
    public abstract void individualModified(X individual);

    @Override
    public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
        try {
            reset();
            handleCreationEvents(changes);
            handleModificationEvents(changes);
        }
        finally {
            signature = factory.getOwlOntology().getIndividualsInSignature();
        }            
    }
    
    private void reset() {
        handledForCreation.clear();
        handledForModification.clear();
        factory.flushOwlReasoner();
    }

    private void handleCreationEvents(List<? extends OWLOntologyChange> changes) {
        for (OWLOntologyChange change : changes) {
            if (change instanceof AddAxiom) { 
                handleCreationEvent((AddAxiom) change);
            }
        }
    }
    
    private void handleCreationEvent(AddAxiom change) {
        for (OWLEntity e : change.getEntities()) {
            if (e instanceof OWLNamedIndividual 
                    && !handledForCreation.contains(e)
                    && !signature.contains(e)) {
                handledForCreation.add((OWLNamedIndividual) e);
                if (inference.canAs((OWLNamedIndividual) e, type)) {
                    WrappedIndividual wrapped = new WrappedIndividualImpl(factory.getOwlOntology(), (OWLNamedIndividual) e);
                    individualCreated(factory.as(wrapped, javaInterface));
                }
            }
        }
    }
    
    private void handleModificationEvents(List<? extends OWLOntologyChange> changes) {
        for (OWLOntologyChange change : changes) {
            if (change instanceof OWLAxiomChange) { 
                handleModificationEvent((OWLAxiomChange) change);
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void handleModificationEvent(OWLAxiomChange change) {
        OWLAxiom axiom = change.getAxiom();
        if (axiom instanceof OWLPropertyAssertionAxiom
                && ((OWLPropertyAssertionAxiom) axiom).getSubject().isNamed()) {
            OWLNamedIndividual i = ((OWLPropertyAssertionAxiom) change.getAxiom()).getSubject().asOWLNamedIndividual();
            if (!handledForModification.contains(i) && inference.canAs((OWLNamedIndividual) i, type)) {
                WrappedIndividual wrapped = new WrappedIndividualImpl(factory.getOwlOntology(), i);
                individualModified(factory.as(wrapped, javaInterface));
            }
        }
    }

}
