package org.protege.owl.codegeneration.main;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.DefaultWorker;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.ReasonerBasedInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.protege.owl.codegeneration.names.IriNames;
import org.protege.owl.codegeneration.test.GenerateTestCode;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class GenerateSimpleJavaCode {
	public static Logger LOGGER = LoggerFactory.getLogger(GenerateTestCode.class);
	public static final String DELETE_OPT   = "d";
	public static final String FACTORY_OPT  = "f";
	public static final String OUTPUT_OPT   = "o";
	public static final String PACKAGE_OPT  = "p";
	public static final String REASONER_OPT = "r";

	public static void main(String [] args) throws Exception {
		CommandLine parsedOptions = parseArguments(args);

		String outputFolderName = parsedOptions.getOptionValue(OUTPUT_OPT);
		File outputFolder = outputFolderName != null ? new File(outputFolderName) : new File("");
		if (parsedOptions.getArgList().size() != 1) {
		    help();
		    System.exit(-1);
		}
		if (parsedOptions.hasOption(DELETE_OPT) && outputFolder.exists()) {
		    delete(outputFolder);
		}
		if (!outputFolder.exists()) {
		    outputFolder.mkdir();
		}
		File ontologyLocation = new File((String) parsedOptions.getArgList().iterator().next());
		generateSimpleJavaCode(ontologyLocation, 
							   parsedOptions.getOptionValue(PACKAGE_OPT), 
							   parsedOptions.getOptionValue(FACTORY_OPT), 
							   parsedOptions.getOptionValue(REASONER_OPT), 
							   outputFolder);
	}
	
	private static void help() {
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("Code Generation", createOptions());
	}
	
	private static CommandLine parseArguments(String[] args) throws ParseException {
		BasicParser parser = new BasicParser();
		return parser.parse(createOptions(), args);
	}
	
	private static Options createOptions() {
		Options options = new Options();
		options.addOption(DELETE_OPT, "delete", false, "delete the output directory before starting");
		options.addOption(FACTORY_OPT, "factory", true, "set name of factory");
		options.addOption(OUTPUT_OPT, "output", true, "output directory");
		options.addOption(PACKAGE_OPT, "package", true, "set package for generated code");
		options.addOption(REASONER_OPT, "reasoner", true, "set reasoner to use to generate code");
		return options;
	}
	
	private static void generateSimpleJavaCode(File   ontologyLocation, 
											   String packageName,
											   String factoryName,
											   String reasonerFactoryName, 
											   File outputFolder) throws OWLOntologyCreationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		long startTime = System.currentTimeMillis();
		boolean useInference = (reasonerFactoryName != null);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(ontologyLocation);
		CodeGenerationOptions options = new CodeGenerationOptions();
		options.setPackage(packageName);
		options.setFactoryClassName(factoryName);
		options.setOutputFolder(outputFolder);
		CodeGenerationInference inference;
		if (reasonerFactoryName != null) {
			OWLReasonerFactory rFactory = (OWLReasonerFactory) Class.forName(reasonerFactoryName).newInstance();
			OWLReasoner reasoner = rFactory.createNonBufferingReasoner(owlOntology);
			inference = new ReasonerBasedInference(owlOntology, reasoner);
		}
		else {
			inference = new SimpleInference(owlOntology);
		}
		// inference.preCompute();
		DefaultWorker.generateCode(owlOntology, options, new IriNames(owlOntology, options), inference);
		LOGGER.info("Generated source code for ontology " + ontologyLocation 
				+ " (" + (useInference ? "inferred " : "asserted.") + "  Total time = " + (System.currentTimeMillis() - startTime) + "ms).");
	}
	
	private static void delete(File file) {
	    if (file.isDirectory()) {
	        for (File child : file.listFiles()) {
	            delete(child);
	        }
	    }
	    file.delete();
	}

}
