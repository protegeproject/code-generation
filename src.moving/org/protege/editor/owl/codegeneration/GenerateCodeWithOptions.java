/**
 * 
 */
package org.protege.editor.owl.codegeneration;

/** 
 * @author z.khan
 *
 */
public interface GenerateCodeWithOptions {

    /**
     * Called when user prompts to generate code
     */
    void okClicked();
    
    /**
     * Called when user cancels code generation
     */
    void cancelClicked();
}
