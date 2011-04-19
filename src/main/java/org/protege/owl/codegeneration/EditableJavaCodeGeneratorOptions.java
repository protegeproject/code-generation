package org.protege.owl.codegeneration;

import java.io.File;


/**Interface for Java Code generator options
 * @author z.khan
 * 
 */
public interface EditableJavaCodeGeneratorOptions extends JavaCodeGeneratorOptions {

    void setAbstractMode(boolean value);

    void setFactoryClassName(String value);

    void setOutputFolder(File file);

    void setPackage(String value);

    void setSetMode(boolean value);

    void setPrefixMode(boolean value);
}
