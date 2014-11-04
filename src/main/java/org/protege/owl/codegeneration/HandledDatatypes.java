package org.protege.owl.codegeneration;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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
	},
	URI(XSDVocabulary.ANY_URI.getIRI(), "URI", "URI") {

		public Object getObject(OWLLiteral literal) {
			return java.net.URI.create(literal.getLiteral());
		}

		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof java.net.URI) {
				return factory.getOWLLiteral(o.toString(), OWL2Datatype.XSD_ANY_URI);
			}
			else {
				return null;
			}
		}

		@Override
		public boolean isMatch(OWLDatatype dt) {
			return dt.getIRI().equals(XSDVocabulary.ANY_URI.getIRI());
		}
	},
	DATE_TIME(XSDVocabulary.DATE_TIME.getIRI(), "XMLGregorianCalendar", "XMLGregorianCalendar") {

		public Object getObject(OWLLiteral literal) {
			try {
				return DatatypeFactory.newInstance().newXMLGregorianCalendar(literal.getLiteral());
			} catch (DatatypeConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		public OWLLiteral getLiteral(OWLDataFactory factory, Object o) {
			if (o instanceof XMLGregorianCalendar) {
				return factory.getOWLLiteral(o.toString(), OWL2Datatype.XSD_DATE_TIME);
			}
			else {
				return null;
			}
		}

		@Override
		public boolean isMatch(OWLDatatype dt) {
			return dt.getIRI().equals(XSDVocabulary.DATE_TIME.getIRI());
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
