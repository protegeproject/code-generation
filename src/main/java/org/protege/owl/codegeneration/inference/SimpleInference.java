package org.protege.owl.codegeneration.inference;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitorEx;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class SimpleInference implements CodeGenerationInference {
	private OWLOntology ontology;
	private Set<OWLObjectProperty> objectProperties;
	private Set<OWLDataProperty> dataProperties;
	
	public SimpleInference(OWLOntology ontology) {
		this.ontology = ontology;
		objectProperties = new HashSet<OWLObjectProperty>();
		dataProperties = new HashSet<OWLDataProperty>();
		for (OWLOntology inImportsClosure : ontology.getImportsClosure()) {
			objectProperties.addAll(inImportsClosure.getObjectPropertiesInSignature());
			dataProperties.addAll(inImportsClosure.getDataPropertiesInSignature());
		}
	}
	
	public Collection<OWLClass> getOwlClasses() {
		return ontology.getClassesInSignature(true);
	}
	
	public Collection<OWLNamedIndividual> getIndividuals(OWLClass owlClass) {
		Collection<OWLNamedIndividual> individuals = new HashSet<OWLNamedIndividual>();
		for (OWLIndividual individual : owlClass.getIndividuals(ontology.getImportsClosure())) {
			if (individual.isNamed()) {
				individuals.add(individual.asOWLNamedIndividual());
			}
		}
		return individuals;
	}
	
	public boolean canAs(OWLNamedIndividual i, OWLClass c) {
		return i.getTypes(ontology.getImportsClosure()).contains(c);
	}
	
	public Collection<OWLClass> getSuperClasses(OWLClass owlClass) {
		return getSubCollection(owlClass.getSuperClasses(ontology.getImportsClosure()), OWLClass.class);
	}
	
	public Collection<OWLClass> getTypes(OWLNamedIndividual i) {
		return getSubCollection(i.getTypes(ontology.getImportsClosure()), OWLClass.class);
	}
	
	public Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass cls) {
		Set<OWLObjectProperty> propertiesForClass = new HashSet<OWLObjectProperty>();
		for (OWLObjectProperty property : objectProperties) {
			if (property.getDomains(ontology.getImportsClosure()).contains(cls)) {
				propertiesForClass.add(property);
			}
		}
		return propertiesForClass;
	}
	
	public OWLClass getRange(OWLObjectProperty p) {
		return asSingleton(getSubCollection(p.getRanges(ontology.getImportsClosure()), OWLClass.class), ontology);
	}
	
	/*
	 * TODO - examine restrictions
	 */
	public OWLClass getRange(OWLClass c, OWLObjectProperty p) {
		return getRange(p);
	}
	
	public Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass cls) {
		Set<OWLDataProperty> propertiesForClass = new HashSet<OWLDataProperty>();
		for (OWLDataProperty property : dataProperties) {
			if (property.getDomains(ontology.getImportsClosure()).contains(cls)) {
				propertiesForClass.add(property);
			}
		}
		return propertiesForClass;
	}
	

	public OWLDatatype getRange(OWLDataProperty p) {
		for (OWLDataRange range : p.getRanges(ontology.getImportsClosure())) {
			OWLDatatype type = range.accept(new RangeAsDatatypeVisitor());
			if (type != null) {
				return type;
			}
		}
		return null;
	}
	
	/*
	 * TODO - examine restrictions
	 */
	public OWLDatatype getRange(OWLClass c, OWLDataProperty p) {
		return getRange(p);
	}
	
	private static <Y, X extends Y> Collection<X> getSubCollection(Collection<Y> collection, Class<? extends X> xClass) {
		Collection<X> subCollection = new HashSet<X>();
		for (Y y : collection) {
			if (xClass.isAssignableFrom(y.getClass())) {
				subCollection.add(xClass.cast(y));
			}
		}
		return subCollection;
	}
	
	/* package */ static <X extends OWLEntity> X asSingleton(Collection<X> xs, OWLOntology owlOntology) {
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
	
	private static class RangeAsDatatypeVisitor implements OWLDataRangeVisitorEx<OWLDatatype> {

		
		public OWLDatatype visit(OWLDatatype node) {
			return node;
		}

		
		public OWLDatatype visit(OWLDataOneOf node) {
			OWLDatatype type = null;
			for (OWLLiteral literal : node.getValues()) {
				if (type == null) {
					type = literal.getDatatype();
				}
				else if (!type.equals(literal.getDatatype())){
					return null;
				}
			}
			return type;
		}

		
		public OWLDatatype visit(OWLDataComplementOf node) {
			return null;
		}

		
		public OWLDatatype visit(OWLDataIntersectionOf node) {
			return node.getOperands().iterator().next().accept(this);
		}

		
		public OWLDatatype visit(OWLDataUnionOf node) {
			OWLDatatype type = null;
			for (OWLDataRange range : node.getOperands()) {
				OWLDatatype otherType = range.accept(this);
				if (type == null) {
					type = otherType;
				}
				else if (!type.equals(otherType)){
					return null;
				}
			}
			return type;
		}

		
		public OWLDatatype visit(OWLDatatypeRestriction node) {
			return node.getDatatype();
		}
		
	}

}
