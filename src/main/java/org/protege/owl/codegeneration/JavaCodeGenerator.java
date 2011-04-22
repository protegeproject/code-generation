package org.protege.owl.codegeneration;

import static org.protege.owl.codegeneration.SubstitutionVariable.CAPITALIZED_PROPERTY;
import static org.protege.owl.codegeneration.SubstitutionVariable.DATE;
import static org.protege.owl.codegeneration.SubstitutionVariable.IMPLEMENTATION_NAME;
import static org.protege.owl.codegeneration.SubstitutionVariable.IMPLEMENTS_EXTENDS;
import static org.protege.owl.codegeneration.SubstitutionVariable.INTERFACE_NAME;
import static org.protege.owl.codegeneration.SubstitutionVariable.IRI;
import static org.protege.owl.codegeneration.SubstitutionVariable.JAVA_CLASS_NAME;
import static org.protege.owl.codegeneration.SubstitutionVariable.PACKAGE;
import static org.protege.owl.codegeneration.SubstitutionVariable.PROPERTY;
import static org.protege.owl.codegeneration.SubstitutionVariable.PROPERTY_RANGE;
import static org.protege.owl.codegeneration.SubstitutionVariable.UPPERCASE_PROPERTY;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.protege.owl.codegeneration.names.CodeGenerationNames;
import org.protege.owl.codegeneration.names.CodeGenerationNamesFactory;
import org.protege.owl.codegeneration.names.NamingUtilities;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;

/**
 * A class that can create Java interfaces in the Protege-OWL format
 * 
 * @author z.khan
 * 
 */
public class JavaCodeGenerator {
	public static final Logger LOGGER = Logger.getLogger(JavaCodeGenerator.class);

    private Worker worker;

    List<Node<OWLClass>> classesNodeList;
    private OWLOntology owlOntology;
    private PrintWriter vocabularyPrintWriter;
    private FileWriter vocabularyfileWriter;

    /**Constructor
     * @param owlOntology
     * @param options
     */
    public JavaCodeGenerator(Worker worker) {
    	this.worker = worker;
        this.owlOntology = worker.getOwlOntology();
        worker.initialize();
    }

    /**Initiates the code generation
     * @param reasoner
     * @throws IOException
     */
    public void createAll(CodeGenerationInference inference) throws IOException {
        Collection<OWLClass> owlClassList = inference.getClasses();
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
        vocabularyfileWriter = new FileWriter(vocabularyFile);
        vocabularyPrintWriter = new PrintWriter(vocabularyfileWriter);
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_VOCABULARY_HEADER, substitutions, null, null);
        fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_VOCABULARY_HEADER), substitutions);

        for (OWLClass owlClass : owlClassList) {
            printClassVocabularyCode(owlClass, substitutions);
        }

        for (OWLObjectProperty owlObjectProperty : objectProperties) {
            worker.configureSubstitutions(CodeGenerationPhase.CREATE_OBJECT_PROPERTY_VOCABULARY, substitutions, null, owlObjectProperty);
            fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_OBJECT_PROPERTY_VOCABULARY), substitutions);
        }

        for (OWLDataProperty owlDataProperty : dataProperties) {
            worker.configureSubstitutions(CodeGenerationPhase.CREATE_DATA_PROPERTY_VOCABULARY, substitutions, null, owlDataProperty);
            fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_DATA_PROPERTY_VOCABULARY), substitutions);
        }

        printVocabularyEndCode(substitutions);
    }



    /** Prints the Vocabulary code for the provided OWLClass 
     * @param owlClass
     */
    private void printClassVocabularyCode(OWLClass owlClass, Map<SubstitutionVariable, String> substitutions) {
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_CLASS_VOCABULARY, substitutions, owlClass, null);
        fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_CLASS_VOCABULARY), substitutions);
    }

    /**Prints the terminating code for Vocabulary code
     * @throws IOException
     */
    private void printVocabularyEndCode(Map<SubstitutionVariable, String> substitutions) throws IOException {
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_VOCABULARY_TAIL, substitutions, null, null);
        fillTemplate(vocabularyPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_CLASS_VOCABULARY), substitutions);
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
            printFactoryCodeForClass(owlClass, factoryPrintWriter, substitutions);
        }
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_FACTORY_TAIL, substitutions, null, null);
        fillTemplate(factoryPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_FACTORY_TAIL), substitutions);
        factoryPrintWriter.close();
    }

    /** Prints the factory code for the provided OWLClass to the PrintStream
     * @param owlClass
     * @param factoryPrintWriter
     */
    private void printFactoryCodeForClass(OWLClass owlClass, PrintWriter factoryPrintWriter, Map<SubstitutionVariable, String> substitutions) {
        worker.configureSubstitutions(CodeGenerationPhase.CREATE_FACTORY_CLASS, substitutions, owlClass, null);
        fillTemplate(factoryPrintWriter, worker.getTemplate(CodeGenerationPhase.CREATE_FACTORY_CLASS), substitutions);
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
