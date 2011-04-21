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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private CodeGenerationOptions options;
    private CodeGenerationInference inference;
    private CodeGenerationNames names;

    List<Node<OWLClass>> classesNodeList;
    private OWLOntology owlOntology;
    private Set<OWLObjectProperty> objectProperties = new HashSet<OWLObjectProperty>();
    private Set<OWLDataProperty> dataProperties = new HashSet<OWLDataProperty>();
    private PrintWriter vocabularyPrintWriter;
    private FileWriter vocabularyfileWriter;

    /**Constructor
     * @param owlOntology
     * @param options
     */
    public JavaCodeGenerator(OWLOntology owlOntology, CodeGenerationOptions options) {
        this.owlOntology = owlOntology;
        this.options = options;
        this.inference = new SimpleInference(owlOntology);
        this.names = new CodeGenerationNamesFactory(owlOntology, options).getCGNames();
        File folder = options.getOutputFolder();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        String pack = options.getPackage();
        if (pack != null) {
            pack = pack.replace('.', '/');
            File file = folder == null ? new File(pack) : new File(folder, pack);
            file.mkdirs();
            File f = new File(file, "impl");
            f.mkdirs();
        } else {
            File file = folder == null ? new File("impl") : new File(folder, "impl");
            file.mkdirs();
        }
    }

    /**Initiates the code generation
     * @param reasoner
     * @throws IOException
     */
    public void createAll(CodeGenerationInference inference) throws IOException {
    	this.inference = inference;
    	objectProperties = owlOntology.getObjectPropertiesInSignature(true);
    	dataProperties = owlOntology.getDataPropertiesInSignature(true);
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
        String interfaceName = names.getInterfaceName(owlClass);
        File baseFile = getInterfaceFile(interfaceName);
        FileWriter fileWriter = new FileWriter(baseFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printInterfaceCode(interfaceName, owlClass, printWriter);
        printWriter.close();
    }
    
    

    /**
     * Writes the interface code for the provided OWlClass to the PrintStream
     * 
     * @param interfaceName 
     * @param owlClass
     * @param printWriter
     */
    private void printInterfaceCode(String interfaceName, OWLClass owlClass, PrintWriter printWriter) {
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);

        Collection<OWLObjectProperty> owlObjectProperties = inference.getObjectPropertiesForClass(owlClass);
        Collection<OWLDataProperty> owlDataProperties = inference.getDataPropertiesForClass(owlClass);
        substitutions.put(PACKAGE, options.getPackage());
        substitutions.put(JAVA_CLASS_NAME, interfaceName);
        substitutions.put(IMPLEMENTS_EXTENDS, getInterfaceExtendsCode(owlClass));
        substitutions.put(DATE, new Date().toString());
        SubstitutionVariable.fillTemplate(printWriter, "/interface.header", substitutions);


        for (OWLObjectProperty owlObjectProperty : owlObjectProperties) {
            printInterfaceObjectPropertyCode(owlClass, owlObjectProperty, printWriter, substitutions);
        }
        for (OWLDataProperty owlDataProperty :owlDataProperties) {
            printInterfaceDataPropertyCode(owlClass, owlDataProperty, printWriter, substitutions);
        }
        SubstitutionVariable.fillTemplate(printWriter, "/interface.tail", substitutions);

    }

    /**
     * Writes the interface object code for the provided OWLObjectProperty to the PrintStream
     * @param owlObjectProperty
     * @param printWriter
     */
    private void printInterfaceObjectPropertyCode(OWLClass owlClass, OWLObjectProperty owlObjectProperty, 
    		                                      PrintWriter printWriter,
    		                                      Map<SubstitutionVariable, String> substitutions) {
        String propertyName = names.getObjectPropertyName(owlObjectProperty);
        String propertyNameUpperCase = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
    	substitutions.put(IRI, owlObjectProperty.getIRI().toString());
    	substitutions.put(PROPERTY, propertyName);
    	substitutions.put(CAPITALIZED_PROPERTY, propertyNameUpperCase);
    	substitutions.put(PROPERTY_RANGE, getObjectPropertyRange(owlClass, owlObjectProperty));
    	SubstitutionVariable.fillTemplate(printWriter, "/interface.object.property", substitutions);
    }
    
    /**
     * @param owlDataProperty
     * @param printWriter
     */
    private void printInterfaceDataPropertyCode(OWLClass owlClass, OWLDataProperty owlDataProperty, 
    		                                    PrintWriter printWriter,
    		                                    Map<SubstitutionVariable, String> substitutions) {
        String propertyName = names.getDataPropertyName(owlDataProperty);
        String propertyNameUpperCase = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
    	substitutions.put(IRI, owlDataProperty.getIRI().toString());
    	substitutions.put(PROPERTY, propertyName);
    	substitutions.put(CAPITALIZED_PROPERTY, propertyNameUpperCase);
    	substitutions.put(PROPERTY_RANGE, getDataPropertyRange(owlClass, owlDataProperty));
    	SubstitutionVariable.fillTemplate(printWriter, "/interface.data.property", substitutions);
    }
    
    
    private void createImplementation(OWLClass owlClass) throws IOException {
        String implName = names.getImplementationName(owlClass);
        File baseFile = getImplementationFile(implName);
        FileWriter fileWriter = new FileWriter(baseFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printImplementationCode(implName, owlClass, printWriter);
        printWriter.close();
    }

    private void printImplementationCode(String implName, OWLClass owlClass, PrintWriter printWriter) {
        Collection<OWLObjectProperty> owlObjectProperties = inference.getObjectPropertiesForClass(owlClass);
        Collection<OWLDataProperty> owlDataProperties = inference.getDataPropertiesForClass(owlClass);
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);
    	substitutions.put(PACKAGE, options.getPackage());
    	substitutions.put(IMPLEMENTS_EXTENDS, getImplementationExtendsCode(owlClass) + " implements " + names.getInterfaceName(owlClass));
    	substitutions.put(DATE, new Date().toString());
    	substitutions.put(JAVA_CLASS_NAME, implName);
    	SubstitutionVariable.fillTemplate(printWriter, "/implementation.header", substitutions);
        for (OWLObjectProperty owlObjectProperty : owlObjectProperties) {
            printImplementationObjectPropertyCode(owlClass, owlObjectProperty, printWriter, substitutions);
        }
        for (OWLDataProperty owlDataProperty :owlDataProperties) {
            printImplementationDataPropertyCode(owlClass, owlDataProperty, printWriter, substitutions);
        }
    	SubstitutionVariable.fillTemplate(printWriter, "/implementation.tail", substitutions);
    }

    /**
     * Writes the interface object code for the provided OWLObjectProperty to the PrintStream
     * @param owlObjectProperty
     * @param printWriter
     */
    private void printImplementationObjectPropertyCode(OWLClass owlClass, OWLObjectProperty owlObjectProperty, 
    		                                      PrintWriter printWriter,
    		                                      Map<SubstitutionVariable, String> substitutions) {
        String propertyName = names.getObjectPropertyName(owlObjectProperty);
        String propertyCapitilized = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String propertyUpperCase = propertyName.toUpperCase();
    	substitutions.put(IRI, owlObjectProperty.getIRI().toString());
    	substitutions.put(PROPERTY, propertyName);
    	substitutions.put(CAPITALIZED_PROPERTY, propertyCapitilized);
    	substitutions.put(UPPERCASE_PROPERTY, propertyUpperCase);
    	substitutions.put(PROPERTY_RANGE, getObjectPropertyRange(owlClass, owlObjectProperty));
    	SubstitutionVariable.fillTemplate(printWriter, "/implementation.object.property", substitutions);
    }
    
    /**
     * @param owlDataProperty
     * @param printWriter
     */
    private void printImplementationDataPropertyCode(OWLClass owlClass, OWLDataProperty owlDataProperty, 
    		                                    PrintWriter printWriter,
    		                                    Map<SubstitutionVariable, String> substitutions) {
        String propertyName = names.getDataPropertyName(owlDataProperty);
        String propertyCapitalized = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String propertyUpperCase = propertyName.toUpperCase();
    	substitutions.put(IRI, owlDataProperty.getIRI().toString());
    	substitutions.put(PROPERTY, propertyName);
    	substitutions.put(CAPITALIZED_PROPERTY, propertyCapitalized);
    	substitutions.put(UPPERCASE_PROPERTY, propertyUpperCase);
    	substitutions.put(PROPERTY_RANGE, getDataPropertyRange(owlClass, owlDataProperty));
    	SubstitutionVariable.fillTemplate(printWriter, "/implementation.data.property", substitutions);
    }


    /** Initilizes the vocabulary code generation 
     * @param owlClassList
     * @throws IOException
     */
    private void printVocabularyCode(Collection<OWLClass> owlClassList) throws IOException {
        File vocabularyFile = getInterfaceFile(JavaCodeGeneratorConstants.VOCABULARY_CLASS_NAME);
        vocabularyfileWriter = new FileWriter(vocabularyFile);
        vocabularyPrintWriter = new PrintWriter(vocabularyfileWriter);
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);
        substitutions.put(PACKAGE, options.getPackage());
        substitutions.put(DATE, new Date().toString());

        SubstitutionVariable.fillTemplate(vocabularyPrintWriter, "/vocabulary.header", substitutions);

        for (OWLClass owlClass : owlClassList) {
            printClassVocabularyCode(owlClass);
        }

        for (OWLObjectProperty owlObjectProperty : objectProperties) {
            substitutions.put(CAPITALIZED_PROPERTY, names.getObjectPropertyName(owlObjectProperty).toUpperCase());
            substitutions.put(IRI, owlObjectProperty.getIRI().toString());
            SubstitutionVariable.fillTemplate(vocabularyPrintWriter, "/vocabulary.object.property", substitutions);
        }

        for (OWLDataProperty owlDataProperty : dataProperties) {
            substitutions.put(CAPITALIZED_PROPERTY, names.getDataPropertyName(owlDataProperty).toUpperCase());
            substitutions.put(IRI, owlDataProperty.getIRI().toString());
            SubstitutionVariable.fillTemplate(vocabularyPrintWriter, "/vocabulary.data.property", substitutions);
        }

        printVocabularyEndCode();
    }



    /** Prints the Vocabulary code for the provided OWLClass 
     * @param owlClass
     */
    private void printClassVocabularyCode(OWLClass owlClass) {
        String className = names.getInterfaceName(owlClass);
        vocabularyPrintWriter.println("    public static final OWLClass " + className.toUpperCase()
                + " = factory.getOWLClass(IRI.create(\"" + owlClass.getIRI().toString() + "\"));");
        vocabularyPrintWriter.println();
    }

    /**Prints the terminating code for Vocabulary code
     * @throws IOException
     */
    private void printVocabularyEndCode() throws IOException {
        vocabularyPrintWriter.println(" }");
        vocabularyfileWriter.close();

    }

    /** Initializes the code generation for factory classes 
     * @param owlClassList
     * @throws IOException
     */
    private void printFactoryClassCode(Collection<OWLClass> owlClassList) throws IOException {
        FileWriter factoryFileWriter = null;
        PrintWriter factoryPrintWriter = null;
        File factoryFile = getInterfaceFile(options.getFactoryClassName());
        factoryFileWriter = new FileWriter(factoryFile);
        factoryPrintWriter = new PrintWriter(factoryFileWriter);
        
    	Map<SubstitutionVariable, String> substitutions = new EnumMap<SubstitutionVariable, String>(SubstitutionVariable.class);
    	substitutions.put(PACKAGE, options.getPackage());
    	substitutions.put(DATE, new Date().toString());
    	substitutions.put(JAVA_CLASS_NAME, options.getFactoryClassName());
    	SubstitutionVariable.fillTemplate(factoryPrintWriter, "/factory.header", substitutions);

        for (OWLClass owlClass : owlClassList) {
            printFactoryCodeForClass(owlClass, factoryPrintWriter, substitutions);
        }
        SubstitutionVariable.fillTemplate(factoryPrintWriter, "/factory.tail", substitutions);
        factoryPrintWriter.close();
    }

    /** Prints the factory code for the provided OWLClass to the PrintStream
     * @param owlClass
     * @param factoryPrintWriter
     */
    private void printFactoryCodeForClass(OWLClass owlClass, PrintWriter factoryPrintWriter, Map<SubstitutionVariable, String> substitutions) {
        substitutions.put(INTERFACE_NAME, names.getInterfaceName(owlClass));
        substitutions.put(IMPLEMENTATION_NAME, names.getImplementationName(owlClass));
        SubstitutionVariable.fillTemplate(factoryPrintWriter, "/factory.owlclass", substitutions);
    }

    private String getImplementationExtendsCode(OWLClass owlClass) {
	    String str = " extends ";
	    String base = getBaseImplementation(owlClass);
	    if (base == null) {
	        return str + JavaCodeGeneratorConstants.ABSTRACT_CODE_GENERATOR_INDIVIDUAL_CLASS;
	    } else {
	        return str + base;
	    }
	}

	private String getObjectPropertyRange(OWLClass owlClass, OWLObjectProperty owlObjectProperty) {
		OWLDataFactory factory  = owlOntology.getOWLOntologyManager().getOWLDataFactory();
		Collection<OWLClass> classes = inference.getRange(owlClass, owlObjectProperty);
		if (classes.isEmpty() || classes.size() > 1 || classes.contains(factory.getOWLThing())) {
			return PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE;
		}
		return names.getInterfaceName(classes.iterator().next());
	}

	private String getDataPropertyRange(OWLClass owlClass, OWLDataProperty owlDataProperty) {
	    OWLDatatype  dt = inference.getRange(owlClass, owlDataProperty);
	    return getDataPropertyJavaName(dt);
	}

	/**
	 * @param dataPropertyRange
	 * @param owlDataRanges
	 * @return
	 */
	private String getDataPropertyJavaName(OWLDatatype dt) {
	    String dataPropertyRange = null;
	    if (dt == null) {
	        dataPropertyRange = PropertyConstants.UNKNOWN_JAVA_DATA_TYPE;
	    } else {
	        dataPropertyRange = getOwlDataTypeAsJavaClassString(dt);
	    }
	    return dataPropertyRange;
	}

	/*
	 * Synchronize this with CodeGeneratorInference implementations.
	 */
	private String getOwlDataTypeAsJavaClassString(OWLDatatype owlDatatype) {
		for (HandledDatatypes handled : HandledDatatypes.values()) {
			if (handled.isMatch(owlDatatype)) {
				return handled.getJavaClass();
			}
		}
		return PropertyConstants.UNKNOWN_JAVA_DATA_TYPE;
	}

	private File getInterfaceFile(String name) {
	    String pack = options.getPackage();
	    if (pack != null) {
	        pack = pack.replace('.', '/') + "/";
	    } else {
	        pack = "";
	    }
	    return new File(options.getOutputFolder(), pack + name + ".java");
	}

	private String getInterfaceExtendsCode(OWLClass owlClass) {
	    String str = " extends ";
	    String base = getBaseInterface(owlClass);
	    if (base == null) {
	    	return "";
	    }
	    else {
	    	return str + base;
	    }
	}

	private File getImplementationFile(String implName) {
	    String pack = options.getPackage();
	    if (pack != null) {
	        pack = pack.replace('.', '/') + "/";
	    } else {
	        pack = "";
	    }
	    return new File(options.getOutputFolder(), pack + "impl/" + implName + ".java");
	}

	/** Returns base implementation of the provided OWLClass
     * @param owlClass
     * @return
     */
    private String getBaseImplementation(OWLClass owlClass) {
        String baseImplementationString = null;
        for (OWLClass superClass : inference.getSuperClasses(owlClass)) {
            if (superClass != null && !superClass.isTopEntity()) {
                if (baseImplementationString == null) {
                    baseImplementationString = names.getImplementationName(superClass);
                } else {
                    return null;
                }
            }
        }
        return baseImplementationString;
    }

    /** Returns base interface of the provided OWLClass
     * @param owlClass The OWLClass whose base interface is to be returned
     * @return
     */
    private String getBaseInterface(OWLClass owlClass) {
        String baseInterfaceString = "";
        for (OWLClass superClass : inference.getSuperClasses(owlClass)) {
            if (superClass != null && !superClass.isTopEntity()) {
                baseInterfaceString += (baseInterfaceString.equals("") ? "" : ", ") + names.getInterfaceName(superClass);
            }
        }
        if (baseInterfaceString.equals("")) {
            return null;
        } else {
            return baseInterfaceString;
        }
    }


}
