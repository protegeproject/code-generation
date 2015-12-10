package org.protege.owl.codegeneration.test;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.DefaultWorker;
import org.protege.owl.codegeneration.Utilities;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.ReasonerBasedInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.protege.owl.codegeneration.names.IriNames;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

/**
 * Ordinarily I wouldn't corrupt the main source tree with testing code.  But this 
 * seems like a reasonable exception at this time.  This bootstraps the maven test
 * build process.  It should be the only test file that appears in the main tree.
 * 
 * @author tredmond
 *
 */
public class GenerateTestCode {
	public static Logger LOGGER = LoggerFactory.getLogger(GenerateTestCode.class);
	
	public final static File ONTOLOGY_ROOT;
	static {
		File aggregator_ontology_root = new File("org.protege.editor.owl.codegeneration/src/test/resources");
		ONTOLOGY_ROOT = aggregator_ontology_root.exists() ? aggregator_ontology_root : new File("src/test/resources");
	}
	public final static String FEB_PATH           = "2013-02-12-issue";
	public final static String FEB_TBOX_ONTOLOGY  = FEB_PATH + File.separator + "ROREKnowledgeModel.owl";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		generateSimpleJavaCode();
		LOGGER.info("All code generated.");
	}
	
	private static void generateSimpleJavaCode() throws OWLOntologyCreationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		File outputFolder = getOutputFolder();
		Utilities.deleteFolder(outputFolder);
		generateSimpleJavaCode("CodeGeneration001.owl", "inferred.testSimple", "MyInferredFactory", true, outputFolder);
		generateSimpleJavaCode("CodeGeneration001.owl", "std.testSimple02", "MySimpleStdFactory", false, outputFolder);
		generateSimpleJavaCode("CodeGeneration002.owl", "inferred.propertyValues", "InferredPropertyValuesFactory", true, outputFolder);
		generateSimpleJavaCode("CodeGeneration003.owl", "inferred.generate03", "InferredGenerate03Factory",true, outputFolder);
		generateSimpleJavaCode("CodeGeneration004.owl", "inferred.generate04", "InferredGenerate04Factory",true, outputFolder);
		generateSimpleJavaCode("pizza.owl", "inferred.pizza", "MyInferredPizzaFactory", true, outputFolder);
		generateSimpleJavaCode(GenerateTestCode.FEB_TBOX_ONTOLOGY, "inferred.febissue", "FebIssueFactory", true, outputFolder);
		generateCustomJavaCode();
	}
	
	private static File getOutputFolder() {
		File aggregator_dir = new File("org.protege.editor.owl.codegeneration");
		String relative_path = "target/generated-sources";
		if (aggregator_dir.exists()) {
			return new File(aggregator_dir, relative_path);
		}
		else {
			return new File(relative_path);
		}
	}
	
	private static void generateSimpleJavaCode(String ontologyLocation, 
	                                           String packageName,
	                                           String factoryName,
	                                           boolean useInference, 
	                                           File outputFolder) throws OWLOntologyCreationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		long startTime = System.currentTimeMillis();
		String fullPackageName = "org.protege.owl.codegeneration." + packageName;
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		addIRIMappers(manager);
		OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(new File(GenerateTestCode.ONTOLOGY_ROOT, ontologyLocation));
		CodeGenerationOptions options = new CodeGenerationOptions();
		options.setPackage(fullPackageName);
		options.setFactoryClassName(factoryName);
		options.setOutputFolder(outputFolder);
        CodeGenerationInference inference;
        if (useInference) {
    		// OWLReasonerFactory rFactory = (OWLReasonerFactory) Class.forName("uk.ac.manchester.cs.jfact.JFactFactory").newInstance();
    		OWLReasonerFactory rFactory = (OWLReasonerFactory) Class.forName("org.semanticweb.HermiT.Reasoner$ReasonerFactory").newInstance();
			// OWLReasonerFactory rFactory = (OWLReasonerFactory) Class.forName("com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory").newInstance();
    		OWLReasoner reasoner = rFactory.createNonBufferingReasoner(owlOntology);
        	inference = new ReasonerBasedInference(owlOntology, reasoner);
        }
        else {
        	inference = new SimpleInference(owlOntology);
        }
        // inference.preCompute();
        DefaultWorker.generateCode(owlOntology, options, new IriNames(owlOntology, options), inference);
		LOGGER.info("Generating source code for ontology " + ontologyLocation 
				+ " (" + (useInference ? "inferred - " : "asserted -") + (System.currentTimeMillis() - startTime) + "ms).");
	}
	
	public static void generateCustomJavaCode() throws IOException, OWLOntologyCreationException {
		long startTime = System.currentTimeMillis();
		File outputFolder = getOutputFolder();
		String fullPackageName = "org.protege.owl.codegeneration.custom.names";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(new File(GenerateTestCode.ONTOLOGY_ROOT, "CustomNames.owl"));
		CodeGenerationOptions options = new CodeGenerationOptions();
		options.setPackage(fullPackageName);
		options.setFactorySubPackage("test.factory.path");
		options.setFactoryClassName("CustomFactory");
		
		options.setOutputFolder(outputFolder);
        CodeGenerationInference inference = new SimpleInference(owlOntology);
        
        DefaultWorker.generateCode(owlOntology, options, new IriNames(owlOntology, options), inference);
		LOGGER.info("Generating source code for ontology using customized options " 
				+ (System.currentTimeMillis() - startTime) + "ms).");
	}

	public static void addIRIMappers(OWLOntologyManager manager) {
		manager.addIRIMapper(new SimpleIRIMapper(IRI.create("http://jamesnaish.wordpress.com/ROREKnowledgeModel.owl"), 
				 IRI.create(new File(ONTOLOGY_ROOT, FEB_PATH + File.separator + "ROREKnowledgeModel.owl"))));
		manager.addIRIMapper(new SimpleIRIMapper(IRI.create("http://jamesnaish.wordpress.com/Model/AutopilotSourceModel.owl"), 
				 IRI.create(new File(ONTOLOGY_ROOT, FEB_PATH + File.separator + "AutopilotSourceModel.owl"))));		
	}
}
