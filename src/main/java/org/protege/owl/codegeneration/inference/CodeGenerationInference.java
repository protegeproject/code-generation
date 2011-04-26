package org.protege.owl.codegeneration.inference;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public interface CodeGenerationInference {

	Collection<OWLClass> getOwlClasses();
	
	Collection<OWLClass> getSuperClasses(OWLClass owlClass);
	
	Collection<OWLNamedIndividual> getIndividuals(OWLClass owlClass);
	
	boolean canAs(OWLNamedIndividual i, OWLClass c);
		
	Collection<OWLClass> getTypes(OWLNamedIndividual i);
	
	/**
	 * This method must satisfy two conditions:
	 * <ul>
	 * <li> For any named object property p and any named class C, if C&#8745;&#8707;p.Thing 
	 *      is satisfiable then p must be a member of 
	 *      getObjectPropertiesForClass(C).</li>
	 * <li> For any named class C and any named class D, if it is known that C is a subclass of D then
	 *      getObjectPropertiesForClass(C) must be a superset of getObjectPropertiesForClass(D).</li>
	 * </ul>
	 * <br/>
	 * These are an artificial pair of constraints that result from the mismatch of java and OWL.
	 * 
	 * @param cls
	 * @return
	 */
	Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass cls);
		
	OWLClass getRange(OWLObjectProperty p);
	
	/**
	 * This method must satisfy two conditions:
	 * <ul>
	 * <li> For any named data property p and any named class C, if C&#8745;&#8707;p.Literal 
	 *      is satisfiable then p must be a member of 
	 *      getDataPropertiesForClass(C).</li>
	 * <li> For any named class C and any named class D, if it is known that C is a subclass of D then
	 *      getDataPropertiesForClass(C) must be a superset of getDataPropertiesForClass(D).</li>
	 * </ul>
	 * <br/>
	 * These are an artificial pair of constraints that result from the mismatch of java and OWL.
	 * 
	 * @param cls
	 * @return
	 */
	Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass cls);
		
	OWLDatatype getRange(OWLDataProperty p);
}
