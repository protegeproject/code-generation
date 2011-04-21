package org.protege.owl.codegeneration;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

public enum HandledDatatypes {
	STRING(XSDVocabulary.STRING.getIRI(), "String", "String"),
	FLOAT(XSDVocabulary.FLOAT.getIRI(), "float", "Float"),
	BOOLEAN(XSDVocabulary.BOOLEAN.getIRI(), "boolean", "Boolean"),
	INTEGER(XSDVocabulary.INTEGER.getIRI(), "int", "Integer")
	;
	
	private IRI iri;
	private String javaType;
	private String javaClass;
	
	private HandledDatatypes(IRI iri, String javaType, String javaClass) {
		this.iri = iri;
		this.javaType = javaType;
		this.javaClass = javaClass;
	}
	
	public IRI getIri() {
		return iri;
	}
	
	public String getJavaType() {
		return javaType;
	}
	
	public String getJavaClass() {
		return javaClass;
	}

	public boolean isMatch(OWLDatatype dt) {
		return dt.getIRI().equals(iri);
	}
}
