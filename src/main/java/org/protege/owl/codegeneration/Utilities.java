package org.protege.owl.codegeneration;

import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

public class Utilities {
    private Utilities() { }
    
	public static void deleteFolder(File folder) {
		if (folder.exists() && folder.isDirectory()) {
			for (File toDelete : folder.listFiles()) {
				if (toDelete.isDirectory()) {
					deleteFolder(toDelete);
				}
				else {
					toDelete.delete();
				}
			}
			folder.delete();
		}
	}
	
	public static <X extends OWLEntity> Collection<X> filterIgnored(Collection<X> entities, OWLOntology ontology) {
		Collection<X> filteredEntities = new TreeSet<X>();
		for (X entity : entities) {
			if (!ignore(entity, ontology)) {
				filteredEntities.add(entity);
			}
		}
		return filteredEntities;
	}

	public static boolean ignore(OWLEntity en, OWLOntology ontology) {
		Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(en.getIRI(), ontology);
		for (OWLAnnotation anno : annotations) {
			if (!anno.getProperty().equals(Constants.IGNORE)) {
				continue;
			}
			OWLAnnotationValue annotationValue = anno.getValue();
			if (!(annotationValue instanceof OWLLiteral)) {
				 continue;
			}
			OWLLiteral literalValue = (OWLLiteral) annotationValue;
			if (literalValue.isBoolean()) {				
				try {
					if (literalValue.parseBoolean()) {
						return true;
					}
				}
				catch (NumberFormatException nfe) {
					continue;
				}
			}
		}
		return false;
	}

}
