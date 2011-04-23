package org.protege.owl.codegeneration;

import static org.protege.owl.codegeneration.SubstitutionVariable.*;
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
import static org.protege.owl.codegeneration.SubstitutionVariable.UPPERCASE_CLASS;
import static org.protege.owl.codegeneration.SubstitutionVariable.UPPERCASE_PROPERTY;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

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

public class DefaultWorker implements Worker {
	private EnumMap<CodeGenerationPhase, String> templateMap = new EnumMap<CodeGenerationPhase, String>(CodeGenerationPhase.class);
	private OWLOntology owlOntology;
	private CodeGenerationOptions options;
    private CodeGenerationInference inference;
    private CodeGenerationNames names;
    
    public static void generateCode(OWLOntology ontology, CodeGenerationOptions options) throws IOException {
    	generateCode(ontology, options, new SimpleInference(ontology));
    }
    
    public static void generateCode(OWLOntology ontology, CodeGenerationOptions options, CodeGenerationInference inference) throws IOException {
    	Worker worker = new DefaultWorker(ontology, options, inference);
    	JavaCodeGenerator generator = new JavaCodeGenerator(worker);
    	generator.createAll();
    }    
	
    
    public DefaultWorker(OWLOntology ontology, 
    		             CodeGenerationOptions options,
			             CodeGenerationInference inference) {
		this.owlOntology = ontology;
		this.options = options;
		this.inference = inference;
		this.names = new CodeGenerationNamesFactory(ontology, options).getCGNames();
	}

    public OWLOntology getOwlOntology() {
		return owlOntology;
	}
    
    public Collection<OWLClass> getOwlClasses() {
    	return inference.getOwlClasses();
    }
    
    public Collection<OWLObjectProperty> getOwlObjectProperties() {
    	return owlOntology.getObjectPropertiesInSignature(true);
    }
    
    public Collection<OWLDataProperty> getOwlDataProperties() {
    	return owlOntology.getDataPropertiesInSignature(true);
    }

    public void initialize() {
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
    
    public Collection<OWLObjectProperty> getObjectPropertiesForClass(OWLClass owlClass) {
    	return inference.getObjectPropertiesForClass(owlClass);
    }
    
    public Collection<OWLDataProperty> getDataPropertiesForClass(OWLClass owlClass) {
    	return inference.getDataPropertiesForClass(owlClass);
    }
    
    public File getInterfaceFile(OWLClass owlClass) {
        String interfaceName = names.getInterfaceName(owlClass);
        return getInterfaceFile(interfaceName);
    }
    
    public File getImplementationFile(OWLClass owlClass) {
    	String implName = names.getImplementationName(owlClass);
    	return getImplementationFile(implName);
    }
    
    @Override
    public File getVocabularyFile() {
    	return getInterfaceFile(Constants.VOCABULARY_CLASS_NAME);
    }
    
    @Override
    public File getFactoryFile() {
    	return getInterfaceFile(options.getFactoryClassName());
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
	
	@Override
	public void configureSubstitutions(CodeGenerationPhase phase,
									   Map<SubstitutionVariable, String> substitutions, 
									   OWLClass owlClass,
									   Object owlProperty) {
		switch (phase) {
		case CREATE_INTERFACE_HEADER:
			configureInterfaceHeaderSubstitutions(substitutions, owlClass);
			break;
		case CREATE_OBJECT_PROPERTY_INTERFACE:
			configureObjectPropertyInterfaceSubstitutions(substitutions, owlClass, (OWLObjectProperty) owlProperty);
            break;
        case CREATE_DATA_PROPERTY_INTERFACE:
            configureDataPropertyInterfaceSubstitutions(substitutions, owlClass, (OWLDataProperty) owlProperty);
            break;
		case CREATE_INTERFACE_TAIL:
			break;
        case CREATE_IMPLEMENTATION_HEADER:
            configureImplementationHeaderSubstitutions(substitutions, owlClass);
            break;
        case CREATE_OBJECT_PROPERTY_IMPLEMENTATION:
            configureObjectPropertyImplementationSubstitutions(substitutions, owlClass, (OWLObjectProperty) owlProperty);
            break;
        case CREATE_DATA_PROPERTY_IMPLEMENTATION:
            configureDataPropertyImplementationSubstitutions(substitutions, owlClass, (OWLDataProperty) owlProperty);
            break;
        case CREATE_IMPLEMENTATION_TAIL:
            break;
        case CREATE_VOCABULARY_HEADER:
            configureVocabularyHeaderSubstitutions(substitutions);
            break;
        case CREATE_CLASS_VOCABULARY:
        	configureClassVocabularySubstitutions(substitutions, owlClass);
        	break;
        case CREATE_OBJECT_PROPERTY_VOCABULARY:
            configureObjectPropertyVocabularySubstitutions(substitutions, (OWLObjectProperty) owlProperty);
            break;
        case CREATE_DATA_PROPERTY_VOCABULARY:
            configureDataPropertyVocabularySubstitutions(substitutions, (OWLDataProperty) owlProperty);
            break;
        case CREATE_FACTORY_HEADER:
            configureFactoryHeaderSubstitutions(substitutions);
            break;
        case CREATE_FACTORY_CLASS:
            configureFactoryClassSubstitutions(substitutions, owlClass);
            break;
        case CREATE_FACTORY_TAIL:
            break;
        default:
            break; // you will create it if you need it...	
		}
	}
	
	/* ******************************************************************************
	 * 
	 */
	
	private void configureInterfaceHeaderSubstitutions(Map<SubstitutionVariable, String> substitutions, OWLClass owlClass) {
		String interfaceName = names.getInterfaceName(owlClass);
        substitutions.put(PACKAGE, options.getPackage());
        substitutions.put(JAVA_CLASS_NAME, interfaceName);
        substitutions.put(IMPLEMENTS_EXTENDS, getInterfaceExtendsCode(owlClass));
        substitutions.put(DATE, new Date().toString());
	}
	
	private void configureObjectPropertyInterfaceSubstitutions(Map<SubstitutionVariable, String> substitutions, OWLClass owlClass, OWLObjectProperty owlObjectProperty) {
        String propertyName = names.getObjectPropertyName(owlObjectProperty);
        String propertyCapitalized = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String propertyUpperCase = propertyName.toUpperCase();
    	substitutions.put(IRI, owlObjectProperty.getIRI().toString());
    	substitutions.put(PROPERTY, propertyName);
    	substitutions.put(CAPITALIZED_PROPERTY, propertyCapitalized);
    	substitutions.put(UPPERCASE_PROPERTY, propertyUpperCase);
    	substitutions.put(PROPERTY_RANGE, getObjectPropertyRange(owlClass, owlObjectProperty, true));
	}

	private void configureDataPropertyInterfaceSubstitutions(Map<SubstitutionVariable, String> substitutions, OWLClass owlClass, OWLDataProperty owlDataProperty) {
                String propertyName = names.getDataPropertyName(owlDataProperty);
        String propertyCapitalized = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String propertyUpperCase = propertyName.toUpperCase();
    	substitutions.put(IRI, owlDataProperty.getIRI().toString());
    	substitutions.put(PROPERTY, propertyName);
    	substitutions.put(CAPITALIZED_PROPERTY, propertyCapitalized);
    	substitutions.put(UPPERCASE_PROPERTY, propertyUpperCase);
    	substitutions.put(PROPERTY_RANGE, getDataPropertyRange(owlClass, owlDataProperty));
    }

    private void configureImplementationHeaderSubstitutions(Map<SubstitutionVariable, String> substitutions, OWLClass owlClass) {
        String implName = names.getImplementationName(owlClass);
    	substitutions.put(PACKAGE, options.getPackage());
    	substitutions.put(IMPLEMENTS_EXTENDS, getImplementationExtendsCode(owlClass) + " implements " + names.getInterfaceName(owlClass));
    	substitutions.put(DATE, new Date().toString());
    	substitutions.put(JAVA_CLASS_NAME, implName);
    }

    private void configureObjectPropertyImplementationSubstitutions(Map<SubstitutionVariable, String> substitutions, OWLClass owlClass, OWLObjectProperty owlObjectProperty) {
        String propertyName = names.getObjectPropertyName(owlObjectProperty);
        String propertyCapitilized = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String propertyUpperCase = propertyName.toUpperCase();
    	substitutions.put(IRI, owlObjectProperty.getIRI().toString());
    	substitutions.put(PROPERTY, propertyName);
    	substitutions.put(CAPITALIZED_PROPERTY, propertyCapitilized);
    	substitutions.put(UPPERCASE_PROPERTY, propertyUpperCase);
    	substitutions.put(PROPERTY_RANGE, getObjectPropertyRange(owlClass, owlObjectProperty, true));
    	substitutions.put(PROPERTY_RANGE_IMPLEMENTATION, getObjectPropertyRange(owlClass, owlObjectProperty, false));
    }

    private void configureDataPropertyImplementationSubstitutions(Map<SubstitutionVariable, String> substitutions, OWLClass owlClass, OWLDataProperty owlDataProperty) {
        String propertyName = names.getDataPropertyName(owlDataProperty);
        String propertyCapitalized = NamingUtilities.convertInitialLetterToUpperCase(propertyName);
        String propertyUpperCase = propertyName.toUpperCase();
    	substitutions.put(IRI, owlDataProperty.getIRI().toString());
    	substitutions.put(PROPERTY, propertyName);
    	substitutions.put(CAPITALIZED_PROPERTY, propertyCapitalized);
    	substitutions.put(UPPERCASE_PROPERTY, propertyUpperCase);
    	substitutions.put(PROPERTY_RANGE, getDataPropertyRange(owlClass, owlDataProperty));
    }

    private void configureVocabularyHeaderSubstitutions(Map<SubstitutionVariable, String> substitutions) {
        substitutions.put(PACKAGE, options.getPackage());
        substitutions.put(DATE, new Date().toString());
    }
    
    private void configureClassVocabularySubstitutions(Map<SubstitutionVariable, String> substitutions, OWLClass owlClass) {
    	substitutions.put(UPPERCASE_CLASS, names.getClassName(owlClass).toUpperCase());
    	substitutions.put(IRI, owlClass.getIRI().toString());
    }

    private void configureObjectPropertyVocabularySubstitutions(Map<SubstitutionVariable, String> substitutions, OWLObjectProperty owlObjectProperty) {
        substitutions.put(UPPERCASE_PROPERTY, names.getObjectPropertyName(owlObjectProperty).toUpperCase());
        substitutions.put(IRI, owlObjectProperty.getIRI().toString());
    }

    private void configureDataPropertyVocabularySubstitutions(Map<SubstitutionVariable, String> substitutions, OWLDataProperty owlDataProperty) {
        substitutions.put(UPPERCASE_PROPERTY, names.getDataPropertyName(owlDataProperty).toUpperCase());
        substitutions.put(IRI, owlDataProperty.getIRI().toString());
    }

    private void configureFactoryHeaderSubstitutions(Map<SubstitutionVariable, String> substitutions) {
        substitutions.put(PACKAGE, options.getPackage());
    	substitutions.put(DATE, new Date().toString());
    	substitutions.put(JAVA_CLASS_NAME, options.getFactoryClassName());
    }

    private void configureFactoryClassSubstitutions(Map<SubstitutionVariable, String> substitutions, OWLClass owlClass) {
    	substitutions.put(IRI, owlClass.getIRI().toString());
        substitutions.put(INTERFACE_NAME, names.getInterfaceName(owlClass));
        substitutions.put(IMPLEMENTATION_NAME, names.getImplementationName(owlClass));
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
	


	private String getInterfaceExtendsCode(OWLClass owlClass) {
	    String str = " extends ";
	    String base = getBaseInterface(owlClass);
	    if (base == null) {
	    	return str + Constants.UKNOWN_CODE_GENERATED_INTERFACE;
	    }
	    else {
	    	return str + base;
	    }
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
    
	private String getObjectPropertyRange(OWLClass owlClass, OWLObjectProperty owlObjectProperty, boolean isInterface) {
		OWLDataFactory factory  = owlOntology.getOWLOntologyManager().getOWLDataFactory();
		Collection<OWLClass> classes = inference.getRange(owlClass, owlObjectProperty);
		if (classes.isEmpty() || classes.size() > 1 || classes.contains(factory.getOWLThing())) {
			return isInterface ? Constants.UKNOWN_CODE_GENERATED_INTERFACE : Constants.ABSTRACT_CODE_GENERATOR_INDIVIDUAL_CLASS;
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
	        dataPropertyRange = Constants.UNKNOWN_JAVA_DATA_TYPE;
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
		return Constants.UNKNOWN_JAVA_DATA_TYPE;
	}

    private String getImplementationExtendsCode(OWLClass owlClass) {
	    String str = " extends ";
	    String base = getBaseImplementation(owlClass);
	    if (base == null) {
	        return str + Constants.ABSTRACT_CODE_GENERATOR_INDIVIDUAL_CLASS;
	    } else {
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


}
