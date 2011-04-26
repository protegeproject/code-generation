package org.protege.owl.codegeneration.test;

import java.io.File;
import java.io.IOException;

import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.DefaultWorker;
import org.protege.owl.codegeneration.JavaCodeGenerator;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.ReasonerBasedInference;
import org.protege.owl.codegeneration.names.IriNames;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
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
		generateSimpleJavaCode();
		System.out.println("All code generated.");
	}
	
	private static void generateSimpleJavaCode() throws OWLOntologyCreationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/CodeGeneration001.owl"));
		CodeGenerationOptions options = new CodeGenerationOptions();
		options.setPackage("org.protege.owl.codegeneration.testSimple");
		options.setOutputFolder(new File("target/generated-sources"));
		OWLReasonerFactory rFactory = (OWLReasonerFactory) Class.forName("org.semanticweb.HermiT.Reasoner$ReasonerFactory").newInstance();
		OWLReasoner reasoner = rFactory.createNonBufferingReasoner(owlOntology);
        CodeGenerationInference inference = new ReasonerBasedInference(owlOntology, reasoner);
        DefaultWorker.generateCode(owlOntology, options, new IriNames(owlOntology, options), inference);
	}

}
