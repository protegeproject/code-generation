package org.protege.owl.codegeneration.property;

import static org.protege.owl.codegeneration.SubstitutionVariable.PROPERTY_RANGE;
import static org.protege.owl.codegeneration.SubstitutionVariable.PROPERTY_RANGE_FOR_CLASS;

import java.util.Map;

import org.protege.owl.codegeneration.Constants;
import org.protege.owl.codegeneration.HandledDatatypes;
import org.protege.owl.codegeneration.SubstitutionVariable;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;

/**
 * This class represents the following java methods that are associated with an OWL dataproperty:
 * <pre>
 *       Collection<? extends ${propertyRangeForClass}> get${OwlProperty}();
 *       boolean has${OwlProperty}();
 *       void add${OwlProperty}(${propertyRange} new${OwlProperty});
 *       void remove${OwlProperty}(${propertyRange} old${OwlProperty});
 * </pre>
 * Note that these methods do get specialized as we move to subclasses.
 * <p/>
 * @author tredmond
 *
 */
public class JavaDataPropertyDeclarations implements JavaPropertyDeclarations {
	private CodeGenerationInference inference;
	private OWLClass owlClass;
	private OWLDataProperty property;
	
	public JavaDataPropertyDeclarations(CodeGenerationInference inference, 
			                            OWLClass owlClass, OWLDataProperty property) {
		this.inference = inference;
		this.owlClass  = owlClass;
		this.property  = property;
	}
	
	public OWLDataProperty getOwlProperty() {
		return property;
	}

	public JavaPropertyDeclarations specializeTo(OWLClass subclass) {
		return new JavaDataPropertyDeclarations(inference, subclass, property);
	}

	public void configureSubstitutions(Map<SubstitutionVariable, String> substitutions) {
        substitutions.put(PROPERTY_RANGE_FOR_CLASS, getDataPropertyRangeForClass());
        substitutions.put(PROPERTY_RANGE, getDataPropertyRange());
	}
	

	private String getDataPropertyRange() {
	    OWLDatatype  dt = inference.getRange(property);
	    return getDataPropertyJavaName(dt);
	}

	private String getDataPropertyRangeForClass() {
	    OWLDatatype  dt = inference.getRange(owlClass, property);
	    return getDataPropertyJavaName(dt);
	}

	/**
	 * @param dataPropertyRange
	 * @param owlDataRanges
	 * @return
	 */
	private String getDataPropertyJavaName(OWLDatatype dt) {
	    String dataPropertyRange = null;
	    if (dt == null) {
	        dataPropertyRange = Constants.UNKNOWN_JAVA_DATA_TYPE;
	    } else {
	        dataPropertyRange = getOwlDataTypeAsJavaClassString(dt);
	    }
	    return dataPropertyRange;
	}
	
	/*
	 * Synchronize this with CodeGeneratorInference implementations.
	 */
	private String getOwlDataTypeAsJavaClassString(OWLDatatype owlDatatype) {
		for (HandledDatatypes handled : HandledDatatypes.values()) {
			if (handled.isMatch(owlDatatype)) {
				return handled.getJavaClass();
			}
		}
		return Constants.UNKNOWN_JAVA_DATA_TYPE;
	}

}
