package org.protege.editor.owl.codegeneration;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.DefaultWorker;
import org.protege.owl.codegeneration.Utilities;
import org.protege.owl.codegeneration.inference.CodeGenerationInference;
import org.protege.owl.codegeneration.inference.ReasonerBasedInference;
import org.protege.owl.codegeneration.inference.SimpleInference;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author z.khan
 * 
 */
public class GenerateProtegeOwlJavaCodeAction extends ProtegeOWLAction implements GenerateCodeCallback {
	public static final Logger LOGGER = Logger.getLogger(GenerateProtegeOwlJavaCodeAction.class);
	public static final String CODE_GENERATION_PREFERENCES = "CODE_GENERATION_PREFERENCES";
	public static final String PACKAGE_PREFS_KEY = "package";
	public static final String FOLDER_PREFS_KEY = "folder";
	public static final String FACTORY_PREFS_KEY = "factory";
	
	
	private Preferences codeGenerationPreferences = PreferencesManager.getInstance().getPreferencesForSet(CODE_GENERATION_PREFERENCES, GenerateProtegeOwlJavaCodeAction.class);

    private static final long serialVersionUID = 1L;

    private CodeGenerationOptions options;

    private JFrame codeGenOptionFrame;

    /* (non-Javadoc)
     * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
     */
    public void initialise() throws Exception {
        options = new CodeGenerationOptions();
        options.setFactoryClassName(codeGenerationPreferences.getString(FACTORY_PREFS_KEY, "MyFactory"));
        options.setPackage(codeGenerationPreferences.getString(PACKAGE_PREFS_KEY, ""));
        String folder = codeGenerationPreferences.getString(FOLDER_PREFS_KEY, null);
        if (folder == null) {
        	folder = new File("").getAbsolutePath().toString();
        }
    	options.setOutputFolder(new File(folder));
    }

    /* (non-Javadoc)
     * @see org.protege.editor.core.Disposable#dispose()
     */
    public void dispose() throws Exception {

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        showGeneratorPanel();
    }

    /**
     * Displays the panel with options required for code generation
     */
    private void showGeneratorPanel() {
        JavaCodeGeneratorPanel javaCodeGeneratorPanel = new JavaCodeGeneratorPanel(options, this);
        codeGenOptionFrame = new JFrame("Generate Protege-OWL Java Code");
        codeGenOptionFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        codeGenOptionFrame.add(javaCodeGeneratorPanel);
        codeGenOptionFrame.setSize(300, 370);
        codeGenOptionFrame.setVisible(true);
        center(codeGenOptionFrame);
    }

    /**Sets the generator panel to center
     * @param component
     */
    public static void center(Component component) {
        Dimension screenSize = component.getToolkit().getScreenSize();
        int BORDER_SIZE = 50;
        screenSize.width -= BORDER_SIZE;
        screenSize.height -= BORDER_SIZE;
        Dimension componentSize = component.getSize();
        int xPos = (screenSize.width - componentSize.width) / 2;
        xPos = Math.max(xPos, 0);
        int yPos = (screenSize.height - componentSize.height) / 2;
        yPos = Math.max(yPos, 0);
        component.setLocation(new Point(xPos, yPos));
    }

    /* 
     * (non-Javadoc)
     * @see org.protege.editor.owl.codegeneration.GenerateCodeWithOptions#okClicked()
     */
    public void okClicked() {
        codeGenOptionFrame.setVisible(false);
        codeGenerationPreferences.putString(FACTORY_PREFS_KEY, options.getFactoryClassName());
        codeGenerationPreferences.putString(PACKAGE_PREFS_KEY, options.getPackage());
        codeGenerationPreferences.putString(FOLDER_PREFS_KEY, options.getOutputFolder().toString());
        OWLModelManager owlModelManager = getOWLModelManager();
        OWLOntology owlOntology = owlModelManager.getActiveOntology();
        CodeGenerationInference inference;
        if (options.useReasoner()) {
        	OWLReasoner reasoner = owlModelManager.getOWLReasonerManager().getCurrentReasoner();
        	inference = new ReasonerBasedInference(owlOntology, reasoner);
        }
        else {
        	inference = new SimpleInference(owlOntology);
        }
        try {
        	Utilities.deleteFolder(options.getOutputFolder());
            DefaultWorker.generateCode(owlOntology, options, new ProtegeNames(owlModelManager, options), inference);
            JOptionPane.showMessageDialog(null, "Java code successfully generated.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info("Java code successfully generated in folder " + options.getOutputFolder() + ".");
        } catch (IOException e) {
        	ProtegeApplication.getErrorLog().logError(e);
        }
    }

    /* (non-Javadoc)
     * @see org.protege.editor.owl.codegeneration.GenerateCodeWithOptions#cancelClicked()
     */
    public void cancelClicked() {
        codeGenOptionFrame.setVisible(false);
    }
}
