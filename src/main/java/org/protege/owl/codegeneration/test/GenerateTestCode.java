package org.protege.owl.codegeneration.test;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.DefaultWorker;
import org.protege.owl.codegeneration.DeleteFolder;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.ReasonerBasedInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
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
	public static Logger LOGGER = Logger.getLogger(GenerateTestCode.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		generateSimpleJavaCode();
		LOGGER.info("All code generated.");
	}
	
	private static void generateSimpleJavaCode() throws OWLOntologyCreationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		File outputFolder = new File("target/generated-sources");
		DeleteFolder.deleteFolder(outputFolder);
		generateSimpleJavaCode("CodeGeneration001.owl", "inferred.testSimple", true, outputFolder);
		generateSimpleJavaCode("CodeGeneration001.owl", "std.testSimple02", false, outputFolder);
		generateSimpleJavaCode("pizza.owl", "inferred.pizza", true, outputFolder);
	}
	
	private static void generateSimpleJavaCode(String ontologyName, String packageName, boolean useInference, File outputFolder) throws OWLOntologyCreationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		long startTime = System.currentTimeMillis();
		String fullPackageName = "org.protege.owl.codegeneration." + packageName;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/" + ontologyName));
		CodeGenerationOptions options = new CodeGenerationOptions();
		options.setPackage(fullPackageName);
		options.setOutputFolder(outputFolder);
        CodeGenerationInference inference;
        if (useInference) {
    		OWLReasonerFactory rFactory = (OWLReasonerFactory) Class.forName("org.semanticweb.HermiT.Reasoner$ReasonerFactory").newInstance();
    		OWLReasoner reasoner = rFactory.createNonBufferingReasoner(owlOntology);
        	inference = new ReasonerBasedInference(owlOntology, reasoner);
        }
        else {
        	inference = new SimpleInference(owlOntology);
        }
        // inference.preCompute();
        DefaultWorker.generateCode(owlOntology, options, new IriNames(owlOntology, options), inference);
		LOGGER.info("Generating source code for ontology " + ontologyName 
				+ " (" + (useInference ? "inferred - " : "asserted -") + (System.currentTimeMillis() - startTime) + "ms).");
	}

}
