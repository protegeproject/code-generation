package org.protege.editor.owl.codegeneration;

import java.io.File;

/**
 * @author Holger Knublauch  <holger@knublauch.com>
 */
public interface JavaCodeGeneratorOptions {

    boolean getAbstractMode();


    String getFactoryClassName();


    File getOutputFolder();


    String getPackage();


    boolean getSetMode();
    
    
    boolean getPrefixMode();
}
