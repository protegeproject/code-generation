package org.protege.owl.codegeneration;

import java.io.File;

/**This class stores the data required for owl code generator.
 * @author z.khan
 *
 */
public class EditableJavaCodeGeneratorOptionsImpl implements EditableJavaCodeGeneratorOptions {

    private boolean abstractMode;

    private boolean prefixMode;

    private boolean setMode;

    private String javaCodePackage = PACKAGE_DEFAULT;

    private String factoryClassName = FACTORY_CLASS_NAME_DEFAULT;

    private File outputFolder;

    public final static String ABSTRACT_MODE = "JavaCodeAbstract";

    public final static String FACTORY_CLASS_NAME = "JavaCodeFactoryClassName";

    public final static String FILE_NAME = "JavaCodeFileName";

    public final static String PACKAGE = "JavaCodePackage";

    public final static String SET_MODE = "JavaCodeSet";

    public final static String PREFIX_MODE = "JavaCodeUsePrefix";

    public final static String FACTORY_CLASS_NAME_DEFAULT = "MyFactory";

    public final static String FILE_NAME_DEFAULT = "";

    public final static String PACKAGE_DEFAULT = null;

    public void setAbstractMode(boolean value) {
        abstractMode = value;

    }

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

    public void setPrefixMode(boolean value) {
        prefixMode = value;
    }

    public void setSetMode(boolean value) {
        setMode = value;
    }

    public boolean getAbstractMode() {
        return abstractMode;
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

    public boolean getPrefixMode() {
        return prefixMode;
    }

    public boolean getSetMode() {
        return setMode;
    }

}
