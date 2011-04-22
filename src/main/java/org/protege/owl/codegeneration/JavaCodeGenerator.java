package org.protege.owl.codegeneration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * A class that can create Java interfaces in the Protege-OWL format
 * 
 * @author z.khan
 * 
 */
public class JavaCodeGenerator {
	public static final Logger LOGGER = Logger.getLogger(JavaCodeGenerator.class);

    private Worker worker;

    /**Constructor
     * @param owlOntology
     * @param options
     */
    public JavaCodeGenerator(Worker worker) {
    	this.worker = worker;
        worker.initialize();
    }

    /**Initiates the code generation
     * @param reasoner
     * @throws IOException
     */
    public void createAll() throws IOException {
        Collection<OWLClass> owlClassList = worker.getOwlClasses();
        printVocabularyCode(owlClassList);
        printFactoryClassCode(owlClassList);
        for (OWLClass owlClass : owlClassList) {
            createInterface(owlClass);
            createImplementation(owlClass);
        }
    }

    /**
     * Generates interface code for the provided OWlClass
     * 
     * @param owlClass The class whose interface code is to generated
     * @throws IOException
     */
    private void createInterface(OWLClass owlClass) throws IOException {
        File baseFile = worker.getInterfaceFile(owlClass);
        FileWriter fileWriter = new FileWriter(baseFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printInterfaceCode(owlClass, printWriter);
        printWriter.close();
    }
    
    

    /**
     * Writes the interface code for the provided OWlClass to the PrintStream
     * 
     * @param interfaceName 
     * @param owlClass
     * @param printWriter
     */
    private void printInterfaceCode(OWLClass owlClass, PrintWriter printWriter) {
        Collection<OWLObjectProperty> owlObjectProperties = worker.getObjectPropertiesForClass(owlClass);
        Collection<OWLDataProperty> owlDataProperties = worker.getDataPropertiesForClass(owlClass);
        
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);
    	worker.configureSubstitutions(CodeGenerationPhase.CREATE_INTERFACE_HEADER, substitutions, owlClass, null);
        fillTemplate(printWriter, worker.getTemplate(CodeGenerationPhase.CREATE_INTERFACE_HEADER), substitutions);

        for (OWLObjectProperty owlObjectProperty : owlObjectProperties) {
            printInterfaceObjectPropertyCode(owlClass, owlObjectProperty, printWriter, substitutions);
        }
        for (OWLDataProperty owlDataProperty :owlDataProperties) {
            printInterfaceDataPropertyCode(owlClass, owlDataProperty, printWriter, substitutions);
        }
    	worker.configureSubstitutions(CodeGenerationPhase.CREATE_INTERFACE_TAIL, substitutions, owlClass, null);
        fillTemplate(printWriter, worker.getTemplate(CodeGenerationPhase.CREATE_INTERFACE_TAIL), substitutions);

    }

    /**
     * Writes the interface object code for the provided OWLObjectProperty to the PrintStream
     * @param owlObjectProperty
     * @param printWriter
     */
    private void printInterfaceObjectPropertyCode(OWLClass owlClass, OWLObjectProperty owlObjectProperty, 
    		                                      PrintWriter printWriter,
    		                                      Map<SubstitutionVariable, String> substitutions) {
    	worker.configureSubstitutions(CodeGenerationPhase.CREATE_OBJECT_PROPERTY_INTERFACE, substitutions, owlClass, owlObjectProperty);
    	fillTemplate(printWriter, worker.getTemplate(CodeGenerationPhase.CREATE_OBJECT_PROPERTY_INTERFACE), substitutions);
    }
    
    /**
     * @param owlDataProperty
     * @param printWriter
     */
    private void printInterfaceDataPropertyCode(OWLClass owlClass, OWLDataProperty owlDataProperty, 
    		                                    PrintWriter printWriter,
    		                                    Map<SubstitutionVariable, String> substitutions) {
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_DATA_PROPERTY_INTERFACE, substitutions, owlClass, owlDataProperty);
    	fillTemplate(printWriter, worker.getTemplate(CodeGenerationPhase.CREATE_DATA_PROPERTY_INTERFACE), substitutions);
    }
    
    
    private void createImplementation(OWLClass owlClass) throws IOException {
        File baseFile = worker.getImplementationFile(owlClass);
        FileWriter fileWriter = new FileWriter(baseFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printImplementationCode(owlClass, printWriter);
        printWriter.close();
    }

    private void printImplementationCode(OWLClass owlClass, PrintWriter printWriter) {
        Collection<OWLObjectProperty> owlObjectProperties = worker.getObjectPropertiesForClass(owlClass);
        Collection<OWLDataProperty> owlDataProperties = worker.getDataPropertiesForClass(owlClass);
        
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_IMPLEMENTATION_HEADER, substitutions, owlClass, null);
    	fillTemplate(printWriter, worker.getTemplate(CodeGenerationPhase.CREATE_IMPLEMENTATION_HEADER), substitutions);
        for (OWLObjectProperty owlObjectProperty : owlObjectProperties) {
            printImplementationObjectPropertyCode(owlClass, owlObjectProperty, printWriter, substitutions);
        }
        for (OWLDataProperty owlDataProperty :owlDataProperties) {
            printImplementationDataPropertyCode(owlClass, owlDataProperty, printWriter, substitutions);
        }
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_IMPLEMENTATION_TAIL, substitutions, owlClass, null);
    	fillTemplate(printWriter, worker.getTemplate(CodeGenerationPhase.CREATE_IMPLEMENTATION_TAIL), substitutions);
    }

    /**
     * Writes the interface object code for the provided OWLObjectProperty to the PrintStream
     * @param owlObjectProperty
     * @param printWriter
     */
    private void printImplementationObjectPropertyCode(OWLClass owlClass, OWLObjectProperty owlObjectProperty, 
    		                                      PrintWriter printWriter,
    		                                      Map<SubstitutionVariable, String> substitutions) {
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_OBJECT_PROPERTY_IMPLEMENTATION, substitutions, owlClass, owlObjectProperty);
    	fillTemplate(printWriter, worker.getTemplate(CodeGenerationPhase.CREATE_OBJECT_PROPERTY_IMPLEMENTATION), substitutions);
    }
    
    /**
     * @param owlDataProperty
     * @param printWriter
     */
    private void printImplementationDataPropertyCode(OWLClass owlClass, OWLDataProperty owlDataProperty, 
    		                                    PrintWriter printWriter,
    		                                    Map<SubstitutionVariable, String> substitutions) {
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_DATA_PROPERTY_IMPLEMENTATION, substitutions, owlClass, owlDataProperty);
    	fillTemplate(printWriter, worker.getTemplate(CodeGenerationPhase.CREATE_DATA_PROPERTY_IMPLEMENTATION), substitutions);
    }


    /** Initilizes the vocabulary code generation 
     * @param owlClassList
     * @throws IOException
     */
    private void printVocabularyCode(Collection<OWLClass> owlClassList) throws IOException {
        File vocabularyFile = worker.getVocabularyFile();
        FileWriter vocabularyfileWriter = new FileWriter(vocabularyFile);
        PrintWriter vocabularyPrintWriter = new PrintWriter(vocabularyfileWriter);
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_VOCABULARY_HEADER, substitutions, null, null);
        fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_VOCABULARY_HEADER), substitutions);

        for (OWLClass owlClass : owlClassList) {
            worker.configureSubstitutions(CodeGenerationPhase.CREATE_CLASS_VOCABULARY, substitutions, owlClass, null);
            fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_CLASS_VOCABULARY), substitutions);        }

        for (OWLObjectProperty owlObjectProperty : worker.getOwlObjectProperties()) {
            worker.configureSubstitutions(CodeGenerationPhase.CREATE_OBJECT_PROPERTY_VOCABULARY, substitutions, null, owlObjectProperty);
            fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_OBJECT_PROPERTY_VOCABULARY), substitutions);
        }

        for (OWLDataProperty owlDataProperty : worker.getOwlDataProperties()) {
            worker.configureSubstitutions(CodeGenerationPhase.CREATE_DATA_PROPERTY_VOCABULARY, substitutions, null, owlDataProperty);
            fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_DATA_PROPERTY_VOCABULARY), substitutions);
        }
        
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_VOCABULARY_TAIL, substitutions, null, null);
        fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_VOCABULARY_TAIL), substitutions);
    
        vocabularyPrintWriter.close();
    }

    /** Initializes the code generation for factory classes 
     * @param owlClassList
     * @throws IOException
     */
    private void printFactoryClassCode(Collection<OWLClass> owlClassList) throws IOException {
        FileWriter factoryFileWriter = null;
        PrintWriter factoryPrintWriter = null;
        File factoryFile = worker.getFactoryFile();
        factoryFileWriter = new FileWriter(factoryFile);
        factoryPrintWriter = new PrintWriter(factoryFileWriter);
        
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);
        
    	worker.configureSubstitutions(CodeGenerationPhase.CREATE_FACTORY_HEADER, substitutions, null, null);
    	fillTemplate(factoryPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_FACTORY_HEADER), substitutions);

        for (OWLClass owlClass : owlClassList) {
            worker.configureSubstitutions(CodeGenerationPhase.CREATE_FACTORY_CLASS, substitutions, owlClass, null);
            fillTemplate(factoryPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_FACTORY_CLASS), substitutions);
        }
        
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_FACTORY_TAIL, substitutions, null, null);
        fillTemplate(factoryPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_FACTORY_TAIL), substitutions);
        
        factoryPrintWriter.close();
    }

	
	public static void fillTemplate(PrintWriter writer, String template, Map<SubstitutionVariable, String> substitutions) {
		for (Entry<SubstitutionVariable, String> entry : substitutions.entrySet()) {
			SubstitutionVariable var = entry.getKey();
			String replacement = entry.getValue();
			template = template.replaceAll("\\$\\{" + var.getName() + "\\}", replacement);
		}
		writer.append(template);
	}


}
