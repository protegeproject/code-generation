package org.protege.owl.codegeneration.inference;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.codegeneration.names.CodeGenerationNames;
import org.protege.owl.codegeneration.property.JavaDataPropertyDeclaration;
import org.protege.owl.codegeneration.property.JavaObjectPropertyDeclaration;
import org.protege.owl.codegeneration.property.JavaPropertyDeclaration;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

public class SimpleInference implements CodeGenerationInference {
	private OWLOntology ontology;
	private OWLDataFactory factory;
	private Set<OWLClass> topLevelClasses;
	private Map<OWLClass, Set<OWLClass>> inferredSubclassMap = new TreeMap<OWLClass, Set<OWLClass>>();
	private Map<OWLClass, Set<OWLClass>> indirectSuperclassMap = new HashMap<OWLClass, Set<OWLClass>>();
	private Map<OWLClass, Set<OWLEntity>> domainMap;
	private Map<OWLObjectProperty, OWLClass> objectRangeMap;
	private Map<OWLDataProperty, OWLDatatype> dataRangeMap;
	
	public SimpleInference(OWLOntology ontology) {
		this.ontology = ontology;
		factory = ontology.getOWLOntologyManager().getOWLDataFactory();
	}
	
	public OWLOntology getOWLOntology() {
		return ontology;
	}
	
	public void preCompute() {
		;
	}
	
	public void flush() {
	    ;
	}
	
	public Collection<OWLClass> getOwlClasses() {
		Set<OWLClass> classes = new HashSet<OWLClass>(ontology.getClassesInSignature());
		classes.remove(factory.getOWLThing());
		return classes;
	}
	
	public Collection<OWLClass> getSubClasses(OWLClass owlClass) {
		if (topLevelClasses == null) {
			initializeInferredSubclasses();
		}
		if (owlClass.equals(factory.getOWLThing())) {
			return Collections.unmodifiableCollection(topLevelClasses);
		}
		else {
			Set<OWLClass> subClasses = new TreeSet<OWLClass>();
			for (OWLClassExpression ce : EntitySearcher.getSubClasses(owlClass, ontology)) {
				if (!ce.isAnonymous()) {
					subClasses.add(ce.asOWLClass());
				}
			}
			Set<OWLClass> inferredSubclasses = inferredSubclassMap.get(owlClass);
			if (inferredSubclasses != null) {
				subClasses.addAll(inferredSubclasses);
			}
			return subClasses;
		}
	}
	
	public Collection<OWLClass> getSuperClasses(OWLClass owlClass) {
		Set<OWLClass> superClasses = new HashSet<OWLClass>();
		for (OWLClassExpression ce : EntitySearcher.getSuperClasses(owlClass, ontology.getImportsClosure())) {
			if (!ce.isAnonymous()) {
				superClasses.add(ce.asOWLClass());
			}
			else if (ce instanceof OWLObjectIntersectionOf) {
			    superClasses.addAll(getNamedConjuncts((OWLObjectIntersectionOf) ce));
			}
		}
		for (OWLClassExpression ce : EntitySearcher.getEquivalentClasses(owlClass, ontology.getImportsClosure())) {
		    if (ce instanceof OWLObjectIntersectionOf) {
                superClasses.addAll(getNamedConjuncts((OWLObjectIntersectionOf) ce));
            }
		}
		superClasses.remove(factory.getOWLThing());
		return superClasses;
	}
	
	private Collection<OWLClass> getNamedConjuncts(OWLObjectIntersectionOf ce) {
	    Set<OWLClass> conjuncts = new HashSet<OWLClass>();
	    for (OWLClassExpression conjunct : ce.getOperands()) {
	        if (!conjunct.isAnonymous()) {
	            conjuncts.add(conjunct.asOWLClass());
	        }
	    }
	    return conjuncts;
	}
	
	@Override
	public Collection<OWLNamedIndividual> getPropertyValues(OWLNamedIndividual i, OWLObjectProperty p) {
	    Collection<OWLNamedIndividual> results = new HashSet<OWLNamedIndividual>();
	    for (OWLOntology imported : ontology.getImportsClosure()) {
	        for (OWLIndividual j : EntitySearcher.getObjectPropertyValues(i, p, imported)) {
	            if (!j.isAnonymous()) {
	                results.add(j.asOWLNamedIndividual());
	            }
	        }
	    }
	    return results;
	}
	
	@Override
	public Collection<OWLLiteral> getPropertyValues(OWLNamedIndividual i, OWLDataProperty p) {
        Set<OWLLiteral> results = new HashSet<OWLLiteral>();
        for (OWLOntology imported : ontology.getImportsClosure()) {
            results.addAll(EntitySearcher.getDataPropertyValues(i, p, imported));
        }
        return results;
	}
	
	public Set<JavaPropertyDeclaration> getJavaPropertyDeclarations(OWLClass cls, CodeGenerationNames names) {
		if (domainMap == null) {
			initializeDomainMap();
		}
		Set<JavaPropertyDeclaration> declarations = new HashSet<JavaPropertyDeclaration>();
		Set<OWLEntity> domains = domainMap.get(cls);
		if (domains != null) {
			for (OWLEntity property : domains) {
				if (property instanceof OWLObjectProperty) {
					declarations.add(new JavaObjectPropertyDeclaration(this, names, (OWLObjectProperty) property));
				}
				else {
					declarations.add(new JavaDataPropertyDeclaration(this, cls, (OWLDataProperty) property));
				}
			}
		}
		return declarations;
	}
	
	@Override
	public boolean isFunctional(OWLObjectProperty p) {
		OWLAxiom functionalAxiom = factory.getOWLFunctionalObjectPropertyAxiom(p);
		return ontology.containsAxiomIgnoreAnnotations(functionalAxiom);
	}
	
	@Override
	public OWLClass getRange(OWLObjectProperty p) {
		if (objectRangeMap == null) {
			intializeObjectRangeMap();
		}
		return objectRangeMap.get(p);
	}
	
	@Override
	public OWLClass getRange(OWLClass owlClass, OWLObjectProperty p) {
		return getRange(p);
	}
	
	@Override
	public boolean isFunctional(OWLDataProperty p) {
		OWLAxiom functionalAxiom = factory.getOWLFunctionalDataPropertyAxiom(p);
		return ontology.containsAxiomIgnoreAnnotations(functionalAxiom);		
	}

	public OWLDatatype getRange(OWLDataProperty p) {
		if (dataRangeMap == null) {
			intializeDataRangeMap();
		}
		return dataRangeMap.get(p);
	}
	
	public OWLDatatype getRange(OWLClass owlClass, OWLDataProperty p) {
		return getRange(p);
	}
	
	public Collection<OWLNamedIndividual> getIndividuals(OWLClass owlClass) {
		Set<OWLNamedIndividual> individuals = new HashSet<OWLNamedIndividual>();
		for (OWLIndividual i : EntitySearcher.getIndividuals(owlClass, ontology)) {
			if (!i.isAnonymous()) {
				individuals.add(i.asOWLNamedIndividual());
			}
		}
		return individuals;
	}
	
	public boolean canAs(OWLNamedIndividual i, OWLClass c) {
	    Collection<OWLClass> types = getTypes(i);
	    if (types.contains(c)) {
	        return true;
	    }
	    for (OWLClass type : types) {
	        if (getIndirectSuperClasses(type).contains(c)) {
	            return true;
	        }
	    }
		return false;
	}
	
	public Collection<OWLClass> getTypes(OWLNamedIndividual i) {
		Set<OWLClass> types = new HashSet<OWLClass>();
		for (OWLClassExpression ce : EntitySearcher.getTypes(i, ontology.getImportsClosure())) {
			if (!ce.isAnonymous()) {
				types.add(ce.asOWLClass());
			}
		}
		return types;
	}
	
	/* *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
	 * 
	 */
	
	private void initializeInferredSubclasses() {
		topLevelClasses = new TreeSet<OWLClass>();
		for (OWLClass owlClass : ontology.getClassesInSignature()) {
			boolean foundParent = false;
			for (OWLClassExpression parent : EntitySearcher.getSuperClasses(owlClass, ontology)) {
				if (hasGoodDirectSuperClass(owlClass, parent)
						|| searchForSuperclassesFromIntersection(owlClass, parent)) {
					foundParent = true;
				}
			}
			for (OWLClassExpression parent : EntitySearcher.getEquivalentClasses(owlClass, ontology)) {
				if (searchForSuperclassesFromIntersection(owlClass, parent)) {
					foundParent = true;
				}
			}			
			if (!foundParent) {
				topLevelClasses.add(owlClass);
			}
		}
	}
	
	private boolean hasGoodDirectSuperClass(OWLClass child, OWLClassExpression parent) {
		return !parent.isAnonymous() && !parent.equals(factory.getOWLThing());
	}

	private boolean searchForSuperclassesFromIntersection(OWLClass child, OWLClassExpression parent) {
		if (parent instanceof OWLObjectIntersectionOf) {
			for (OWLClassExpression conjunct : ((OWLObjectIntersectionOf) parent).getOperands()) {
				if (!conjunct.isAnonymous() && !conjunct.equals(factory.getOWLThing())) {
					Set<OWLClass> inferredSubclasses = inferredSubclassMap.get(conjunct);
					if (inferredSubclasses == null) {
						inferredSubclasses = new TreeSet<OWLClass>();
						inferredSubclassMap.put(conjunct.asOWLClass(), inferredSubclasses);
					}
					inferredSubclasses.add(child);
					return true;
				}
			}
		}
		return false;
	}
	
	private void initializeDomainMap() {
		domainMap = new HashMap<OWLClass, Set<OWLEntity>>();
		for (OWLObjectPropertyDomainAxiom axiom : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
			if (!axiom.getDomain().isAnonymous() && !axiom.getProperty().isAnonymous()) {
				OWLClass owlClass = axiom.getDomain().asOWLClass();
				Set<OWLEntity> domains = domainMap.get(owlClass);
				if (domains == null) {
					domains = new HashSet<OWLEntity>();
					domainMap.put(owlClass, domains);
				}
				domains.add(axiom.getProperty().asOWLObjectProperty());
			}
		}
		for (OWLDataPropertyDomainAxiom axiom : ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
			if (!axiom.getDomain().isAnonymous() && !axiom.getProperty().isAnonymous()) {
				OWLClass owlClass = axiom.getDomain().asOWLClass();
				Set<OWLEntity> domains = domainMap.get(owlClass);
				if (domains == null) {
					domains = new HashSet<OWLEntity>();
					domainMap.put(owlClass, domains);
				}
				domains.add(axiom.getProperty().asOWLDataProperty());
			}
		}
	}
	
	private void intializeObjectRangeMap() {
		objectRangeMap = new HashMap<OWLObjectProperty, OWLClass>();
		for (OWLObjectPropertyRangeAxiom axiom : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE)) {
			if (!axiom.getRange().isAnonymous() && !axiom.getProperty().isAnonymous()) {
				OWLObjectProperty property = axiom.getProperty().asOWLObjectProperty();
				if (objectRangeMap.get(property) == null) {
					objectRangeMap.put(property, axiom.getRange().asOWLClass());
				}
			}
		}
	}
	
	private void intializeDataRangeMap() {
		dataRangeMap = new HashMap<OWLDataProperty, OWLDatatype>();
		for (OWLDataPropertyRangeAxiom axiom : ontology.getAxioms(AxiomType.DATA_PROPERTY_RANGE)) {
			if (!axiom.getProperty().isAnonymous()) {
				OWLDataProperty property = axiom.getProperty().asOWLDataProperty();
				OWLDatatype dt = getContainingDatatype(axiom.getRange());
				if (dataRangeMap.get(property) == null && dt != null) {
					dataRangeMap.put(property, dt);
				}
			}
		}
	}
	
	private OWLDatatype getContainingDatatype(OWLDataRange range) {
		if (range instanceof OWLDatatype) {
			return (OWLDatatype) range;
		}
		else if (range instanceof OWLDatatypeRestriction) {
			return ((OWLDatatypeRestriction) range).getDatatype();
		}
		return null;
	}
	
	private Set<OWLClass> getIndirectSuperClasses(OWLClass cls) {
	    Set<OWLClass> superClasses = indirectSuperclassMap.get(cls);
	    if (superClasses == null) {
	        superClasses = new HashSet<OWLClass>();
	        addIndirectSuperClasses(superClasses, cls);
	        indirectSuperclassMap.put(cls, superClasses);
	    }
	    return superClasses;
	}
	
	private void addIndirectSuperClasses(Set<OWLClass> superClasses, OWLClass cls) {
	    Collection<OWLClass> newSuperClasses = getSuperClasses(cls);
	    newSuperClasses.removeAll(superClasses);
	    superClasses.addAll(newSuperClasses);
	    for (OWLClass newSuperClass : newSuperClasses) {
	        addIndirectSuperClasses(superClasses, newSuperClass);
	    }
	}
	
}
