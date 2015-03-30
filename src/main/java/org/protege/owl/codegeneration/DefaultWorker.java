package org.protege.owl.codegeneration;

import static org.protege.owl.codegeneration.SubstitutionVariable.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeSet;

import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.protege.owl.codegeneration.names.CodeGenerationNames;
import org.protege.owl.codegeneration.names.NamingUtilities;
import org.protege.owl.codegeneration.property.JavaPropertyDeclarationCache;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

public class DefaultWorker implements Worker {
	private EnumMap<CodeGenerationPhase, String> templateMap = new EnumMap<CodeGenerationPhase, String>(CodeGenerationPhase.class);
	private OWLOntology owlOntology;
	private CodeGenerationOptions options;
	private CodeGenerationNames names;
    private CodeGenerationInference inference;
    private JavaPropertyDeclarationCache propertyDeclarations;
    
    public static void generateCode(OWLOntology ontology, CodeGenerationOptions options, CodeGenerationNames names) throws IOException {
    	generateCode(ontology, options, names, new SimpleInference(ontology));
    }
    
    public static void generateCode(OWLOntology ontology, CodeGenerationOptions options, CodeGenerationNames names, CodeGenerationInference inference) throws IOException {
    	Worker worker = new DefaultWorker(ontology, options, names, inference);
    	JavaCodeGenerator generator = new JavaCodeGenerator(worker);
    	generator.createAll();
    }    
	
    
    public DefaultWorker(OWLOntology ontology, 
    		             CodeGenerationOptions options,
    		             CodeGenerationNames names,
			             CodeGenerationInference inference) {
		this.owlOntology = ontology;
		this.options = options;
		this.names = names;
		this.inference = inference;
		propertyDeclarations = new JavaPropertyDeclarationCache(inference, names);
	}

    public OWLOntology getOwlOntology() {
		return owlOntology;
	}
    
    @Override
    public CodeGenerationInference getInference() {
    	return inference;
    }
    
    public Collection<OWLClass> getOwlClasses() {
    	return new TreeSet<OWLClass>(inference.getOwlClasses());
    }
    
    public Collection<OWLObjectProperty> getOwlObjectProperties() {
    	return Utilities.filterIgnored(owlOntology.getObjectPropertiesInSignature(true), owlOntology);
    }
    
    public Collection<OWLDataProperty> getOwlDataProperties() {
    	return Utilities.filterIgnored(owlOntology.getDataPropertiesInSignature(true), owlOntology);
    }
    
    public Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass owlClass) {
    	return Utilities.filterIgnored(propertyDeclarations.getObjectPropertiesForClass(owlClass), owlOntology);
    }
    
    public Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass owlClass) {
    	return Utilities.filterIgnored(propertyDeclarations.getDataPropertiesForClass(owlClass), owlOntology);
    }

    public void initialize() {
        File folder = options.getOutputFolder();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        File packageFile;
        String pack = options.getPackage();
        if (pack != null) {
            String packPath = pack.replace('.', '/');
            packageFile = folder == null ? new File(packPath) : new File(folder, packPath);
            packageFile.mkdirs();
        } else {
        	packageFile= new File("");
        }
        File implFile = new File(packageFile, "impl");
        implFile.mkdirs();
        String subPackage = options.getFactorySubPackage();
        String subPackagePath = subPackage.replace('.', '/');
        File factoryDirectory = new File(packageFile, subPackagePath);
        factoryDirectory.mkdirs();
    }
    
    public File getInterfaceFile(OWLClass owlClass) {
        String interfaceName = names.getInterfaceName(owlClass);
        return getInterfaceFile(interfaceName);
    }
    
    public File getImplementationFile(OWLClass owlClass) {
    	String implName = names.getImplementationName(owlClass);
    	return getImplementationFile(implName);
    }
    
    public File getVocabularyFile() {
    	return new File(options.getOutputFolder(),
    			options.getVocabularyFqn().replace('.', '/') +".java");
    }
    
    public File getFactoryFile() {
    	return new File(options.getOutputFolder(),
    			options.getFactoryFqn().replace('.', '/')+".java");
    }


	public String getTemplate(CodeGenerationPhase phase, OWLClass owlClass, Object owlProperty) {
    	String resource = "/" + phase.getTemplateName();
		String template = templateMap.get(phase);
		if (template == null) {
			try {
				URL u = CodeGenerationOptions.class.getResource(resource);
				Reader reader = new InputStreamReader(u.openStream());
				StringBuffer buffer = new StringBuffer();
				int charsRead;
				char[] characters = new char[1024];
				while (true) {
					charsRead = reader.read(characters);
					if (charsRead < 0) {
						break;
					}
					buffer.append(characters, 0, charsRead);
				}
				template = buffer.toString();
				templateMap.put(phase, template);
				reader.close();
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return template;
    }
	
	public void configureSubstitutions(CodeGenerationPhase phase,
									   Map<SubstitutionVariable, String> substitutions, 
									   OWLClass owlClass,
									   OWLEntity owlProperty) {
		switch (phase) {
		case CREATE_VOCABULARY_HEADER:
		case CREATE_FACTORY_HEADER:
			configureCommonSubstitutions(substitutions, owlClass, owlProperty);
			break;
		case CREATE_CLASS_VOCABULARY:
		case CREATE_FACTORY_CLASS:
			configureClassSubstitutions(substitutions, owlClass);
			break;
		case CREATE_OBJECT_PROPERTY_VOCABULARY:
		case CREATE_DATA_PROPERTY_VOCABULARY:
			configurePropertySubstitutions(substitutions, owlProperty);
			break;
		case CREATE_INTERFACE_HEADER:
		case CREATE_IMPLEMENTATION_HEADER:
			configureCommonSubstitutions(substitutions, owlClass, owlProperty);
			configureClassSubstitutions(substitutions, owlClass);
			break;
		case CREATE_DATA_PROPERTY_INTERFACE:
		case CREATE_FUNCTIONAL_DATA_PROPERTY_INTERFACE:
		case CREATE_DATA_PROPERTY_IMPLEMENTATION:
		case CREATE_FUNCTIONAL_DATA_PROPERTY_IMPLEMENTATION:
		case CREATE_OBJECT_PROPERTY_INTERFACE:
		case CREATE_FUNCTIONAL_OBJECT_PROPERTY_INTERFACE:
		case CREATE_OBJECT_PROPERTY_IMPLEMENTATION:
		case CREATE_FUNCTIONAL_OBJECT_PROPERTY_IMPLEMENTATION:
			configureClassSubstitutions(substitutions, owlClass);
			configurePropertySubstitutions(substitutions, owlProperty);
	        propertyDeclarations.get(owlClass, owlProperty).configureSubstitutions(substitutions);
			break;
		case CREATE_FACTORY_TAIL:
		case CREATE_IMPLEMENTATION_TAIL:
		case CREATE_INTERFACE_TAIL:
		case CREATE_VOCABULARY_TAIL:
			break;
		default:
			break;
		}
		
	}

	private void configureCommonSubstitutions(Map<SubstitutionVariable, String> substitutions, 
											  OWLClass owlClass,
											  OWLEntity owlProperty) {
        substitutions.put(PACKAGE, options.getPackage());
        substitutions.put(DATE, new Date().toString());
        substitutions.put(USER, System.getProperty("user.name"));
        substitutions.put(FACTORY_CLASS_NAME, options.getFactoryClassName());
        substitutions.put(FACTORY_PACKAGE, options.getFactoryPackage());
        substitutions.put(FACTORY_EXTRA_IMPORT, options.getExtraFactoryImport());
        substitutions.put(IMPLEMENTATION_EXTRA_IMPORT, options.getExtraImplementationImport());
    }

	private void configureClassSubstitutions(Map<SubstitutionVariable, String> substitutions, 
											  OWLClass owlClass) {
		String upperCaseClassName = names.getClassName(owlClass).toUpperCase();
        substitutions.put(INTERFACE_NAME, names.getInterfaceName(owlClass));
        substitutions.put(IMPLEMENTATION_NAME, names.getImplementationName(owlClass));
        substitutions.put(JAVADOC, getJavadoc(owlClass));
        substitutions.put(UPPERCASE_CLASS, upperCaseClassName);
        substitutions.put(SubstitutionVariable.VOCABULARY_CLASS, "CLASS_" + upperCaseClassName);
        substitutions.put(CLASS_IRI, owlClass.getIRI().toString());
        substitutions.put(INTERFACE_LIST, getSuperInterfaceList(owlClass));
    }

	private void configurePropertySubstitutions(Map<SubstitutionVariable, String> substitutions,
                                                OWLEntity owlProperty) {
        String propertyName;
        if (owlProperty instanceof OWLObjectProperty) {
            OWLObjectProperty owlObjectProperty = (OWLObjectProperty) owlProperty;
            propertyName = names.getObjectPropertyName(owlObjectProperty);
        }
        else {
            OWLDataProperty owlDataProperty = (OWLDataProperty) owlProperty;
            propertyName = names.getDataPropertyName(owlDataProperty);
        }
        String propertyCapitalized = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String propertyUpperCase = propertyName.toUpperCase();
        if (owlProperty instanceof OWLObjectProperty) {
            substitutions.put(SubstitutionVariable.VOCABULARY_PROPERTY, "OBJECT_PROPERTY_" + propertyUpperCase);
        }
        else {
            substitutions.put(SubstitutionVariable.VOCABULARY_PROPERTY, "DATA_PROPERTY_" + propertyUpperCase);
        }
        substitutions.put(JAVADOC, getJavadoc(owlProperty));
        substitutions.put(PROPERTY, propertyName);
        substitutions.put(CAPITALIZED_PROPERTY, propertyCapitalized);
        substitutions.put(UPPERCASE_PROPERTY, propertyUpperCase);
        substitutions.put(PROPERTY_IRI, owlProperty.getIRI().toString());
    }
	

	
	/* ******************************************************************************
	 * 
	 */
	private File getInterfaceFile(String name) {
	    String pack = options.getPackage();
	    if (pack != null) {
	        pack = pack.replace('.', '/') + "/";
	    } else {
	        pack = "";
	    }
	    return new File(options.getOutputFolder(), pack + name + ".java");
	}
	


	private String getSuperInterfaceList(OWLClass owlClass) {
	    String base = getBaseInterface(owlClass);
	    if (base == null) {
	    	return Constants.UKNOWN_CODE_GENERATED_INTERFACE;
	    }
	    else {
	    	return base;
	    }
	}
	
    /** Returns base interface of the provided OWLClass
     * @param owlClass The OWLClass whose base interface is to be returned
     * @return
     */
    private String getBaseInterface(OWLClass owlClass) {
        String baseInterfaceString = "";
        for (OWLClass superClass : inference.getSuperClasses(owlClass)) {
            if (inference.getOwlClasses().contains(superClass)) {
                baseInterfaceString += (baseInterfaceString.equals("") ? "" : ", ") + names.getInterfaceName(superClass);
            }
        }
        if (baseInterfaceString.equals("")) {
            return null;
        } else {
            return baseInterfaceString;
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
	
	private String getJavadoc(OWLEntity e) {
	    StringBuffer sb = new StringBuffer();
	    Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(e, owlOntology, Constants.JAVADOC);
	    if (annotations.size() == 1) {
	        OWLAnnotation javadocAnnotation = annotations.iterator().next();
	        if (javadocAnnotation.getValue() instanceof OWLLiteral) {
	            sb.append('\n');
	            sb.append(((OWLLiteral) javadocAnnotation.getValue()).getLiteral());
	        }
	    }
	    return sb.toString();
	}


}
