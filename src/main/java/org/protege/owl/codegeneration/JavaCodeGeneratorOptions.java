package org.protege.owl.codegeneration;

import java.io.File;

/** Interface for code generation options.
 * @author z.khan
 *
 */
public interface JavaCodeGeneratorOptions {

    boolean getAbstractMode();


    String getFactoryClassName();


    File getOutputFolder();


    String getPackage();


    boolean getSetMode();
    
    
    boolean getPrefixMode();
}
