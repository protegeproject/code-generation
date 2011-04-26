/**
 * 
 */
package org.protege.editor.owl.codegeneration;

import org.protege.editor.owl.model.OWLModelManager;

/** 
 * @author z.khan
 *
 */
public interface GenerateCodeCallback {

    /**
     * Called when user prompts to generate code
     */
    void okClicked();
    
    /**
     * Called when user cancels code generation
     */
    void cancelClicked();
    
    OWLModelManager getOWLModelManager();
}
