package org.protege.editor.owl.codegeneration;

import java.io.File;

import org.protege.editor.owl.codegeneration.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 * A class that can create Java interfaces in the Protege-OWL format from an OWL
 * model.
 * 
 * @author z.khan
 * 
 */
public class JavaCodeGenerator {

    private JavaCodeGeneratorOptions options;

    private static final boolean TRANSITIVE = true;
    private static final boolean ALL_PROPERTIES = TRANSITIVE;
    private static final boolean ONLY_LOCAL_PROPERTIES = !TRANSITIVE;

    List<Node<OWLClass>> classesNodeList;
    private OWLReasoner reasoner;
    private IRI iri;
    private OWLDataFactory owlDataFactory;
    private OWLOntology owlOntology;
    private Set<OWLObjectProperty> objectProperties;
    private Set<OWLDataProperty> dataProperties;
    private DefaultPrefixManager prefixManager;

    public JavaCodeGenerator(OWLOntology owlOntology, JavaCodeGeneratorOptions options) {

        this.owlOntology = owlOntology;
        this.options = options;
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

    public void createAll(OWLReasoner reasoner) throws IOException {

        this.reasoner = reasoner;
        getOntologyObjectProperties();
        getOntologyDataProperties();
        Node<OWLClass> topNode = this.reasoner.getTopClassNode();

        List<OWLClass> owlClassList = getClassesList(topNode);
        for (Iterator iterator = owlClassList.iterator(); iterator.hasNext();) {
            OWLClass owlClass = (OWLClass) iterator.next();
            IRI classIri = owlClass.getIRI();

            createInterface(owlClass);
            createImplementation(owlClass);
        }
    }

    /**
     * @param owlClass
     * @return
     */
    protected String getInterfaceName(OWLClass owlClass) {
        if (prefixManager == null) {
            prefixManager = new DefaultPrefixManager(iri.toString() + "#");
        }
        String interfaceName = prefixManager.getShortForm(owlClass);
        interfaceName = interfaceName.replace(":", "");
        return interfaceName;
    }

    protected String getInterfaceNamePossiblyAbstract(OWLClass owlClass) {
        String interfaceName = getInterfaceName(owlClass);
        if (options.getAbstractMode()) {
            interfaceName += "_";
        }
        return interfaceName;
    }

    protected String getObjectPropertyName(OWLObjectProperty owlObjectProperty) {
        if (prefixManager == null) {
            prefixManager = new DefaultPrefixManager(iri.toString() + "#");
        }

        String propertyName = prefixManager.getShortForm(owlObjectProperty);
        propertyName = propertyName.replace(":", "");
        return propertyName;
    }

    /**
     * Gets all the Object Properties of the Ontology
     */
    private void getOntologyObjectProperties() {
        objectProperties = owlOntology.getObjectPropertiesInSignature();

    }

    /**
     * Gets all the Data Properties of the Ontology
     */
    private void getOntologyDataProperties() {
        dataProperties = owlOntology.getDataPropertiesInSignature();
    }

    /**
     * Generates Interfaces
     * 
     * @param owlClass
     * @param prefixManager
     * @throws IOException
     */
    private void createInterface(OWLClass owlClass) throws IOException {

        String interfaceName = getInterfaceNamePossiblyAbstract(owlClass);
        File baseFile = getInterfaceFile(interfaceName);
        FileWriter fileWriter = new FileWriter(baseFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printInterfaceCode(interfaceName, owlClass, printWriter);
        fileWriter.close();

        if (options.getAbstractMode()) {
            createUserInterface(owlClass);
        }
    }

    private void printInterfaceCode(String className, OWLClass owlClass, PrintWriter printWriter) {
        printInterfacePackageStatement(printWriter);

        List<OWLObjectProperty> owlObjectProperties = getClassObjectProperties(owlClass);
        List<OWLDataProperty> owlDataProperties = getClassDataProperties(owlClass);

        printWriter.println("import org.semanticweb.owlapi.model.*;");
        printWriter.println();

        addImportJavaUtilCode(printWriter, owlDataProperties, owlObjectProperties);

        printWriter.println("/**");
        printWriter.println(" * Generated by Protege (http://protege.stanford.edu).");
        printWriter.println(" * Source Class: " + className);
        printWriter.println(" *");
        printWriter.println(" * @version generated on " + new Date());
        printWriter.println(" */");
        printWriter.println("public interface " + className + getInterfaceExtendsCode(owlClass) + " {");

        for (Iterator iterator = owlObjectProperties.iterator(); iterator.hasNext();) {
            OWLObjectProperty owlObjectProperty = (OWLObjectProperty) iterator.next();
            printInterfaceObjectPropertyCode(owlObjectProperty, printWriter);
            printWriter.println();

        }
        for (Iterator iterator = owlDataProperties.iterator(); iterator.hasNext();) {
            OWLDataProperty owlDataProperty = (OWLDataProperty) iterator.next();
            printInterfaceDataPropertyCode(owlDataProperty, printWriter);
        }
        printWriter.println();
        printWriter.println("    void delete();");
        printWriter.println("}");

    }

    private void printInterfaceObjectPropertyCode(OWLObjectProperty owlObjectProperty, PrintWriter printWriter) {
        String propertyName = getObjectPropertyName(owlObjectProperty);
        String propertyNameUpperCase = getInitialLetterAsUpperCase(propertyName);
        printWriter.println();
        printWriter.println("    // Property " + owlObjectProperty.getIRI());
        printWriter.println();
        printWriter.println("    " + getObjectPropertyRange(owlObjectProperty, false) + " get" + propertyNameUpperCase
                + "();");
        printWriter.println();
        printWriter.println("    " + "OWLObjectProperty get" + propertyNameUpperCase + "Property();");
        printWriter.println();
        printWriter.println("    boolean has" + propertyNameUpperCase + "();");

        if (!owlObjectProperty.isFunctional(owlOntology)) {
            Set<OWLClassExpression> oClassExpressions = owlObjectProperty.getRanges(owlOntology);
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

    private String getObjectPropertyRange(OWLObjectProperty owlObjectProperty, boolean useExtends) {
        Set<OWLClassExpression> oClassExpressions = owlObjectProperty.getRanges(owlOntology);
        String objPropertyRange = getObjectPropertyJavaName(oClassExpressions);
        if (owlObjectProperty.isFunctional(owlOntology)) {// property can contain only single value
            return objPropertyRange;
        } else {//Property contains multiple values

            if (oClassExpressions.size() > 1) { // Contains More than 1 range hence disable use of extends
                useExtends = false;
            }
//            String genericsString = objPropertyRange.equals(PropertyConstants.JAVA_LANG_OBJECT) ? "<?>"
//                    : useExtends ? "<? extends " + objPropertyRange + ">" : "<" + objPropertyRange + ">";
            String genericsString = objPropertyRange.equals(PropertyConstants.JAVA_LANG_OBJECT) ? ""
                    : useExtends ? "<? extends " + objPropertyRange + ">" : "<" + objPropertyRange + ">";
            objPropertyRange = options.getSetMode() ? "Set" + genericsString : "Collection" + genericsString;
            return objPropertyRange;
        }
    }

    /**
     * @param objPropertyRange
     * @param oClassExpressions
     * @return
     */
    private String getObjectPropertyJavaName(Set<OWLClassExpression> oClassExpressions) {
        String objPropertyRange = null;
        if (oClassExpressions == null || oClassExpressions.isEmpty() || oClassExpressions.size() > 1) {// If count of range is other then One (zero of more than 1 ) then return range as java.lang.Object
            //return PropertyConstants.JAVA_LANG_OBJECT;
            objPropertyRange = PropertyConstants.JAVA_LANG_OBJECT;
        } else {
            for (OWLClassExpression owlClassExpression : oClassExpressions) {
                try {
                    OWLClass owlClass = owlClassExpression.asOWLClass();
                    objPropertyRange = getInterfaceName(owlClass);
                    break;
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return objPropertyRange;
    }

    private void printInterfaceDataPropertyCode(OWLDataProperty owlDataProperty, PrintWriter printWriter) {
        String propertyName = getDataPropertyName(owlDataProperty);
        String propertyNameUpperCase = getInitialLetterAsUpperCase(propertyName);
        printWriter.println();
        printWriter.println();
        printWriter.println("    // Property " + owlDataProperty.getIRI());
        printWriter.println();
        printWriter.println("    " + getDataPropertyRange(owlDataProperty) + " get" + propertyNameUpperCase + "();");
        printWriter.println();
        printWriter.println("    " + "OWLDataProperty get" + propertyNameUpperCase + "Property();");
        printWriter.println();
        printWriter.println("    boolean has" + propertyNameUpperCase + "();");
        if (!owlDataProperty.isFunctional(owlOntology)) {
            Set<OWLDataRange> owlDataRanges = owlDataProperty.getRanges(owlOntology);
            printWriter.println();
            printWriter
                    .println("    " + PropertyConstants.JAVA_UTIL_ITERATOR + " list" + propertyNameUpperCase + "();");
            printWriter.println();
            printWriter.println("    void add" + propertyNameUpperCase + "(" + getDataPropertyJavaName(owlDataRanges)
                    + " new" + propertyNameUpperCase + ");");
            printWriter.println();
            printWriter.println("    void remove" + propertyNameUpperCase + "("
                    + getDataPropertyJavaName(owlDataRanges) + " old" + propertyNameUpperCase + ");");

        }
        printWriter.println();
        printWriter.println("    void set" + propertyNameUpperCase + "(" + getDataPropertyRange(owlDataProperty)
                + " new" + propertyNameUpperCase + ");");

    }

    private String getDataPropertyRange(OWLDataProperty owlDataProperty) {
        Set<OWLDataRange> owlDataRanges = owlDataProperty.getRanges(owlOntology);
        String dataPropertyRange = getDataPropertyJavaName(owlDataRanges);

        if (owlDataProperty.isFunctional(owlOntology)) {// property can contain only single value
            return dataPropertyRange;
        } else {//Property contains multiple values
            //            if (owlDataRanges.size() > 1) { // Contains More than 1 range hence disable use of extends
            //                useExtends = false;
            //                
            //            }
//            String genericsString = dataPropertyRange.equals(PropertyConstants.JAVA_LANG_OBJECT) ? "<?>" : "<"
//                    + dataPropertyRange + ">";
            String genericsString = dataPropertyRange.equals(PropertyConstants.JAVA_LANG_OBJECT) ? "" : "<"
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
    private String getDataPropertyJavaName(Set<OWLDataRange> owlDataRanges) {
        String dataPropertyRange = null;
        if (owlDataRanges == null || owlDataRanges.isEmpty() || owlDataRanges.size() > 1) {
            dataPropertyRange = PropertyConstants.JAVA_LANG_OBJECT;
        } else {
            dataPropertyRange = getOwlDataTypeAsString(owlDataRanges, dataPropertyRange);
        }
        return dataPropertyRange;
    }

    /**
     * @param owlDataRanges
     * @param dataPropertyRange
     * @return
     */
    private String getOwlDataTypeAsString(Set<OWLDataRange> owlDataRanges, String dataPropertyRange) {
        for (OWLDataRange owlDataRange : owlDataRanges) {
            OWLDatatype owlDatatype = owlDataRange.asOWLDatatype();
            DataRangeType s = owlDatatype.getDataRangeType();
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
            }
            //            else if (dataTypeFragment.trim().equals(PropertyConstants.INT)) {
            //                dataPropertyRange = PropertyConstants.INT;
            //            } else if (dataTypeFragment.trim().equals(PropertyConstants.BYTE)) {
            //                dataPropertyRange = PropertyConstants.BYTE;
            //            } else if (dataTypeFragment.trim().equals(PropertyConstants.LONG)) {
            //                dataPropertyRange = PropertyConstants.LONG;
            //            } else if (dataTypeFragment.trim().equals(PropertyConstants.SHORT)) {
            //                dataPropertyRange = PropertyConstants.SHORT;
            //            } 
            else {
                dataPropertyRange = PropertyConstants.JAVA_LANG_OBJECT;
            }
            break;
        }
        return dataPropertyRange;
    }

    protected String getDataPropertyName(OWLDataProperty owlDataProperty) {
        if (prefixManager == null) {
            prefixManager = new DefaultPrefixManager(iri.toString() + "#");
        }

        String propertyName = prefixManager.getShortForm(owlDataProperty);
        propertyName = propertyName.replace(":", "");
        return propertyName;
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

    private List<OWLObjectProperty> getClassObjectProperties(OWLClass owlClass) {

        List<OWLObjectProperty> owlObjectProperties = new ArrayList<OWLObjectProperty>();

        for (Iterator iterator = objectProperties.iterator(); iterator.hasNext();) {
            OWLObjectProperty owlObjectProperty = (OWLObjectProperty) iterator.next();
            Set<OWLClassExpression> owlClassExpressions = owlObjectProperty.getDomains(owlOntology);

            for (Iterator iterator2 = owlClassExpressions.iterator(); iterator2.hasNext();) {
                OWLClassExpression owlClassExpression = (OWLClassExpression) iterator2.next();
                OWLClass owlCls = owlClassExpression.asOWLClass();
                if (owlClass.getIRI().toString().trim().equals(owlCls.getIRI().toString().trim())) {
                    owlObjectProperties.add(owlObjectProperty);
                    break;
                }
            }
        }
        Set<OWLClassExpression> sc = owlClass.getSuperClasses(owlOntology);
        for (OWLClassExpression owlClassExpression : sc) {
            if (owlClassExpression.isAnonymous()) {
                Set<OWLObjectProperty> s = owlClassExpression.getObjectPropertiesInSignature();
                for (OWLObjectProperty owlObjectProperty : s) {
                    if (!owlObjectProperties.contains(owlObjectProperty)) {
                        owlObjectProperties.add(owlObjectProperty);
                    }
                }
            }
        }
        return owlObjectProperties;
    }

    private List<OWLDataProperty> getClassDataProperties(OWLClass owlClass) {
        List<OWLDataProperty> owlDataProperties = new ArrayList<OWLDataProperty>();

        for (Iterator iterator = dataProperties.iterator(); iterator.hasNext();) {
            OWLDataProperty owlDataProperty = (OWLDataProperty) iterator.next();
            Set<OWLClassExpression> owlClassExpressions = owlDataProperty.getDomains(owlOntology);

            for (Iterator iterator2 = owlClassExpressions.iterator(); iterator2.hasNext();) {
                OWLClassExpression owlClassExpression = (OWLClassExpression) iterator2.next();
                OWLClass owlClassToCompare = owlClassExpression.asOWLClass();
                if (owlClass.getIRI().toString().trim().equals(owlClassToCompare.getIRI().toString().trim())) {
                    owlDataProperties.add(owlDataProperty);

                    break;
                } else {
                }
            }
        }
        return owlDataProperties;
    }

    private void addImportJavaUtilCode(PrintWriter printWriter, List<OWLDataProperty> owlDataProperties,
            List<OWLObjectProperty> owlObjectProperties) {
        for (Iterator iterator = owlDataProperties.iterator(); iterator.hasNext();) {
            OWLDataProperty owlDataProperty = (OWLDataProperty) iterator.next();
            if (!owlDataProperty.isFunctional(owlOntology)) {
                printWriter.println("import java.util.*;");
                printWriter.println();
                return;
            }
        }
        for (Iterator iterator = owlObjectProperties.iterator(); iterator.hasNext();) {
            OWLObjectProperty owlObjectProperty = (OWLObjectProperty) iterator.next();
            if (!owlObjectProperty.isFunctional(owlOntology)) {
                printWriter.println("import java.util.*;");
                printWriter.println();
                return;
            }
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

    private List<OWLClass> getClassesList(Node<OWLClass> topNode) {
        Node<OWLClass> unsatisfableClasses = reasoner.getUnsatisfiableClasses();

        List<OWLClass> owlClassList = new ArrayList<OWLClass>();
        classesNodeList = new ArrayList<Node<OWLClass>>();
        getSubClasses(topNode);

        for (Iterator iterator = classesNodeList.iterator(); iterator.hasNext();) {

            Node<OWLClass> nodeClasses = (Node<OWLClass>) iterator.next();
            Set<OWLClass> entities = nodeClasses.getEntities();
            for (Iterator iterator2 = entities.iterator(); iterator2.hasNext();) {
                OWLClass owlClass = (OWLClass) iterator2.next();
                if (!owlClassList.contains(owlClass) && !owlClass.isBuiltIn()
                        && !unsatisfableClasses.contains(owlClass)) {
                    owlClassList.add(owlClass);
                }
            }
        }
        return owlClassList;
    }

    private void getSubClasses(Node<OWLClass> parent) {

        if (parent.isBottomNode()) {
            return;
        }

        parent.getEntities();
        for (Node<OWLClass> child : reasoner.getSubClasses(parent.getRepresentativeElement(), true)) {
            if (!classesNodeList.contains(child)) {
                classesNodeList.add(child);
                getSubClasses(child);
            }
        }

    }

    private String getInterfaceExtendsCode(OWLClass owlClass) {
        String str = " extends ";
        String base = getBaseInterface(owlClass);
        if (base == null) {
            return str + "OWLIndividual";
        } else {
            return str + base;
        }
    }

    private void createImplementation(OWLClass owlClass) throws IOException {
        String implName = getImplementationNamePossiblyAbstract(owlClass);

        File file = getImplementationFile(implName);
        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printImplementationCode(implName, owlClass, printWriter);
        fileWriter.close();

        if (options.getAbstractMode()) {
            createUserImplementation(owlClass);
        }
    }

    private String getImplementationName(OWLClass owlClass) {
        return "Default" + getInterfaceName(owlClass);
    }

    private String getImplementationNamePossiblyAbstract(OWLClass owlClass) {
        return "Default" + getInterfaceNamePossiblyAbstract(owlClass);
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
        String userInterfaceName = getInterfaceName(owlClass);
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
        printWriter.println("public interface " + getInterfaceName(owlClass) + " extends "
                + getInterfaceNamePossiblyAbstract(owlClass) + " {");
        printWriter.println("}");

    }

    private void printImplementationCode(String implName, OWLClass owlClass, PrintWriter printWriter) {

        printImplementationPackageStatement(printWriter);
        printWriter.println();
        List<OWLObjectProperty> owlObjectProperties = getClassObjectProperties(owlClass);
        List<OWLDataProperty> owlDataProperties = getClassDataProperties(owlClass);
        String pack = options.getPackage();
        if (pack != null) {
            printWriter.println("import " + pack + "." + getInterfaceNamePossiblyAbstract(owlClass) + ";");
            printWriter.println("import " + pack + ".*;");
            printWriter.println();
        }

        printWriter.println("import java.util.*;");
        printWriter.println();
        printWriter.println("import org.protege.editor.owl.codegeneration.AbstractCodeGeneratorIndividual;");
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
        printWriter.println("         implements " + getInterfaceNamePossiblyAbstract(owlClass) + " {");

        printConstructors(printWriter, implName);

        for (Iterator iterator = owlObjectProperties.iterator(); iterator.hasNext();) {
            OWLObjectProperty owlObjectProperty = (OWLObjectProperty) iterator.next();
            printImplementationObjectPropertyCode(owlObjectProperty, printWriter);
            printWriter.println();
        }

        for (Iterator iterator = owlDataProperties.iterator(); iterator.hasNext();) {
            OWLDataProperty owlDataProperty = (OWLDataProperty) iterator.next();
            printImplementationDataPropertyCode(owlDataProperty, printWriter);
        }

        printWriter.println("    public void delete(){");
        printWriter.println("        deleteIndividual();");
        printWriter.println("    }");
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
        printWriter.println("        super(owlDataFactory, iri, owlOntology);");
        printWriter.println("    }");
        printWriter.println();
    }

    private void printImplementationObjectPropertyCode(OWLObjectProperty owlObjectProperty, PrintWriter printWriter) {
        String propertyName = getObjectPropertyName(owlObjectProperty);
        String propertyNameUpperCase = getInitialLetterAsUpperCase(propertyName);
        String getPropertyFunctionName = "get" + propertyNameUpperCase + "Property()";
        String objectPropertyRange = getObjectPropertyRange(owlObjectProperty, false);
        boolean isFunctional = owlObjectProperty.isFunctional(owlOntology);
        //        
        printWriter.println();
        printWriter.println("    // Property " + owlObjectProperty.getIRI());
        printWriter.println();
        printWriter.println("    public OWLObjectProperty " + getPropertyFunctionName + " {");
        printWriter.println("        final String iriString = \"" + owlObjectProperty.getIRI() + "\";");
        printWriter.println("        final IRI iri = IRI.create(iriString);");
        printWriter.println("        return getOWLDataFactory().getOWLObjectProperty(iri);");
        printWriter.println("    }");

        printWriter.println();
        printWriter.println("    public " + objectPropertyRange + " get" + propertyNameUpperCase + "() {");
        if (!isFunctional) {
            Set<OWLClassExpression> oClassExpressions = owlObjectProperty.getRanges(owlOntology);
            String objectPropertyType = getObjectPropertyJavaName(oClassExpressions);

            printWriter.println("        Set<" + objectPropertyType + "> propertyValues = new HashSet<"
                    + objectPropertyType + ">();");
            printWriter.println("        Set<OWLIndividual> values = getObjectPropertyValues("
                    + getPropertyFunctionName + ", getOwlOntology());");
            printWriter.println("        if(values ==null || values.isEmpty()){");
            printWriter.println("            return null;");
            printWriter.println("        }");
            printWriter.println("        for (OWLIndividual owlIndividual : values) {");
            if (objectPropertyType.equals(PropertyConstants.JAVA_LANG_OBJECT)) {
                printWriter.println("  propertyValues.add(owlIndividual);");
            } else {
                printWriter.println("            propertyValues.add(new Default" + objectPropertyType
                        + "(getOWLDataFactory(), owlIndividual.asOWLNamedIndividual().getIRI(), getOwlOntology()));");
            }
            printWriter.println("        }");
            printWriter.println("        return propertyValues;");

        } else {
            printWriter.println("        Set<OWLIndividual> values = getObjectPropertyValues("
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

        printWriter.println();
        printWriter.println();
        printWriter.println("    public boolean has" + propertyNameUpperCase + "() {");
        printWriter.println("        Set<OWLIndividual> values = getObjectPropertyValues(" + getPropertyFunctionName
                + ", getOwlOntology());");
        printWriter.println("        return (values == null || values.isEmpty()) ? false : true;");
        printWriter.println("    }");

        if (!isFunctional) {
            Set<OWLClassExpression> oClassExpressions = owlObjectProperty.getRanges(owlOntology);
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
                    .println("        OWLObjectPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLObjectPropertyAssertionAxiom( "
                            + getPropertyFunctionName
                            + ", this, (OWLIndividual)"
                            + " new"
                            + propertyNameUpperCase
                            + " );");
            printWriter.println("        getOwlOntology().getOWLOntologyManager().addAxiom( getOwlOntology(), axiom);");
            printWriter.println("    }");
            printWriter.println();
            printWriter.println("    public void remove" + propertyNameUpperCase + "(" + objPropertyJavaName + " old"
                    + propertyNameUpperCase + ") {");
            printWriter.println("    removeObjectPropertyValue( (OWLNamedIndividual) old" + propertyNameUpperCase + ", "
                    + getPropertyFunctionName + ");");
            printWriter.println("    }");

            printWriter.println();
            printWriter.println("    public void set" + propertyNameUpperCase + "( "
                    + objectPropertyRange + " new" + propertyNameUpperCase + "List ) {");
            printWriter.println("        OWLObjectProperty property = "+getPropertyFunctionName+";");
            printWriter.println("        "+objectPropertyRange+" prevValues = get" + propertyNameUpperCase + "();");
            printWriter.println("        for ("+objPropertyJavaName+" value : prevValues) {");
            printWriter.println("            removeObjectPropertyValue( (OWLNamedIndividual) value, property);");
            printWriter.println("        }");
            
            printWriter.println("        for (" + objPropertyJavaName + " element : " + " new" + propertyNameUpperCase
                    + "List" + ") {");
            printWriter
                    .println("            OWLObjectPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLObjectPropertyAssertionAxiom( "
                            + getPropertyFunctionName + ", this, (OWLIndividual)" + " element);");
            printWriter
                    .println("            getOwlOntology().getOWLOntologyManager().addAxiom(getOwlOntology(), axiom);");
            printWriter.println("        }");
            printWriter.println("    }");

        } else {

            printWriter.println();
            printWriter.println("    public void set" + propertyNameUpperCase + "("
                    + objectPropertyRange + " new" + propertyNameUpperCase + ") {");
            printWriter.println("        OWLObjectProperty property = "+getPropertyFunctionName+";");
            printWriter.println("        "+objectPropertyRange+" prevValue = get" + propertyNameUpperCase + "();");
            printWriter.println("        removeObjectPropertyValue( (OWLNamedIndividual) prevValue, property);");
            printWriter
                    .println("        OWLObjectPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLObjectPropertyAssertionAxiom( "
                            + getPropertyFunctionName + ", this, " + "new" + propertyNameUpperCase + ");");
            printWriter.println("        getOwlOntology().getOWLOntologyManager().addAxiom(getOwlOntology(), axiom);");
            printWriter.println("    }");

        }
    }

    private void createUserImplementation(OWLClass owlClass) throws IOException {
        String implName = getImplementationName(owlClass);
        File file = getImplementationFile(implName);
        if (!file.exists()) {
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printUserImplementationCode(implName, owlClass, printWriter);
            fileWriter.close();
        }
    }

    private void printUserImplementationCode(String implName, OWLClass owlClass, PrintWriter printWriter) {
        printImplementationPackageStatement(printWriter);

        String pack = options.getPackage();
        if (pack != null) {
            printWriter.println("import " + pack + ".*;");
            printWriter.println();
        }
        printWriter.println("import org.semanticweb.owlapi.model.*;");
        printWriter.println();
        printWriter.println("public class " + implName + " extends " + getImplementationNamePossiblyAbstract(owlClass));
        printWriter.println("         implements " + getInterfaceName(owlClass) + " {");
        printConstructors(printWriter, implName);
        printWriter.println("}");

    }

    //TODO: Refactor this function , make modular
    private void printImplementationDataPropertyCode(OWLDataProperty owlDataProperty, PrintWriter printWriter) {

        String propertyName = getDataPropertyName(owlDataProperty);
        String propertyNameUpperCase = getInitialLetterAsUpperCase(propertyName);
        String getPropertyFunctionName = "get" + propertyNameUpperCase + "Property()";

        Set<OWLDataRange> owlDataRanges = owlDataProperty.getRanges(owlOntology);
        String dataPropertyJavaName = getDataPropertyJavaName(owlDataRanges);
        
        boolean isDataTypeBasic = isDataTypeBasic(owlDataProperty);

        boolean isFunctional = owlDataProperty.isFunctional(owlOntology);
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
        printWriter.println("    public " + getDataPropertyRange(owlDataProperty) + " get" + propertyNameUpperCase
                + "() {");

        if (isFunctional) {

            printWriter.println("        Set<OWLLiteral> propertyValues = getDataPropertyValues( "
                    + getPropertyFunctionName + ", getOwlOntology());");
            printWriter.println("        for (OWLLiteral owlLiteral : propertyValues) {");
            if (dataPropertyJavaName.equalsIgnoreCase(PropertyConstants.JAVA_LANG_OBJECT)) {
                printWriter.println("            return (" + dataPropertyJavaName + ") owlLiteral.getLiteral();");
            } else {
                printWriter.println("            return new " + dataPropertyJavaName + "( owlLiteral.getLiteral());");
            }
            printWriter.println("        }");
            printWriter.println("        return null;");
        } else {
            printWriter.println("        Set<OWLLiteral> propertyValues = getDataPropertyValues( "
                    + getPropertyFunctionName + ", getOwlOntology());");
            printWriter.println("        Set<" + dataPropertyJavaName + "> values = new HashSet<" + dataPropertyJavaName
                    + ">();");
            printWriter.println("        for (OWLLiteral owlLiteral : propertyValues) {");
            if (dataPropertyJavaName.equalsIgnoreCase(PropertyConstants.JAVA_LANG_OBJECT)) {
                printWriter.println("            values.add( (" + dataPropertyJavaName + ")owlLiteral.getLiteral());");
            } else {
                printWriter.println("            values.add( new " + dataPropertyJavaName + "( owlLiteral.getLiteral()));");
            }
            printWriter.println("        }");
            printWriter.println("        return values;");
        }
        printWriter.println("    }");
        printWriter.println();

        printWriter.println("    public boolean has" + propertyNameUpperCase + "(){");
        printWriter.println("        "+getDataPropertyRange(owlDataProperty)+" value = get" + propertyNameUpperCase+"();");
        if(isFunctional){
            printWriter.println("        return ( value == null )? false : true;");
        }else {
            printWriter.println("        return ( value == null || value.isEmpty() )? false : true;");
        }
        printWriter.println("    }");

        if (!owlDataProperty.isFunctional(owlOntology)) {
            printWriter.println();
            printWriter.println("    public " + PropertyConstants.JAVA_UTIL_ITERATOR + " list" + propertyNameUpperCase
                    + "() {");
            printWriter.println("        return get" + propertyNameUpperCase+"().iterator();");
            printWriter.println("    }");
            printWriter.println();
            printWriter.println("    public void add" + propertyNameUpperCase + "("
                    + getDataPropertyJavaName(owlDataRanges) + " new" + propertyNameUpperCase + "){");
            if(isDataTypeBasic) {
                printWriter.println("        OWLLiteral literal = getOWLDataFactory().getOWLLiteral( new"+propertyNameUpperCase+");");
            }else {
                printWriter.println("        OWLLiteral literal = getOWLDataFactory().getOWLLiteral( new"+propertyNameUpperCase+".toString(), \"\");");
            }
            printWriter.println("        if (!doesPropertyContainsLiteral("+getPropertyFunctionName+", literal)) {");
            printWriter.println("            OWLDataPropertyAssertionAxiom axiom = getOWLDataFactory().getOWLDataPropertyAssertionAxiom(");
            printWriter.println("                "+getPropertyFunctionName+", this, literal);");
            printWriter.println("            getOwlOntology().getOWLOntologyManager().addAxiom(getOwlOntology(), axiom);");
            printWriter.println("        }");
            printWriter.println("    }");
            printWriter.println();
            printWriter.println("    public void remove" + propertyNameUpperCase + "("
                    + getDataPropertyJavaName(owlDataRanges) + " old" + propertyNameUpperCase + "){");
            if(isDataTypeBasic) {
                printWriter.println("        OWLLiteral literal = getOWLDataFactory().getOWLLiteral( old" + propertyNameUpperCase + ");");
            }else {
                printWriter.println("        OWLLiteral literal = getOWLDataFactory().getOWLLiteral( old" + propertyNameUpperCase + ".toString(),\"\");");
            }
            printWriter.println("        removeDataPropertyValue(literal, "+getPropertyFunctionName+");");
            printWriter.println("    }");

        }
        printWriter.println();
        
        
        
        //Generate Setter function
        if (isFunctional) {
            printWriter.println("    public void set" + propertyNameUpperCase + "(" + getDataPropertyRange(owlDataProperty)
                    + " new" + propertyNameUpperCase + "){");
            printWriter.println("        "+getDataPropertyRange(owlDataProperty)+" prevValue = get" + propertyNameUpperCase + "()"+";");
            printWriter.println("        //Remove previous value/values");
            printWriter.println("        if (prevValue != null) {");
            if(isDataTypeBasic) {
                printWriter.println("            OWLLiteral literalToRemove = getOWLDataFactory().getOWLLiteral(prevValue);");
                printWriter.println("            removeDataPropertyValue(literalToRemove, "+getPropertyFunctionName+");");
                printWriter.println("        }");
                printWriter.println("    OWLLiteral literal = getOWLDataFactory().getOWLLiteral(("+" new" + propertyNameUpperCase+" == null) ? null : "+" new" + propertyNameUpperCase+");");
                
            }else {
                printWriter.println("            OWLLiteral literalToRemove = getOWLDataFactory().getOWLLiteral(prevValue.toString(), \"\");");
                printWriter.println("            removeDataPropertyValue(literalToRemove, "+getPropertyFunctionName+");");
                printWriter.println("        }");
                printWriter.println("    OWLLiteral literal = getOWLDataFactory().getOWLLiteral(("+" new" + propertyNameUpperCase+" == null) ? null : "+" new" + propertyNameUpperCase+".toString(), \"\");");
                
                
                
            }
            printWriter.println("    setDataProperty("+getPropertyFunctionName+", literal);");
            
        } else {
            printWriter.println("    public void set" + propertyNameUpperCase + "(" + getDataPropertyRange(owlDataProperty)
                    + " new" + propertyNameUpperCase + "List ){");
            printWriter.println("        "+getDataPropertyRange(owlDataProperty)+" prevValueList = get" + propertyNameUpperCase + "();");
            printWriter.println("        if (prevValueList != null) {");
            printWriter.println("            for ("+dataPropertyJavaName+" prevValue : prevValueList) {");
            if(isDataTypeBasic) {
                printWriter.println("                OWLLiteral literalToRemove = getOWLDataFactory().getOWLLiteral(prevValue);");
            }else {
                printWriter.println("                OWLLiteral literalToRemove = getOWLDataFactory().getOWLLiteral(prevValue.toString(), \"\");");
            }
            
            printWriter.println("                removeDataPropertyValue(literalToRemove, "+getPropertyFunctionName+");");
            printWriter.println("            }");
            printWriter.println("        }");
            printWriter.println("        for ("+dataPropertyJavaName+" value : new" + propertyNameUpperCase + "List ) {");
            if(isDataTypeBasic) {
                printWriter.println("            OWLLiteral literal = getOWLDataFactory().getOWLLiteral(value);");
            }else {
                printWriter.println("            OWLLiteral literal = getOWLDataFactory().getOWLLiteral(value.toString(), \"\");");
            }
            printWriter.println("            setDataProperty("+getPropertyFunctionName+", literal);");
            printWriter.println("        }");
            
            
        }
        printWriter.println("    }");

    }

    private boolean isDataTypeBasic(OWLDataProperty owlDataProperty) {
        Set<OWLDataRange> ranges = owlDataProperty.asOWLDataProperty().getRanges(owlOntology);
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

    private boolean hasMultipleSuperclasses(OWLClass owlClass) {
        boolean superclassFound = false;
        for (OWLClassExpression owlClassExpression : owlClass.getSuperClasses(owlOntology)) {
            if (owlClassExpression.isAnonymous()) {
                continue;
            }
            OWLClass superClass = owlClassExpression.asOWLClass();
            if (superClass != null && !superClass.isTopEntity() && !superClass.isBuiltIn()) {
                if (superclassFound == false) {
                    superclassFound = true;
                } else {
                    return true;
                }
            }
        }

        return false;

    }

    private String getBaseImplementation(OWLClass owlClass) {
        String baseImplementationString = "";
        for (OWLClassExpression owlClassExpression : owlClass.getSuperClasses(owlOntology)) {
            if (owlClassExpression.isAnonymous()) {
                continue;
            }
            OWLClass superClass = owlClassExpression.asOWLClass();
            if (superClass != null && !superClass.isTopEntity() && !superClass.isBuiltIn()) {
                if (baseImplementationString.equals("")) {
                    baseImplementationString = getImplementationName(superClass);
                } else {
                    return null;
                }
                //                baseImplementationString += (baseImplementationString.equals("") ? "" : ", ") + getInterfaceName(superClass);
            }

        }
        if (baseImplementationString.equals("")) {
            return null;
        } else {
            return baseImplementationString;
        }
    }

    private String getBaseInterface(OWLClass owlClass) {
        String baseInterfaceString = "";
        for (OWLClassExpression owlClassExpression : owlClass.getSuperClasses(owlOntology)) {
            if (owlClassExpression.isAnonymous()) {
                continue;
            }
            OWLClass superClass = owlClassExpression.asOWLClass();
            if (superClass != null && !superClass.isTopEntity() && !superClass.isBuiltIn()) {
                baseInterfaceString += (baseInterfaceString.equals("") ? "" : ", ") + getInterfaceName(superClass);
            }

        }
        if (baseInterfaceString.equals("")) {
            return null;
        } else {
            return baseInterfaceString;
        }
    }

    public void setIRI(IRI iri) {
        this.iri = iri;
    }

    /**
     * @param owlDataFactory
     *            the owlDataFactory to set
     */
    public void setOwlDataFactory(OWLDataFactory owlDataFactory) {
        this.owlDataFactory = owlDataFactory;
    }

    public String getInitialLetterAsUpperCase(String name) {
        if (name.length() > 1) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name.toUpperCase();
        }
    }
}
