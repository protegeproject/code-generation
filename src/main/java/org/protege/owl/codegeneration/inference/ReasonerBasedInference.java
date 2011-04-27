package org.protege.owl.codegeneration.inference;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.codegeneration.HandledDatatypes;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class ReasonerBasedInference implements CodeGenerationInference {
	public static final Logger LOGGER = Logger.getLogger(ReasonerBasedInference.class);
	
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	private OWLDataFactory factory;
	private Map<OWLClass, Set<OWLObjectProperty>> class2ObjectPropertyMap  = new HashMap<OWLClass, Set<OWLObjectProperty>>();
	private Map<OWLClass, Set<OWLDataProperty>>   class2DataPropertyMap    = new HashMap<OWLClass, Set<OWLDataProperty>>();
	private Map<OWLObjectProperty, OWLClass> objectRangeMap = new HashMap<OWLObjectProperty, OWLClass>();
	private Map<OWLDataProperty, OWLDatatype> dataRangeMap = new HashMap<OWLDataProperty,OWLDatatype>();

	public ReasonerBasedInference(OWLOntology ontology, OWLReasoner reasoner) {
		this.ontology = ontology;
		this.reasoner = reasoner;
		factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		analyzeProperties();
	}
	
	private void analyzeProperties() {
		Set<OWLObjectProperty> objectProperties = ontology.getObjectPropertiesInSignature(true);
		Set<OWLDataProperty> dataProperties = ontology.getDataPropertiesInSignature(true);

        analyzeClassObjectPropertyAssociations(objectProperties);
        analyzeClassDataPropertyAssociations(dataProperties);
        analyzeObjectPropertyRanges(objectProperties);
        analyzeDataPropertyRanges(dataProperties); 
	}

    private void analyzeClassObjectPropertyAssociations(Set<OWLObjectProperty> objectProperties) {
		LOGGER.info("Calculating class/object property associations...");
		long startTime = System.currentTimeMillis();
		for (OWLObjectProperty p : objectProperties) {
			OWLClassExpression someCE = factory.getOWLObjectSomeValuesFrom(p, factory.getOWLThing());
			for (OWLClass superClass : reasoner.getSuperClasses(someCE, true).getFlattened()) {
				addToMap(class2ObjectPropertyMap, superClass, p);
				for (OWLClass subSuperClass : reasoner.getSubClasses(superClass, false).getFlattened()) {
					addToMap(class2ObjectPropertyMap, subSuperClass, p);
				}
			}
		}
		LOGGER.info("Took " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    private void analyzeClassDataPropertyAssociations(Set<OWLDataProperty> dataProperties) {
		LOGGER.info("Calculating class/data property associations...");
		long startTime = System.currentTimeMillis();
		for (OWLDataProperty p : dataProperties) {
			OWLClassExpression someCE = factory.getOWLDataSomeValuesFrom(p, factory.getTopDatatype());
			for (OWLClass superClass : reasoner.getSuperClasses(someCE, true).getFlattened()) {
				addToMap(class2DataPropertyMap, superClass, p);
				for (OWLClass subSuperClass : reasoner.getSubClasses(superClass, false).getFlattened()) {
					addToMap(class2DataPropertyMap, subSuperClass, p);
				}
			}
		}
		LOGGER.info("Took " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    private void analyzeObjectPropertyRanges(Set<OWLObjectProperty> objectProperties) {
		LOGGER.info("Calculating object property ranges...");
		long startTime = System.currentTimeMillis();
		for (OWLObjectProperty p : objectProperties) {
			OWLClassExpression possibleValues = factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectInverseOf(p), factory.getOWLThing());
			Collection<OWLClass> classes;
			classes = reasoner.getEquivalentClasses(possibleValues).getEntities();
			if (classes != null && !classes.isEmpty()) {
				objectRangeMap.put(p, SimpleInference.asSingleton(classes, ontology));
			}
			else {
				classes = reasoner.getSuperClasses(possibleValues, true).getFlattened();
				objectRangeMap.put(p, SimpleInference.asSingleton(classes, ontology));
			}
		}
		LOGGER.info("Took " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    private void analyzeDataPropertyRanges(Set<OWLDataProperty> dataProperties) {
		LOGGER.info("Calculating object property ranges...");
		long startTime = System.currentTimeMillis();
        for (OWLDataProperty p : dataProperties) {
    		OWLDatatype range = null;
            for (HandledDatatypes handled : HandledDatatypes.values()) {
            	OWLDatatype dt = factory.getOWLDatatype(handled.getIri());
                OWLClassExpression couldHaveOtherValues = factory.getOWLObjectComplementOf(factory.getOWLDataAllValuesFrom(p, dt));
                if (!reasoner.isSatisfiable(couldHaveOtherValues)) {
                	range = dt;
                	break;
                }
            }
            if (range != null) {
            	dataRangeMap.put(p, range);
            }
        }        
		LOGGER.info("Took " + (System.currentTimeMillis() - startTime) + "ms.");
    }
	
	private <X, Y> void addToMap(Map<X, Set<Y>> map, X x, Y y) {
		Set<Y> ys = map.get(x);
		if (ys == null) {
			ys = new HashSet<Y>();
			map.put(x, ys);
		}
		ys.add(y);
	}
	
	public Collection<OWLClass> getOwlClasses() {
		Set<OWLClass> classes = new HashSet<OWLClass>(ontology.getClassesInSignature());
		classes.removeAll(reasoner.getUnsatisfiableClasses().getEntities());
		return classes;
	}
	
	public Collection<OWLNamedIndividual> getIndividuals(OWLClass owlClass) {
		return reasoner.getInstances(owlClass, false).getFlattened();
	}
	
	public boolean canAs(OWLNamedIndividual i, OWLClass c) {
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		return reasoner.isSatisfiable(factory.getOWLObjectIntersectionOf(c, factory.getOWLObjectOneOf(i)));
	}
	
	public Collection<OWLClass> getSuperClasses(OWLClass owlClass) {
		return reasoner.getSuperClasses(owlClass, true).getFlattened();
	}
	
	public Collection<OWLClass> getTypes(OWLNamedIndividual i) {
		return reasoner.getTypes(i, true).getFlattened();
	}

	public Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass cls) {
		Set<OWLObjectProperty> properties = class2ObjectPropertyMap.get(cls);
		if (properties == null) {
			return Collections.emptySet();
		}
		else {
			return Collections.unmodifiableSet(properties);
		}
	}
	
	public OWLClass getRange(OWLObjectProperty p) {
		return objectRangeMap.get(p);
	}
	
	public OWLClass getRange(OWLClass cls, OWLObjectProperty p) {
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLClassExpression values = factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectInverseOf(p), cls);
		return SimpleInference.asSingleton(reasoner.getSuperClasses(values, true).getFlattened(), ontology);
	}
	
	public Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass cls) {
		Set<OWLDataProperty> properties = class2DataPropertyMap.get(cls);
		if (properties == null) {
			return Collections.emptySet();
		}
		else {
			return Collections.unmodifiableSet(properties);
		}
	}
	
	public OWLDatatype getRange(OWLDataProperty p) {
		return dataRangeMap.get(p);
	}
	
	public OWLDatatype getRange(OWLClass cls, OWLDataProperty p) {
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		for (HandledDatatypes handled : HandledDatatypes.values()) {
			OWLDatatype dt = factory.getOWLDatatype(handled.getIri());
			OWLClassExpression hasValueOfSomeOtherType 
			              = factory.getOWLObjectIntersectionOf(
			            		            cls,
											factory.getOWLObjectComplementOf(factory.getOWLDataAllValuesFrom(p, dt))
											);
			if (!reasoner.isSatisfiable(hasValueOfSomeOtherType)) {
				return dt;
			}
		}
		return null;
	}


}
