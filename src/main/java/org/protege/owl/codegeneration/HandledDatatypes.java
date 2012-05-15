package org.protege.owl.codegeneration;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

public enum HandledDatatypes {
	STRING(XSDVocabulary.STRING.getIRI(), "String", "String") {
		
		public Object getObject(OWLLiteral literal) {
			return literal.getLiteral();
		}
		
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof String) {
				return factory.getOWLLiteral((String) o);
			}
			else {
				return null;
			}
		}
	},
	FLOAT(XSDVocabulary.FLOAT.getIRI(), "float", "Float") {
	
		public Object getObject(OWLLiteral literal) {
			return Float.parseFloat(literal.getLiteral());
		}
		
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Float) {
				return factory.getOWLLiteral((Float) o);
			}
			else {
				return null;
			}
		}
	},
	BOOLEAN(XSDVocabulary.BOOLEAN.getIRI(), "boolean", "Boolean") {
		
		public Object getObject(OWLLiteral literal) {
			return Boolean.parseBoolean(literal.getLiteral());
		}
		
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Boolean) {
				return factory.getOWLLiteral((Boolean) o);
			}
			else {
				return null;
			}
		}
	},
	INTEGER(XSDVocabulary.INTEGER.getIRI(), "int", "Integer") {
		
		public Object getObject(OWLLiteral literal) {
			return Integer.parseInt(literal.getLiteral());
		}
		
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Integer) {
				return factory.getOWLLiteral((Integer) o);
			}
			else {
				return null;
			}
		}
		
		@Override
		public boolean isMatch(OWLDatatype dt) {
			return dt.getIRI().equals(XSDVocabulary.INT.getIRI()) || dt.getIRI().equals(XSDVocabulary.INTEGER.getIRI());
		}
	},
	LONG(XSDVocabulary.LONG.getIRI(), "long", "Long") {
		
		public Object getObject(OWLLiteral literal) {
			return Long.parseLong(literal.getLiteral());
		}
		
		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof Long) {
				return factory.getOWLLiteral(o.toString(), OWL2Datatype.XSD_LONG);
			}
			else {
				return null;
			}
		}
		
		@Override
		public boolean isMatch(OWLDatatype dt) {
			return dt.getIRI().equals(XSDVocabulary.LONG.getIRI());
		}
	}
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
	
	public abstract Object getObject(OWLLiteral literal);
	
	public abstract OWLLiteral getLiteral(OWLDataFactory factory, Object o);
}
