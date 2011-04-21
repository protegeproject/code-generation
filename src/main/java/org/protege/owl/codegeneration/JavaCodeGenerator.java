package org.protege.owl.codegeneration;

import static org.protege.owl.codegeneration.SubstitutionVariable.IRI;
import static org.protege.owl.codegeneration.SubstitutionVariable.PACKAGE;
import static org.protege.owl.codegeneration.SubstitutionVariable.DATE;
import static org.protege.owl.codegeneration.SubstitutionVariable.JAVA_CLASS_NAME;
import static org.protege.owl.codegeneration.SubstitutionVariable.IMPLEMENTS_EXTENDS;
import static org.protege.owl.codegeneration.SubstitutionVariable.CLASS;
import static org.protege.owl.codegeneration.SubstitutionVariable.CAPITALIZED_CLASS;
import static org.protege.owl.codegeneration.SubstitutionVariable.PROPERTY;
import static org.protege.owl.codegeneration.SubstitutionVariable.CAPITALIZED_PROPERTY;
import static org.protege.owl.codegeneration.SubstitutionVariable.PROPERTY_RANGE;
import static org.protege.owl.codegeneration.SubstitutionVariable.USER;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.protege.owl.codegeneration.names.CodeGenerationNames;
import org.protege.owl.codegeneration.names.CodeGenerationNamesFactory;
import org.protege.owl.codegeneration.names.NamingUtilities;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
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
    private Set<OWLOntology> importedOntologies;
    private Set<OWLOntology> allOwlOntologies = new HashSet<OWLOntology>();

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
        setAllOntologies();
        getOntologyObjectProperties();
        getOntologyDataProperties();
        Collection<OWLClass> owlClassList = inference.getClasses();
        printVocabularyCode(owlClassList);
        // printFactoryClassCode(owlClassList);
        for (OWLClass owlClass : owlClassList) {
            createInterface(owlClass);
            // createImplementation(owlClass);
        }
    }

    /**
     * Adds the ontology and all its imported ontology in a set.
     */
    private void setAllOntologies() {

        importedOntologies = owlOntology.getImports();
        allOwlOntologies.add(owlOntology);
        allOwlOntologies.addAll(importedOntologies);
    }

    /**
     * Retrives the OWLOntology to which the owlClass belongs to.
     * 
     * @param owlClass
     * @return The iri of the ontology to which the OWLClass belongs
     */
    private OWLOntology getParentOntology(IRI iri) {

        for (OWLOntology ontology : allOwlOntologies) {
            if (iri.toString().startsWith(ontology.getOntologyID().getOntologyIRI().toString())) {
                return ontology;
            }
        }
        return null;
    }

    /**
     * Gets all the Object Properties of the Ontology
     */
    private void getOntologyObjectProperties() {
        for (OWLOntology ontology : allOwlOntologies) {
            objectProperties.addAll(ontology.getObjectPropertiesInSignature());
        }
    }

    /**
     * Gets all the Data Properties of the Ontology
     */
    private void getOntologyDataProperties() {
        for (OWLOntology ontology : allOwlOntologies) {
            dataProperties.addAll(ontology.getDataPropertiesInSignature());
        }
    }

    /**
     * Generates interface code for the provided OWlClass
     * 
     * @param owlClass The class whose interface code is to generated
     * @throws IOException
     */
    private void createInterface(OWLClass owlClass) throws IOException {

        String interfaceName = names.getInterfaceNamePossiblyAbstract(owlClass);
        File baseFile = getInterfaceFile(interfaceName);
        FileWriter fileWriter = new FileWriter(baseFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printInterfaceCode(interfaceName, owlClass, printWriter);
        fileWriter.close();

        if (options.getAbstractMode()) {
            createUserInterface(owlClass);
        }
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
        printWriter.print(SubstitutionVariable.fillTemplate("/interface.header", substitutions));


        for (Iterator<OWLObjectProperty> iterator = owlObjectProperties.iterator(); iterator.hasNext();) {
            OWLObjectProperty owlObjectProperty = iterator.next();
            printInterfaceObjectPropertyCode(owlClass, owlObjectProperty, printWriter, substitutions);

            printWriter.println();

        }
        for (Iterator<OWLDataProperty> iterator = owlDataProperties.iterator(); iterator.hasNext();) {
            OWLDataProperty owlDataProperty = iterator.next();
            printInterfaceDataPropertyCode(owlClass, owlDataProperty, printWriter);
        }
        printWriter.println();
        printWriter.println("    OWLNamedIndividual getOwlIndividual();");
        printWriter.println();
        printWriter.println("    OWLOntology getOwlOntology();");
        printWriter.println();
        printWriter.println("    void delete();");
        printWriter.println("}");

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
    	substitutions.put(PROPERTY_RANGE, getObjectPropertyRange(owlObjectProperty, false));
        if (!inference.isFunctional(owlClass, owlObjectProperty)) {
        	printWriter.print(SubstitutionVariable.fillTemplate("/interface.nonfunctional.object.property", substitutions));
        }

        if (!inference.isFunctional(owlClass, owlObjectProperty)) {
        	// TODO FixMe!!!
            Set<OWLClassExpression> oClassExpressions = owlObjectProperty.getRanges(allOwlOntologies);
            String objPropertyJavaName = getObjectPropertyJavaName(oClassExpressions);
            printWriter.println();
            printWriter
                    .println("    " + PropertyConstants.JAVA_UTIL_ITERATOR + " list" + propertyNameUpperCase + "();");
            printWriter.println();
            printWriter.println("    void add" + propertyNameUpperCase + "(" + objPropertyJavaName + " new"
                    + propertyNameUpperCase + ");");
            printWriter.println();
            printWriter.println("    void remove" + propertyNameUpperCase + "(" + objPropertyJavaName + " old"
                    + propertyNameUpperCase + ");");
        }

        printWriter.println();
        printWriter.println("    void set" + propertyNameUpperCase + "("
                + getObjectPropertyRange(owlObjectProperty, false) + " new" + propertyNameUpperCase + ");");
    }

    /**
     * Retrieves the range for the provided OWLObjectProperty
     * @param owlObjectProperty The property whose range is to be returned
     * @param useExtends
     * @return
     */
    private String getObjectPropertyRange(OWLObjectProperty owlObjectProperty, boolean useExtends) {
        Set<OWLClassExpression> oClassExpressions = owlObjectProperty.getRanges(allOwlOntologies);
        String objPropertyRange = getObjectPropertyJavaName(oClassExpressions);
        if (owlObjectProperty.isFunctional(allOwlOntologies)) {// property can
            // contain only
            // single value
            return objPropertyRange;
        } else {// Property contains multiple values

            if (oClassExpressions.size() > 1) { // Contains More than 1 range
                // hence disable use of extends
                useExtends = false;
            }
            String genericsString = objPropertyRange.equals(PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE) ? ""
                    : useExtends ? "<? extends " + objPropertyRange + ">" : "<" + objPropertyRange + ">";
            objPropertyRange = options.getSetMode() ? "Set" + genericsString : "Collection" + genericsString;
            return objPropertyRange;
        }
    }

    /** Returns a Java name for the object property
     * @param objPropertyRange
     * @param oClassExpressions
     * @return
     */
    private String getObjectPropertyJavaName(Set<OWLClassExpression> oClassExpressions) {
        String objPropertyRange = null;
        if (oClassExpressions == null || oClassExpressions.isEmpty() || oClassExpressions.size() > 1) {// If count of range is other
            // then One (zero of more
            // than 1 ) then return
            // range as java.lang.Object
            objPropertyRange = PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE;
        } else {
            for (OWLClassExpression owlClassExpression : oClassExpressions) {
                try {
                    OWLClass owlClass = owlClassExpression.asOWLClass();
                    objPropertyRange = names.getInterfaceName(owlClass);
                    break;
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return objPropertyRange;
    }

    /**
     * @param owlDataProperty
     * @param printWriter
     */
    private void printInterfaceDataPropertyCode(OWLClass owlClass, OWLDataProperty owlDataProperty, PrintWriter printWriter) {
        String propertyName = names.getDataPropertyName(owlDataProperty);
        String propertyNameUpperCase = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        printWriter.println();
        printWriter.println();
        printWriter.println("    // Property " + owlDataProperty.getIRI());
        printWriter.println();
        printWriter.println("    " + getDataPropertyRange(owlClass, owlDataProperty) + " get" + propertyNameUpperCase + "();");
        printWriter.println();
        printWriter.println("    " + "OWLDataProperty get" + propertyNameUpperCase + "Property();");
        printWriter.println();
        printWriter.println("    boolean has" + propertyNameUpperCase + "();");
        if (!owlDataProperty.isFunctional(allOwlOntologies)) {
            OWLDatatype dt = inference.getRange(owlClass, owlDataProperty);
            printWriter.println();
            printWriter
                    .println("    " + PropertyConstants.JAVA_UTIL_ITERATOR + " list" + propertyNameUpperCase + "();");
            printWriter.println();
            printWriter.println("    void add" + propertyNameUpperCase + "(" + getDataPropertyJavaName(dt)
                    + " new" + propertyNameUpperCase + ");");
            printWriter.println();
            printWriter.println("    void remove" + propertyNameUpperCase + "("
                    + getDataPropertyJavaName(dt) + " old" + propertyNameUpperCase + ");");

        }
        printWriter.println();
        printWriter.println("    void set" + propertyNameUpperCase + "(" + getDataPropertyRange(owlClass, owlDataProperty)
                + " new" + propertyNameUpperCase + ");");

    }

    private String getDataPropertyRange(OWLClass owlClass, OWLDataProperty owlDataProperty) {
        OWLDatatype  dt = inference.getRange(owlClass, owlDataProperty);
        String dataPropertyRange = getDataPropertyJavaName(dt);

        if (owlDataProperty.isFunctional(allOwlOntologies)) {// property can
            // contain only
            // single value
            return dataPropertyRange;
        } else {
            String genericsString = dataPropertyRange.equals(PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE) ? "" : "<"
                    + dataPropertyRange + ">";
            dataPropertyRange = options.getSetMode() ? "Set" + genericsString : "Collection" + genericsString;
            return dataPropertyRange;
        }

    }

    /**
     * @param dataPropertyRange
     * @param owlDataRanges
     * @return
     */
    private String getDataPropertyJavaName(OWLDatatype dt) {
        String dataPropertyRange = null;
        if (dt == null) {
            dataPropertyRange = PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE;
        } else {
            dataPropertyRange = getOwlDataTypeAsString(dt, dataPropertyRange);
        }
        return dataPropertyRange;
    }

    /*
     * Synchronize this with CodeGeneratorInference implementations.
     */
    private String getOwlDataTypeAsString(OWLDatatype owlDatatype, String dataPropertyRange) {
    	IRI dataTypeIRI = owlDatatype.getIRI();
    	String dataTypeFragment = dataTypeIRI.getFragment();

    	if (owlDatatype.isBoolean()) {
    		dataPropertyRange = PropertyConstants.BOOLEAN;
    	} else if (owlDatatype.isDouble()) {
    		dataPropertyRange = PropertyConstants.DOUBLE;
    	} else if (owlDatatype.isFloat()) {
    		dataPropertyRange = PropertyConstants.FLOAT;
    	} else if (owlDatatype.isInteger() || dataTypeFragment.trim().equals(PropertyConstants.INT)) {
    		dataPropertyRange = PropertyConstants.INTEGER;
    	} else if (owlDatatype.isString()) {
    		dataPropertyRange = PropertyConstants.STRING;
    	} else {
    		dataPropertyRange = PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE;
    	}
    	return dataPropertyRange;
    }

    /**
     * Prints package statement
     * 
     * @param printWriter
     */
    private void printInterfacePackageStatement(PrintWriter printWriter) {
        if (options.getPackage() != null) {
            printWriter.println("package " + options.getPackage() + ";");
            printWriter.println();
        }
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

    private void createImplementation(OWLClass owlClass) throws IOException {
        String implName = names.getImplementationNamePossiblyAbstract(owlClass);

        File file = getImplementationFile(implName);
        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printImplementationCode(implName, owlClass, printWriter);
        fileWriter.close();

        if (options.getAbstractMode()) {
            createUserImplementation(owlClass);
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

    private void createUserInterface(OWLClass owlClass) throws IOException {
        String userInterfaceName = names.getInterfaceName(owlClass);
        File file = getInterfaceFile(userInterfaceName);
        if (!file.exists()) {
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printUserInterfaceCode(userInterfaceName, owlClass, printWriter);
            fileWriter.close();
        }
    }

    private void printUserInterfaceCode(String userInterfaceName, OWLClass owlClass, PrintWriter printWriter) {
        printInterfacePackageStatement(printWriter);
        printWriter.println("public interface " + names.getInterfaceName(owlClass) + " extends "
                + names.getInterfaceNamePossiblyAbstract(owlClass) + " {");
        printWriter.println("}");

    }

    private void printImplementationCode(String implName, OWLClass owlClass, PrintWriter printWriter) {

        printImplementationPackageStatement(printWriter);
        printWriter.println();
        Collection<OWLObjectProperty> owlObjectProperties = inference.getObjectPropertiesForClass(owlClass);
        Collection<OWLDataProperty> owlDataProperties = inference.getDataPropertiesForClass(owlClass);
        String pack = options.getPackage();
        if (pack != null) {
            printWriter.println("import " + pack + "." + names.getInterfaceNamePossiblyAbstract(owlClass) + ";");
            printWriter.println("import " + pack + ".*;");
            printWriter.println();
        }

        printWriter.println("import java.util.*;");
        printWriter.println();
        printWriter.println("import org.protege.owl.codegeneration.AbstractCodeGeneratorIndividual;");
        printWriter.println("import org.semanticweb.owlapi.model.*;");

        printWriter.println();
        printWriter.println("/**");
        printWriter.println(" * Generated by Protege (http://protege.stanford.edu).");
        printWriter.println(" * Source Class: " + owlClass.getIRI().toString());
        printWriter.println(" *");
        printWriter.println(" * @version generated on " + new Date());
        printWriter.println(" */");

        printWriter.println();
        printWriter.println("public class " + implName + getImplementationExtendsCode(owlClass));
        printWriter.println("         implements " + names.getInterfaceNamePossiblyAbstract(owlClass) + " {");

        printConstructors(printWriter, implName);

        for (Iterator iterator = owlObjectProperties.iterator(); iterator.hasNext();) {
            OWLObjectProperty owlObjectProperty = (OWLObjectProperty) iterator.next();
            printImplementationObjectPropertyCode(owlObjectProperty, printWriter);
            printWriter.println();
        }

        for (Iterator iterator = owlDataProperties.iterator(); iterator.hasNext();) {
            OWLDataProperty owlDataProperty = (OWLDataProperty) iterator.next();
            printImplementationDataPropertyCode(owlClass, owlDataProperty, printWriter);
        }

        printWriter.println();
        printWriter.println("}");

    }

    /**
     * @param printWriter
     */
    private void printImplementationPackageStatement(PrintWriter printWriter) {
        if (options.getPackage() != null) {
            printWriter.println("package " + options.getPackage() + ".impl;");
        } else {
            printWriter.println("package impl;");
        }
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

    private void printConstructors(PrintWriter printWriter, String implementationName) {
        printWriter.println();
        printWriter.println("    public " + implementationName
                + "(OWLDataFactory owlDataFactory, IRI iri, OWLOntology owlOntology) {");
        printWriter.println("        super(owlOntology, iri);");
        printWriter.println("    }");
        printWriter.println();
    }

    private void printImplementationObjectPropertyCode(OWLObjectProperty owlObjectProperty, PrintWriter printWriter) {
        String propertyName = names.getObjectPropertyName(owlObjectProperty);
        String propertyNameUpperCase = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String getPropertyFunctionName = "get" + propertyNameUpperCase + "Property()";
        String objectPropertyRange = getObjectPropertyRange(owlObjectProperty, false);
        boolean isFunctional = owlObjectProperty.isFunctional(allOwlOntologies);

        printWriter.println();
        printWriter.println("    // Property " + owlObjectProperty.getIRI());
        printWriter.println();
        printWriter.println("    public OWLObjectProperty " + getPropertyFunctionName + " {");
        printWriter.println("        final String iriString = \"" + owlObjectProperty.getIRI() + "\";");
        printWriter.println("        final IRI iri = IRI.create(iriString);");
        printWriter.println("        return getOwlOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(iri);");
        printWriter.println("    }");

        printWriter.println();

        printImplObjectPopertyGetMethod(owlObjectProperty, printWriter, propertyNameUpperCase, getPropertyFunctionName,
                objectPropertyRange, isFunctional);

        printWriter.println();
        printWriter.println();
        printWriter.println("    public boolean has" + propertyNameUpperCase + "() {");
        printWriter.println("        Set<OWLIndividual> values = getOwlIndividual().getObjectPropertyValues(" + getPropertyFunctionName
                + ", getOwlOntology());");
        printWriter.println("        return (values == null || values.isEmpty()) ? false : true;");
        printWriter.println("    }");

        if (!isFunctional) {
            Set<OWLClassExpression> oClassExpressions = owlObjectProperty.getRanges(allOwlOntologies);
            String objPropertyJavaName = getObjectPropertyJavaName(oClassExpressions);

            printWriter.println();
            printWriter.println("    public " + PropertyConstants.JAVA_UTIL_ITERATOR + " list" + propertyNameUpperCase
                    + "() {");
            printWriter.println("        return get" + propertyNameUpperCase + "().iterator();");
            printWriter.println("    }");

            printWriter.println();
            printWriter.println("    public void add" + propertyNameUpperCase + "(" + objPropertyJavaName + " new"
                    + propertyNameUpperCase + ") {");
            printWriter
                    .println("        OWLObjectPropertyAssertionAxiom axiom = getOwlOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom( "
                            + getPropertyFunctionName
                            + ", getOwlIndividual(), (OWLIndividual)"
                            + " new"
                            + propertyNameUpperCase
                            + " );");
            printWriter.println("        getOwlOntology().getOWLOntologyManager().addAxiom( getOwlOntology(), axiom);");
            printWriter.println("    }");
            printWriter.println();
            printWriter.println("    public void remove" + propertyNameUpperCase + "(" + objPropertyJavaName + " old"
                    + propertyNameUpperCase + ") {");
            printWriter.println("        removeObjectPropertyValue( " + getPropertyFunctionName + ", (OWLNamedIndividual) old" + propertyNameUpperCase+");");
            printWriter.println("    }");

            printWriter.println();
            printWriter.println("    public void set" + propertyNameUpperCase + "( " + objectPropertyRange + " new"
                    + propertyNameUpperCase + "List ) {");
            printWriter.println("        OWLObjectProperty property = " + getPropertyFunctionName + ";");
            printWriter.println("        " + objectPropertyRange + " prevValues = get" + propertyNameUpperCase + "();");
            printWriter.println("        for (" + objPropertyJavaName + " value : prevValues) {");
            printWriter.println("            removeObjectPropertyValue( property, (OWLNamedIndividual) value);");
            printWriter.println("        }");

            printWriter.println("        for (" + objPropertyJavaName + " element : " + " new" + propertyNameUpperCase
                    + "List" + ") {");
            printWriter
                    .println("            OWLObjectPropertyAssertionAxiom axiom = getOwlOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom( "
                            + getPropertyFunctionName + ", getOwlIndividual(), (OWLIndividual)" + " element);");
            printWriter
                    .println("            getOwlOntology().getOWLOntologyManager().addAxiom(getOwlOntology(), axiom);");
            printWriter.println("        }");
            printWriter.println("    }");

        } else {

            printWriter.println();
            printWriter.println("    public void set" + propertyNameUpperCase + "(" + objectPropertyRange + " new"
                    + propertyNameUpperCase + ") {");
            printWriter.println("        OWLObjectProperty property = " + getPropertyFunctionName + ";");
            printWriter.println("        " + objectPropertyRange + " prevValue = get" + propertyNameUpperCase + "();");
            printWriter.println("        removeObjectPropertyValue( property, (OWLNamedIndividual) prevValue);");
            printWriter
                    .println("        OWLObjectPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLObjectPropertyAssertionAxiom( "
                            + getPropertyFunctionName + ", this, " + "new" + propertyNameUpperCase + ");");
            printWriter.println("        getOwlOntology().getOWLOntologyManager().addAxiom(getOwlOntology(), axiom);");
            printWriter.println("    }");

        }
    }

    /** Prints 
     * @param owlObjectProperty
     * @param printWriter
     * @param propertyNameUpperCase
     * @param getPropertyFunctionName
     * @param objectPropertyRange
     * @param isFunctional
     */
    private void printImplObjectPopertyGetMethod(OWLObjectProperty owlObjectProperty, PrintWriter printWriter,
            String propertyNameUpperCase, String getPropertyFunctionName, String objectPropertyRange,
            boolean isFunctional) {
        printWriter.println("    public " + objectPropertyRange + " get" + propertyNameUpperCase + "() {");
        if (!isFunctional) {
            Set<OWLClassExpression> oClassExpressions = owlObjectProperty.getRanges(allOwlOntologies);
            String objectPropertyType = getObjectPropertyJavaName(oClassExpressions);

            printWriter.println("        Set<" + objectPropertyType + "> propertyValues = new HashSet<"
                    + objectPropertyType + ">();");
            printWriter.println("        Set<OWLIndividual> values = getOwlIndividual().getObjectPropertyValues("
                    + getPropertyFunctionName + ", getOwlOntology());");
            printWriter.println("        if(values ==null || values.isEmpty()){");
            printWriter.println("            return null;");
            printWriter.println("        }");
            printWriter.println("        for (OWLIndividual owlIndividual : values) {");
            if (objectPropertyType.equals(PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE)) {
                printWriter.println("  propertyValues.add(owlIndividual);");
            } else {
                printWriter.println("            propertyValues.add(new Default" + objectPropertyType
                        + "(getOWLDataFactory(), owlIndividual.asOWLNamedIndividual().getIRI(), getOwlOntology()));");
            }
            printWriter.println("        }");
            printWriter.println("        return propertyValues;");

        } else {
            printWriter.println("        Set<OWLIndividual> values = getOwlIndividual().getObjectPropertyValues("
                    + getPropertyFunctionName + ", getOwlOntology());");
            printWriter.println("        if (values == null || values.isEmpty()) {");
            printWriter.println("            return null;");
            printWriter.println("        }");
            printWriter.println("        for (OWLIndividual owlIndividual : values) {");
            printWriter.println("            return new Default" + objectPropertyRange
                    + "(getOWLDataFactory(), owlIndividual.asOWLNamedIndividual().getIRI(), getOwlOntology());");
            printWriter.println("        }");
            printWriter.println("        return null;");
        }
        printWriter.println("    }");
    }

    /** 
     * Creates implementation when 'create abstract base file' option is true
     * @param owlClass
     * @throws IOException
     */
    private void createUserImplementation(OWLClass owlClass) throws IOException {
        String implName = names.getImplementationName(owlClass);
        File file = getImplementationFile(implName);
        if (!file.exists()) {
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printUserImplementationCode(implName, owlClass, printWriter);
            fileWriter.close();
        }
    }

    /**Prints implementation code when 'create abstract base file' option is true
     * @param implName
     * @param owlClass
     * @param printWriter
     */
    private void printUserImplementationCode(String implName, OWLClass owlClass, PrintWriter printWriter) {
        printImplementationPackageStatement(printWriter);

        String pack = options.getPackage();
        if (pack != null) {
            printWriter.println("import " + pack + ".*;");
            printWriter.println();
        }
        printWriter.println("import org.semanticweb.owlapi.model.*;");
        printWriter.println();
        printWriter.println("public class " + implName + " extends " + names.getImplementationNamePossiblyAbstract(owlClass));
        printWriter.println("         implements " + names.getInterfaceName(owlClass) + " {");
        printConstructors(printWriter, implName);
        printWriter.println("}");

    }

    /** Prints code for data properties for OWLClass implementation
     * @param owlDataProperty
     * @param printWriter
     */
    private void printImplementationDataPropertyCode(OWLClass owlClass, OWLDataProperty owlDataProperty, PrintWriter printWriter) {

        String propertyName = names.getDataPropertyName(owlDataProperty);
        String propertyNameUpperCase = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String getPropertyFunctionName = "get" + propertyNameUpperCase + "Property()";

        OWLDatatype dt = inference.getRange(owlClass, owlDataProperty);
        String dataPropertyJavaName = getDataPropertyJavaName(dt);

        boolean isDataTypeBasic = isDataTypeBasic(owlDataProperty);

        boolean isFunctional = owlDataProperty.isFunctional(allOwlOntologies);
        printWriter.println();
        printWriter.println();
        printWriter.println("    // Property " + owlDataProperty.getIRI());
        printWriter.println();
        printWriter.println("    public " + "OWLDataProperty " + getPropertyFunctionName + " {");
        printWriter.println("        final String iriString = \"" + owlDataProperty.getIRI() + "\";");
        printWriter.println("        final IRI iri = IRI.create(iriString);");
        printWriter.println("        return getOWLDataFactory().getOWLDataProperty(iri);");
        printWriter.println("    }");
        printWriter.println();

        printImplDataPropertyGetterCode(owlClass, owlDataProperty, printWriter, propertyNameUpperCase, getPropertyFunctionName,
                dataPropertyJavaName, isFunctional);

        printWriter.println("    public boolean has" + propertyNameUpperCase + "(){");
        printWriter.println("        " + getDataPropertyRange(owlClass, owlDataProperty) + " value = get" + propertyNameUpperCase
                + "();");
        if (isFunctional) {
            printWriter.println("        return ( value == null )? false : true;");
        } else {
            printWriter.println("        return ( value == null || value.isEmpty() )? false : true;");
        }
        printWriter.println("    }");

        if (!owlDataProperty.isFunctional(allOwlOntologies)) {
            printWriter.println();
            printWriter.println("    public " + PropertyConstants.JAVA_UTIL_ITERATOR + " list" + propertyNameUpperCase
                    + "() {");
            printWriter.println("        return get" + propertyNameUpperCase + "().iterator();");
            printWriter.println("    }");
            printWriter.println();
            printWriter.println("    public void add" + propertyNameUpperCase + "("
                    + getDataPropertyJavaName(dt) + " new" + propertyNameUpperCase + "){");
            if (isDataTypeBasic) {
                printWriter.println("        OWLLiteral literal = getOWLDataFactory().getOWLLiteral( new"
                        + propertyNameUpperCase + ");");
            } else {
                printWriter.println("        OWLLiteral literal = getOWLDataFactory().getOWLLiteral( new"
                        + propertyNameUpperCase + ".toString(), \"\");");
            }
            printWriter
                    .println("        if (!doesPropertyContainsLiteral(" + getPropertyFunctionName + ", literal)) {");
            printWriter
                    .println("            OWLDataPropertyAssertionAxiom axiom = getOwlOntology().getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(");
            printWriter.println("                " + getPropertyFunctionName + ", getOwlIndividual(), literal);");
            printWriter
                    .println("            getOwlOntology().getOWLOntologyManager().addAxiom(getOwlOntology(), axiom);");
            printWriter.println("        }");
            printWriter.println("    }");
            printWriter.println();
            printWriter.println("    public void remove" + propertyNameUpperCase + "("
                    + getDataPropertyJavaName(dt) + " old" + propertyNameUpperCase + "){");
            if (isDataTypeBasic) {
                printWriter.println("        OWLLiteral literal = getOWLDataFactory().getOWLLiteral( old"
                        + propertyNameUpperCase + ");");
            } else {
                printWriter.println("        OWLLiteral literal = getOWLDataFactory().getOWLLiteral( old"
                        + propertyNameUpperCase + ".toString(),\"\");");
            }
            printWriter.println("        removeDataPropertyValue(" + getPropertyFunctionName + ", literal);");
            printWriter.println("    }");

        }
        printWriter.println();

        printImplDataPropertySetterCode(owlClass, owlDataProperty, printWriter, propertyNameUpperCase, getPropertyFunctionName,
                dataPropertyJavaName, isDataTypeBasic, isFunctional);
        printWriter.println("    }");

    }

    /**
     * Prints getter code for the OWLDataProperty for a particular OWLClass
     * @param owlDataProperty
     * @param printWriter
     * @param propertyNameUpperCase
     * @param getPropertyFunctionName
     * @param dataPropertyJavaName
     * @param isFunctional
     */
    private void printImplDataPropertyGetterCode(OWLClass owlClass, OWLDataProperty owlDataProperty, PrintWriter printWriter,
            String propertyNameUpperCase, String getPropertyFunctionName, String dataPropertyJavaName,
            boolean isFunctional) {
        printWriter.println("    public " + getDataPropertyRange(owlClass, owlDataProperty) + " get" + propertyNameUpperCase
                + "() {");

        if (isFunctional) {

            printWriter.println("        Set<OWLLiteral> propertyValues = getDataPropertyValues( "
                    + getPropertyFunctionName + ", getOwlOntology());");
            printWriter.println("        for (OWLLiteral owlLiteral : propertyValues) {");
            if (dataPropertyJavaName.equalsIgnoreCase(PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE)) {
                printWriter.println("            return (" + dataPropertyJavaName + ") owlLiteral.getLiteral();");
            } else {
                printWriter.println("            return new " + dataPropertyJavaName + "( owlLiteral.getLiteral());");
            }
            printWriter.println("        }");
            printWriter.println("        return null;");
        } else {
            printWriter.println("        Set<OWLLiteral> propertyValues = getDataPropertyValues( "
                    + getPropertyFunctionName + ", getOwlOntology());");
            printWriter.println("        Set<" + dataPropertyJavaName + "> values = new HashSet<"
                    + dataPropertyJavaName + ">();");
            printWriter.println("        for (OWLLiteral owlLiteral : propertyValues) {");
            if (dataPropertyJavaName.equalsIgnoreCase(PropertyConstants.UNKNOWN_JAVA_OBJECT_TYPE)) {
                printWriter.println("            values.add( (" + dataPropertyJavaName + ")owlLiteral.getLiteral());");
            } else {
                printWriter.println("            values.add( new " + dataPropertyJavaName
                        + "( owlLiteral.getLiteral()));");
            }
            printWriter.println("        }");
            printWriter.println("        return values;");
        }
        printWriter.println("    }");
        printWriter.println();
    }

    /** Prints setter code for the OWLDataProperty for a particular OWLClass
     * @param owlDataProperty
     * @param printWriter
     * @param propertyNameUpperCase
     * @param getPropertyFunctionName
     * @param dataPropertyJavaName
     * @param isDataTypeBasic
     * @param isFunctional
     */
    private void printImplDataPropertySetterCode(OWLClass owlClass, OWLDataProperty owlDataProperty, PrintWriter printWriter,
            String propertyNameUpperCase, String getPropertyFunctionName, String dataPropertyJavaName,
            boolean isDataTypeBasic, boolean isFunctional) {
        if (isFunctional) {
            printWriter.println("    public void set" + propertyNameUpperCase + "("
                    + getDataPropertyRange(owlClass, owlDataProperty) + " new" + propertyNameUpperCase + "){");
            printWriter.println("        " + getDataPropertyRange(owlClass, owlDataProperty) + " prevValue = get"
                    + propertyNameUpperCase + "()" + ";");
            printWriter.println("        //Remove previous value/values");
            printWriter.println("        if (prevValue != null) {");
            if (isDataTypeBasic) {
                printWriter
                        .println("            OWLLiteral literalToRemove = getOWLDataFactory().getOWLLiteral(prevValue);");
                printWriter.println("            removeDataPropertyValue( "+getPropertyFunctionName+", literalToRemove);");
                printWriter.println("        }");
                printWriter.println("    OWLLiteral literal = getOWLDataFactory().getOWLLiteral((" + " new"
                        + propertyNameUpperCase + " == null) ? null : " + " new" + propertyNameUpperCase + ");");

            } else {
                printWriter
                        .println("            OWLLiteral literalToRemove = getOWLDataFactory().getOWLLiteral(prevValue.toString(), \"\");");
                printWriter.println("            removeDataPropertyValue( "+getPropertyFunctionName+", literalToRemove);");
                printWriter.println("        }");
                printWriter.println("    OWLLiteral literal = getOWLDataFactory().getOWLLiteral((" + " new"
                        + propertyNameUpperCase + " == null) ? null : " + " new" + propertyNameUpperCase
                        + ".toString(), \"\");");

            }
            printWriter.println("    setDataProperty(" + getPropertyFunctionName + ", literal);");

        } else {
            printWriter.println("    public void set" + propertyNameUpperCase + "("
                    + getDataPropertyRange(owlClass, owlDataProperty) + " new" + propertyNameUpperCase + "List ){");
            printWriter.println("        " + getDataPropertyRange(owlClass, owlDataProperty) + " prevValueList = get"
                    + propertyNameUpperCase + "();");
            printWriter.println("        if (prevValueList != null) {");
            printWriter.println("            for (" + dataPropertyJavaName + " prevValue : prevValueList) {");
            if (isDataTypeBasic) {
                printWriter
                        .println("                OWLLiteral literalToRemove = getOWLDataFactory().getOWLLiteral(prevValue);");
            } else {
                printWriter
                        .println("                OWLLiteral literalToRemove = getOWLDataFactory().getOWLLiteral(prevValue.toString(), \"\");");
            }

            printWriter.println("                removeDataPropertyValue( "+getPropertyFunctionName+", literalToRemove );");
            printWriter.println("            }");
            printWriter.println("        }");
            printWriter.println("        for (" + dataPropertyJavaName + " value : new" + propertyNameUpperCase
                    + "List ) {");
            if (isDataTypeBasic) {
                printWriter.println("            OWLLiteral literal = getOWLDataFactory().getOWLLiteral(value);");
            } else {
                printWriter
                        .println("            OWLLiteral literal = getOWLDataFactory().getOWLLiteral(value.toString(), \"\");");
            }
            printWriter.println("            setDataProperty(" + getPropertyFunctionName + ", literal);");
            printWriter.println("        }");

        }
    }

    /** Determines whether the data type for the OWLDataProperty is basic or not
     * @param owlDataProperty
     * @return
     */
    private boolean isDataTypeBasic(OWLDataProperty owlDataProperty) {
        Set<OWLDataRange> ranges = owlDataProperty.asOWLDataProperty().getRanges(allOwlOntologies);
        if (ranges == null || ranges.isEmpty() || ranges.size() > 1) {
            return false;
        }
        for (OWLDataRange owlDataRange : ranges) {
            OWLDatatype owlDatatype = owlDataRange.asOWLDatatype();
            if (owlDatatype.isBoolean() || owlDatatype.isDouble() || owlDatatype.isFloat() || owlDatatype.isInteger()
                    || owlDatatype.isString()) {
                return true;
            }
        }
        return false;
    }

    /** Initilizes the vocabulary code generation 
     * @param owlClassList
     * @throws IOException
     */
    private void printVocabularyCode(Collection<OWLClass> owlClassList) throws IOException {
        createVocabularyClassFile();
        printVocabularyInitialCode();

        vocabularyPrintWriter
                .println("    private static final OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();");
        vocabularyPrintWriter.println();

        for (Iterator<OWLClass> iterator = owlClassList.iterator(); iterator.hasNext();) {
            OWLClass owlClass = iterator.next();
            printClassVocabularyCode(owlClass);
        }

        for (OWLObjectProperty owlObjectProperty : objectProperties) {
            String propertyName = names.getObjectPropertyName(owlObjectProperty);
            vocabularyPrintWriter
                    .println("    public static final OWLObjectProperty " + propertyName.toUpperCase()
                            + " = factory.getOWLObjectProperty(IRI.create(\"" + owlObjectProperty.getIRI().toString()
                            + "\"));");
            vocabularyPrintWriter.println();
        }

        for (OWLDataProperty owlDataProperty : dataProperties) {
            String propertyName = names.getDataPropertyName(owlDataProperty);
            vocabularyPrintWriter.println("    public static final OWLDataProperty " + propertyName.toUpperCase()
                    + " = factory.getOWLDataProperty(IRI.create(\"" + owlDataProperty.getIRI().toString() + "\"));");
            vocabularyPrintWriter.println();
        }

        printVocabularyEndCode();
    }

    /**Creates file for Vocabulary class
     * @throws IOException
     */
    private void createVocabularyClassFile() throws IOException {
        File vocabularyFile = getInterfaceFile(JavaCodeGeneratorConstants.VOCABULARY_CLASS_NAME);
        vocabularyfileWriter = new FileWriter(vocabularyFile);
        vocabularyPrintWriter = new PrintWriter(vocabularyfileWriter);

    }

    /**
     * Prints the initial code for Vocabulary class
     */
    private void printVocabularyInitialCode() {
        printInterfacePackageStatement(vocabularyPrintWriter);
        vocabularyPrintWriter.println("import org.semanticweb.owlapi.apibinding.OWLManager;");
        vocabularyPrintWriter.println("import org.semanticweb.owlapi.model.*;");
        vocabularyPrintWriter.println();
        vocabularyPrintWriter.println("/**");
        vocabularyPrintWriter.println(" * Generated by Protege (http://protege.stanford.edu).");
        vocabularyPrintWriter.println(" * Source Class: Vocabulary");
        vocabularyPrintWriter.println(" *");
        vocabularyPrintWriter.println(" * @version generated on " + new Date());
        vocabularyPrintWriter.println(" */");
        vocabularyPrintWriter.println();
        vocabularyPrintWriter.println("public class " + JavaCodeGeneratorConstants.VOCABULARY_CLASS_NAME + " {");
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
        printFactoryInitialCode(factoryPrintWriter);

        for (Iterator<OWLClass> iterator = owlClassList.iterator(); iterator.hasNext();) {
            OWLClass owlClass = iterator.next();
            printFactoryCodeForClass(owlClass, factoryPrintWriter);
        }
        printFactoryClassEndCode(factoryPrintWriter, factoryFileWriter);
    }

    /**Prints the initial code for factory class
     * @param factoryPrintWriter
     */
    private void printFactoryInitialCode(PrintWriter factoryPrintWriter) {
        printInterfacePackageStatement(factoryPrintWriter);
        factoryPrintWriter.println("import java.util.*;");
        factoryPrintWriter.println("import org.semanticweb.owlapi.model.*;");
        factoryPrintWriter.println();
        factoryPrintWriter.println();
        if (options.getPackage() != null) {
            factoryPrintWriter.println("import static " + options.getPackage() + "."
                    + JavaCodeGeneratorConstants.VOCABULARY_CLASS_NAME + ".*;");
            factoryPrintWriter.println("import " + options.getPackage() + ".impl.*;");
        } else {
            factoryPrintWriter.println("import static " + JavaCodeGeneratorConstants.VOCABULARY_CLASS_NAME + ".*;");
            factoryPrintWriter.println("import impl.*;");
        }

        factoryPrintWriter.println();
        factoryPrintWriter.println("/**");
        factoryPrintWriter.println(" * Generated by Protege (http://protege.stanford.edu).");
        factoryPrintWriter.println(" * Source Class: Factory");
        factoryPrintWriter.println(" *");
        factoryPrintWriter.println(" * @version generated on " + new Date());
        factoryPrintWriter.println(" */");
        factoryPrintWriter.println();
        factoryPrintWriter.println("public class " + options.getFactoryClassName().trim() + " {");
        factoryPrintWriter.println();
        factoryPrintWriter.println("    private OWLOntology owlOntology;");
        factoryPrintWriter.println();
        factoryPrintWriter
                .println("    public " + options.getFactoryClassName().trim() + "(OWLOntology owlOntology) {");
        factoryPrintWriter.println("        this.owlOntology = owlOntology;");
        factoryPrintWriter.println("    }");

    }

    /** Prints the factory code for the provided OWLClass to the PrintStream
     * @param owlClass
     * @param factoryPrintWriter
     */
    private void printFactoryCodeForClass(OWLClass owlClass, PrintWriter factoryPrintWriter) {
        String implName = names.getImplementationName(owlClass);
        String className = names.getInterfaceName(owlClass);

        factoryPrintWriter.println("    public " + className + " create" + className + "(String name) {");
        factoryPrintWriter
                .println("        IRI iri = IRI.create(owlOntology.getOntologyID().getOntologyIRI().toString() + \"#\" + name);");
        factoryPrintWriter.println("        " + implName + " entity = new " + implName
                + "(owlOntology.getOWLOntologyManager().getOWLDataFactory(), iri, owlOntology);");
        factoryPrintWriter
                .println("        OWLClassAssertionAxiom axiom = owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom("
                        + className.toUpperCase() + ", entity.getOwlIndividual()); ");
        factoryPrintWriter.println("        owlOntology.getOWLOntologyManager().addAxiom(owlOntology, axiom);");
        factoryPrintWriter.println("        return entity;");
        factoryPrintWriter.println("    }");
        factoryPrintWriter.println();

        factoryPrintWriter.println("    public " + className + " get" + className + "(String name) {");
        factoryPrintWriter.println("        Set<OWLIndividual> individuals =" + className.toUpperCase()
                + ".getIndividuals(owlOntology);");
        factoryPrintWriter.println("        if(individuals == null) {");
        factoryPrintWriter.println("            return null;");
        factoryPrintWriter.println("        }");
        factoryPrintWriter.println("        for (OWLIndividual owlIndividual : individuals) {");
        factoryPrintWriter
                .println("            String fragment = owlIndividual.asOWLNamedIndividual().getIRI().getFragment();");
        factoryPrintWriter.println("            if(fragment.trim().equals(name.trim())){");
        factoryPrintWriter
                .println("                return  new Default"
                        + className
                        + "(owlOntology.getOWLOntologyManager().getOWLDataFactory(), owlIndividual.asOWLNamedIndividual().getIRI(), owlOntology);");
        factoryPrintWriter.println("            }");
        factoryPrintWriter.println("        }");
        factoryPrintWriter.println("        return null;");
        factoryPrintWriter.println("    }");
        factoryPrintWriter.println();

        factoryPrintWriter.println("    public Collection<" + className + "> getAll" + className + "Instance(){");
        factoryPrintWriter.println("        Collection<" + className + "> instances = new HashSet<" + className
                + ">();");
        factoryPrintWriter.println("        Set<OWLIndividual> individuals =" + className.toUpperCase()
                + ".getIndividuals(owlOntology);");
        factoryPrintWriter.println("        for (OWLIndividual owlIndividual : individuals) {");
        factoryPrintWriter
                .println("            instances.add(new Default"
                        + className
                        + "(owlOntology.getOWLOntologyManager().getOWLDataFactory(), owlIndividual.asOWLNamedIndividual().getIRI(), owlOntology));");
        factoryPrintWriter.println("        }");
        factoryPrintWriter.println("        return instances;");
        factoryPrintWriter.println("    }");
        factoryPrintWriter.println();
    }

    /** Prints the terminating code
     * @param factoryPrintWriter
     * @param factoryFileWriter
     * @throws IOException
     */
    private void printFactoryClassEndCode(PrintWriter factoryPrintWriter, FileWriter factoryFileWriter)
            throws IOException {
        factoryPrintWriter.println(" }");
        factoryFileWriter.close();
    }

    /** Returns base implementation of the provided OWLClass
     * @param owlClass
     * @return
     */
    private String getBaseImplementation(OWLClass owlClass) {
        String baseImplementationString = "";
        for (OWLClassExpression owlClassExpression : owlClass.getSuperClasses(allOwlOntologies)) {
            if (owlClassExpression.isAnonymous()) {
                continue;
            }
            OWLClass superClass = owlClassExpression.asOWLClass();
            if (superClass != null && !superClass.isTopEntity() && !superClass.isBuiltIn()) {
                if (baseImplementationString.equals("")) {
                    baseImplementationString = names.getImplementationName(superClass);
                } else {
                    return null;
                }
            }
        }
        if (baseImplementationString.equals("")) {
            return null;
        } else {
            return baseImplementationString;
        }
    }

    /** Returns base interface of the provided OWLClass
     * @param owlClass The OWLClass whose base interface is to be returned
     * @return
     */
    private String getBaseInterface(OWLClass owlClass) {
        String baseInterfaceString = "";
        for (OWLClassExpression owlClassExpression : owlClass.getSuperClasses(allOwlOntologies)) {
            if (owlClassExpression.isAnonymous()) {
                continue;
            }
            OWLClass superClass = owlClassExpression.asOWLClass();
            if (superClass != null && !superClass.isTopEntity() && !superClass.isBuiltIn()) {
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
