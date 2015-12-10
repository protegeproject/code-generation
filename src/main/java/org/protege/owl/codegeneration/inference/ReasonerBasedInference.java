package org.protege.owl.codegeneration.inference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.codegeneration.HandledDatatypes;
import org.protege.owl.codegeneration.names.CodeGenerationNames;
import org.protege.owl.codegeneration.property.JavaDataPropertyDeclaration;
import org.protege.owl.codegeneration.property.JavaObjectPropertyDeclaration;
import org.protege.owl.codegeneration.property.JavaPropertyDeclaration;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

public class ReasonerBasedInference implements CodeGenerationInference {
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	private OWLDataFactory factory;
	private Set<OWLClass> allClasses;
	private Map<OWLClass, Set<OWLEntity>> domainMap;
	private Map<OWLClass, Map<OWLObjectProperty, OWLClass>> objectRangeMap = new HashMap<OWLClass, Map<OWLObjectProperty, OWLClass>>();
	private Map<OWLClass, Map<OWLDataProperty, OWLDatatype>> dataRangeMap = new HashMap<OWLClass, Map<OWLDataProperty,OWLDatatype>>();


	public ReasonerBasedInference(OWLOntology ontology, OWLReasoner reasoner) {
		this.ontology = ontology;
		this.reasoner = reasoner;
		factory = ontology.getOWLOntologyManager().getOWLDataFactory();
	}
	
    @Override
	public OWLOntology getOWLOntology() {
		return ontology;
	}
	
    @Override
	public void preCompute() {
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS);
	}
	
    @Override
	public void flush() {
	    reasoner.flush();
	}
	
    @Override
	public Collection<OWLClass> getOwlClasses() {
		if (allClasses == null) {
			allClasses = new HashSet<OWLClass>(ontology.getClassesInSignature());
			allClasses.removeAll(reasoner.getUnsatisfiableClasses().getEntities());
			allClasses.removeAll(reasoner.getEquivalentClasses(factory.getOWLThing()).getEntities());
		}
		return allClasses;
	}
	
    @Override
	public Collection<OWLClass> getSubClasses(OWLClass owlClass) {
		return reasoner.getSubClasses(owlClass, true).getFlattened();
	}
	
    @Override
	public Collection<OWLClass> getSuperClasses(OWLClass owlClass) {
		return reasoner.getSuperClasses(owlClass, true).getFlattened();
	}
	
    @Override
	public Set<JavaPropertyDeclaration> getJavaPropertyDeclarations(OWLClass cls, CodeGenerationNames names) {
		if (domainMap == null) {
			initializeDomainMap();
		}
		Set<JavaPropertyDeclaration> declarations = new HashSet<JavaPropertyDeclaration>();
		if (domainMap.get(cls) != null) {
			for (OWLEntity p : domainMap.get(cls)) {
				if (p instanceof OWLObjectProperty) {
					declarations.add(new JavaObjectPropertyDeclaration(this, names, (OWLObjectProperty) p));
				}
				else if (p instanceof OWLDataProperty) {
					declarations.add(new JavaDataPropertyDeclaration(this, cls, (OWLDataProperty) p));
				}
			}
		}
		return declarations;
	}
    
	@Override
	public boolean isFunctional(OWLObjectProperty p) {
		OWLClassExpression moreThanTwoValues = factory.getOWLObjectMinCardinality(2, p);
		return !reasoner.isSatisfiable(moreThanTwoValues);
	}

	@Override
	public OWLClass getRange(OWLObjectProperty p) {
		return getRange(factory.getOWLThing(), p);
	}
	
    @Override
	public OWLClass getRange(OWLClass owlClass, OWLObjectProperty p) {
		Map<OWLObjectProperty, OWLClass> property2RangeMap = objectRangeMap.get(owlClass);
		if (property2RangeMap == null) {
			property2RangeMap = new HashMap<OWLObjectProperty, OWLClass>();
			objectRangeMap.put(owlClass, property2RangeMap);
		}
		OWLClass cls = property2RangeMap.get(p);
		if (cls == null) {
			OWLClassExpression possibleValues = factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectInverseOf(p), owlClass);
			Collection<OWLClass> classes;
			classes = reasoner.getEquivalentClasses(possibleValues).getEntities();
			if (classes != null && !classes.isEmpty()) {
				cls =  asSingleton(classes, ontology);
			}
			else {
				classes = reasoner.getSuperClasses(possibleValues, true).getFlattened();
				cls = asSingleton(classes, ontology);
			}
			property2RangeMap.put(p, cls);
		}
		return cls;
	}
	
    @Override
	public boolean isFunctional(OWLDataProperty p) {
		 OWLClassExpression moreThanTwoValues = factory.getOWLDataMinCardinality(2, p);
		 return !reasoner.isSatisfiable(moreThanTwoValues);
	}

	@Override
	public OWLDatatype getRange(OWLDataProperty p) {
		return getRange(factory.getOWLThing(), p);
	}
	
    @Override
	public OWLDatatype getRange(OWLClass owlClass, OWLDataProperty p) {
		Map<OWLDataProperty, OWLDatatype> property2RangeMap = dataRangeMap.get(owlClass);
		if (property2RangeMap == null) {
			property2RangeMap = new HashMap<OWLDataProperty, OWLDatatype>();
			dataRangeMap.put(owlClass, property2RangeMap);
		}
		OWLDatatype range = property2RangeMap.get(p);
		if (range == null) {
            for (HandledDatatypes handled : HandledDatatypes.values()) {
            	OWLDatatype dt = factory.getOWLDatatype(handled.getIri());
                OWLClassExpression couldHaveOtherValues = factory.getOWLObjectComplementOf(factory.getOWLDataAllValuesFrom(p, dt));
                OWLClassExpression classCouldHaveOtherValues = factory.getOWLObjectIntersectionOf(owlClass, couldHaveOtherValues);
                if (!reasoner.isSatisfiable(classCouldHaveOtherValues)) {
                	range = dt;
                	break;
                }
            }
            if (range != null) {
            	property2RangeMap.put(p, range);
            }
		}
		return range;
	}

    @Override
    public Collection<OWLNamedIndividual> getIndividuals(OWLClass owlClass) {
		return reasoner.getInstances(owlClass, false).getFlattened();
	}

    @Override
	public boolean canAs(OWLNamedIndividual i, OWLClass c) {
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		return reasoner.isSatisfiable(factory.getOWLObjectIntersectionOf(c, factory.getOWLObjectOneOf(i)));
	}

	@Override
	public Collection<OWLClass> getTypes(OWLNamedIndividual i) {
		return reasoner.getTypes(i, true).getFlattened();
	}
	
	@Override
	public Collection<OWLNamedIndividual> getPropertyValues(OWLNamedIndividual i, OWLObjectProperty p) {
	    return reasoner.getObjectPropertyValues(i, p).getFlattened();
	}
	
	@Override
	public Collection<OWLLiteral> getPropertyValues(OWLNamedIndividual i, OWLDataProperty p) {
        Set<OWLLiteral> results = new HashSet<OWLLiteral>();
        results.addAll(reasoner.getDataPropertyValues(i, p));
        // the behavior of getDataPropertyValues is somewhat undefined
        // so make sure that the asserted ones are included.
        results.addAll(EntitySearcher.getDataPropertyValues(i, p, ontology.getImportsClosure()));
        return results;
	}

	/* *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
	 * 
	 */
	private static <X extends OWLEntity> X asSingleton(Collection<X> xs, OWLOntology owlOntology) {
		X result = null;
		for (X x : xs) {
			if (owlOntology.containsEntityInSignature(x, true)) {
				if (result == null) {
					result = x;
				}
				else {
					return null;
				}
			}
		}
		return result;
	}

	private void initializeDomainMap() {
		domainMap = new HashMap<OWLClass, Set<OWLEntity>>();
		for (OWLObjectProperty p : ontology.getObjectPropertiesInSignature()) {
			OWLClassExpression mustHavePropertyValue = factory.getOWLObjectSomeValuesFrom(p, factory.getOWLThing());
			addPropertyToDomainMap(p, mustHavePropertyValue);
		}
		for (OWLDataProperty p : ontology.getDataPropertiesInSignature()) {
			OWLClassExpression mustHavePropertyValue = factory.getOWLDataSomeValuesFrom(p, factory.getTopDatatype());
			addPropertyToDomainMap(p, mustHavePropertyValue);
		}
	}
	
	private void addPropertyToDomainMap(OWLEntity p, OWLClassExpression mustHavePropertyValue) {
		Set<OWLClass> equivalents = reasoner.getEquivalentClasses(mustHavePropertyValue).getEntities();
		if (!equivalents.isEmpty()) {
			for (OWLClass domain : equivalents) {
				addToDomainMap(domain, p);
			}
		}
		else {
			for (OWLClass domain : reasoner.getSuperClasses(mustHavePropertyValue, true).getFlattened()) {
				addToDomainMap(domain, p);
			}
		}
	}
	
	private void addToDomainMap(OWLClass domain, OWLEntity property) {
		Set<OWLEntity> properties = domainMap.get(domain);
		if (properties == null) {
			properties = new TreeSet<OWLEntity>();
			domainMap.put(domain, properties);
		}
		properties.add(property);
	}

}
