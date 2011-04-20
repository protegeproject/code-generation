package org.protege.owl.codegeneration.test;

import java.io.File;

import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.JavaCodeGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * Ordinarily I wouldn't corrupt the main source tree with testing code.  But this 
 * seems like a reasonable exception at this time.  This bootstraps the maven test
 * build process.  It should be the only test file that appears in the main tree.
 * 
 * @author tredmond
 *
 */
public class GenerateTestCode {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/pizza.owl"));
		CodeGenerationOptions options = new CodeGenerationOptions();
		options.setPackage("org.protege.pizza");
		options.setOutputFolder(new File("target/generated-sources"));
		JavaCodeGenerator generator = new JavaCodeGenerator(owlOntology, options);
		OWLReasonerFactory rFactory = (OWLReasonerFactory) Class.forName("org.semanticweb.HermiT.Reasoner$ReasonerFactory").newInstance();
		generator.createAll(rFactory.createNonBufferingReasoner(owlOntology));
	}

}
