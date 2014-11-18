package org.protege.owl.codegeneration;

import java.io.File;

/**This class stores the data required for owl code generator.
 * @author z.khan
 *
 */
public class CodeGenerationOptions {

    private String javaCodePackage = PACKAGE_DEFAULT;

    private String factoryClassName = FACTORY_CLASS_NAME_DEFAULT;
    
    private String factorySubPackage = FACTORY_SUBPACKAGE_DEFAULT;

    private File outputFolder;

    private boolean useReasoner;

    public final static String FACTORY_CLASS_NAME_DEFAULT = "MyFactory";
    
    public final static String FACTORY_SUBPACKAGE_DEFAULT = "";

    public final static String FILE_NAME_DEFAULT = "";

    public final static String PACKAGE_DEFAULT = null;
    
    /* ****************************************************************
     * POJO Configuration methods
     */
    
    public void setFactoryClassName(String value) {
        if (value != null && value.trim().length() > 0) {
            factoryClassName = value;
        } else {
            factoryClassName = FACTORY_CLASS_NAME_DEFAULT;
        }
    }

    public void setOutputFolder(File file) {
        outputFolder = file;
    }

    public void setPackage(String value) {
        if (value == null || value.trim().length() == 0) {
            value = PACKAGE_DEFAULT;
        }
        javaCodePackage = value;
    }
    
    public void setFactorySubPackage(String factorySubPackage) {
		this.factorySubPackage = factorySubPackage;
	}
    
    public void setUseReasoner(boolean useReasoner) {
		this.useReasoner = useReasoner;
	}

    public String getFactoryClassName() {
        return factoryClassName;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public String getPackage() {
        return javaCodePackage;
    }
    
    public String getFactorySubPackage() {
		return factorySubPackage;
	}
    
    public boolean useReasoner() {
		return useReasoner;
	}
    
    public String getVocabularyFqn() {
    	return getFactoryLikeFqn(Constants.VOCABULARY_CLASS_NAME);
    }
    
    public String getFactoryFqn() {
    	return getFactoryLikeFqn(getFactoryClassName());
    }
    
    public String getFactoryLikeFqn(String className) {
    	String subPackage = getFactorySubPackage();
    	if (subPackage.isEmpty()) {
    		return getPackage() + "." + className;
    	}
    	else {
    		return getPackage() + "." + subPackage + "." + className;
    	}
    }
    
    public String getFactoryPackage() {
    	String subPackage = getFactorySubPackage();
    	if (subPackage.isEmpty()) {
    		return getPackage();
    	}
    	else {
    		return getPackage() + "." + subPackage;
    	}
    }
    
    public String getExtraFactoryImport() {
    	String subPackage = getFactorySubPackage();
    	if (subPackage.isEmpty()) {
    		return "";
    	}
    	else {
    		return "import " + getPackage() + ".*;\n";
    	}
    }
    
    public String getExtraImplementationImport() {
    	String subPackage = getFactorySubPackage();
    	if (subPackage.isEmpty()) {
    		return "";
    	}
    	else {
    		return "import " + getPackage() + "." + subPackage + ".*;\n";
    	}
    }

}
