package org.protege.editor.owl.codegeneration;

import java.io.File;

/**
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
