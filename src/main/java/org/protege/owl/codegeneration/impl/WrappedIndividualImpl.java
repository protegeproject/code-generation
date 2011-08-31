package org.protege.owl.codegeneration.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.codegeneration.WrappedIndividual;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

/**
 * @author z.khan
 * 
 */
public class WrappedIndividualImpl implements WrappedIndividual {
    
    private OWLOntology owlOntology;
    private OWLNamedIndividual owlIndividual;
    private CodeGenerationHelper delegate;
    
    /**Constructor
     * @param owlDataFactory
     * @param iri
     * @param owlOntology
     */
    public WrappedIndividualImpl(OWLOntology owlOntology, IRI iri) {
        this(owlOntology, owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri));
    }
    
    public WrappedIndividualImpl(OWLOntology owlOntology, OWLNamedIndividual owlIndividual) {
        this.owlOntology = owlOntology;
        this.owlIndividual = owlIndividual;
        delegate = new CodeGenerationHelper(owlOntology, owlIndividual);
    }
 
    /**
     * @return the owlOntology
     */
    public OWLOntology getOwlOntology() {
        return owlOntology;
    }
    
    public OWLNamedIndividual getOwlIndividual() {
		return owlIndividual;
	}
    
    protected CodeGenerationHelper getDelegate() {
		return delegate;
	}
    
    /**
     * Asserts that the individual has a particular OWL type.
     */
    
    public void assertOwlType(OWLClassExpression type) {
        OWLOntologyManager manager = owlOntology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        manager.addAxiom(owlOntology, factory.getOWLClassAssertionAxiom(type, owlIndividual));
    }
    
    /**
     * Deletes the individual from Ontology 
     */
    public void delete() {
        OWLEntityRemover remover = new OWLEntityRemover(getOwlOntology().getOWLOntologyManager(), Collections
                .singleton(getOwlOntology()));
        owlIndividual.accept(remover);
        getOwlOntology().getOWLOntologyManager().applyChanges(remover.getChanges());
    }
    
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof WrappedIndividual)) {
    		return false;
    	}
    	WrappedIndividual other = (WrappedIndividual) obj;
    	return other.getOwlOntology().equals(owlOntology) && other.getOwlIndividual().equals(owlIndividual);
    }
    
    @Override
    public int hashCode() {
    	return owlOntology.hashCode() + 42 * owlIndividual.hashCode();
    }
    
    @Override
    public int compareTo(WrappedIndividual o) {
        return owlIndividual.compareTo(o.getOwlIndividual());
    }
    
    @Override
    public String toString() {
        ShortFormProvider provider = new SimpleShortFormProvider();
        StringBuffer sb = new StringBuffer();
        printTypes(sb, provider);
        sb.append('(');
        printObjectPropertyValues(sb, provider);
        printDataPropertyValues(sb, provider);
        sb.append(')');
        return sb.toString();
    }
    
    private void printTypes(StringBuffer sb, ShortFormProvider provider) {
        Set<OWLClass> types = new TreeSet<OWLClass>();
        for (OWLClassExpression ce : owlIndividual.getTypes(owlOntology)) {
            if (!ce.isAnonymous()) {
                types.add(ce.asOWLClass());
            }
        }
        if (types.size() > 1) {
            sb.append('[');
        }
        else if (types.size() == 0) {
            sb.append("Untyped");
        }
        boolean firstTime = true;
        for (OWLClass type : types) {
            if (firstTime) {
                firstTime = false;
            }
            else {
                sb.append(", ");
            }
            sb.append(provider.getShortForm(type));
        }
        if (types.size() > 1) {
            sb.append(']');
        }
    }
    
    private void printObjectPropertyValues(StringBuffer sb, ShortFormProvider provider) {
        Map<OWLObjectPropertyExpression, Set<OWLIndividual>> valueMap = new TreeMap<OWLObjectPropertyExpression, Set<OWLIndividual>>(owlIndividual.getObjectPropertyValues(owlOntology));
        for (Entry<OWLObjectPropertyExpression, Set<OWLIndividual>> entry : valueMap.entrySet()) {
            OWLObjectPropertyExpression pe = entry.getKey();
            Set<OWLIndividual> values = entry.getValue();
            if (!pe.isAnonymous()) {
                OWLObjectProperty property = pe.asOWLObjectProperty();
                sb.append(provider.getShortForm(property));
                sb.append(": ");
                boolean firstTime = true;
                for (OWLIndividual value : values) {
                    if (!value.isAnonymous()) {
                        if (firstTime) {
                            firstTime = false;
                        }
                        else {
                            sb.append(", ");
                        }
                        sb.append(provider.getShortForm(value.asOWLNamedIndividual()));
                    }
                }
                sb.append("; ");
            }
        }
    }

    private void printDataPropertyValues(StringBuffer sb, ShortFormProvider provider) {
        Map<OWLDataPropertyExpression, Set<OWLLiteral>> valueMap = new TreeMap<OWLDataPropertyExpression, Set<OWLLiteral>>(owlIndividual.getDataPropertyValues(owlOntology));
        for (Entry<OWLDataPropertyExpression, Set<OWLLiteral>> entry : valueMap.entrySet()) {
            OWLDataProperty property = entry.getKey().asOWLDataProperty();
            Set<OWLLiteral> values = entry.getValue();
            sb.append(provider.getShortForm(property));
            sb.append(": ");
            boolean firstTime = true;
            for (OWLLiteral value : values) {
                if (firstTime) {
                    firstTime = false;
                }
                else {
                    sb.append(", ");
                }
                sb.append(value.getLiteral());
            }
            sb.append("; ");
        }
    }

}
