package org.protege.editor.owl.codegeneration;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.protege.editor.owl.model.inference.NoOpReasoner;
import org.protege.owl.codegeneration.CodeGenerationOptions;
import org.protege.owl.codegeneration.Constants;

/**
 * This class creates a panel, which contains options for code generations.
 * @author z.khan
 * 
 */
public class JavaCodeGeneratorPanel extends JPanel {

    private static final long serialVersionUID = 4160225251535243881L;

    private GenerateCodeCallback generateCodeCallback;

    private JTextField factoryClassNameTextField;

    private JFileChooser fileChooser = new JFileChooser(".");

    private CodeGenerationOptions options;

    private JTextField packageTextField;

    private JTextField rootFolderTextField;

    private JCheckBox useReasonerCheckBox;

    private JButton okButton;

    private JButton cancelButton;

    /**Constructor
     * @param options the EditableJavaCodeGeneratorOptions object in which to save the option values. 
     * @param generateCodeWithOptions
     */
    public JavaCodeGeneratorPanel(CodeGenerationOptions options,
    		                      GenerateCodeCallback generateCodeWithOptions) {

        this.options = options;
        this.generateCodeCallback = generateCodeWithOptions;

        Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        setBorder(border);

        packageTextField = new JTextField();
        if (options.getPackage() != null) {
            packageTextField.setText(options.getPackage());
        }
        rootFolderTextField = new JTextField();
        if (options.getOutputFolder() != null) {
            rootFolderTextField.setText(options.getOutputFolder().getAbsolutePath());
        }

        fileChooser.setDialogTitle("Select output folder");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        factoryClassNameTextField = new JTextField();
        if (options.getFactoryClassName() != null) {
            factoryClassNameTextField.setText(options.getFactoryClassName());
        }

        useReasonerCheckBox = new JCheckBox("Use Reasoner");
        if (generateCodeWithOptions.getOWLModelManager().getReasoner() instanceof NoOpReasoner) {
        	useReasonerCheckBox.setEnabled(false);
        	options.setUseReasoner(false);
        }
        useReasonerCheckBox.setSelected(options.useReasoner());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JButton selectFolder = new JButton("Select folder...");
        selectFolder.setAction(new AbstractAction() {

            private static final long serialVersionUID = 3783359547867594508L;

            public void actionPerformed(ActionEvent e) {
                selectFolder();

            }
        });
        URL url = JavaCodeGeneratorPanel.class.getResource("select_folder.gif");
        selectFolder.setIcon(new ImageIcon(url));

        okButton = new JButton("Ok");

        cancelButton = new JButton("Cancel");

        JPanel rootOutPutFolderPanel = new JPanel(new BorderLayout(5, 5));
        rootOutPutFolderPanel.add(new JLabel("Root output folder"), BorderLayout.WEST);
        rootOutPutFolderPanel.add(selectFolder, BorderLayout.EAST);
        rootOutPutFolderPanel.add(rootFolderTextField, BorderLayout.SOUTH);
        add(getComponentWithNonStretchingVertically(rootOutPutFolderPanel));
        add(Box.createVerticalStrut(8));

        JPanel packageNameJPanel = new JPanel(new BorderLayout());
        packageNameJPanel.add(new JLabel("Java package"), BorderLayout.WEST);
        add(packageNameJPanel);
        add(getComponentWithNonStretchingVertically(packageTextField));
        add(Box.createVerticalStrut(8));

        JPanel factoryClassNameJPanel = new JPanel(new BorderLayout());
        factoryClassNameJPanel.add(new JLabel("Factory class name"), BorderLayout.WEST);
        add(factoryClassNameJPanel);
        add(getComponentWithNonStretchingVertically(factoryClassNameTextField));
        add(Box.createVerticalStrut(8));

        add(createCheckBoxPanel(useReasonerCheckBox));
        add(Box.createVerticalStrut(8));

        JPanel buttonJPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonJPanel.add(okButton);
        buttonJPanel.add(cancelButton);
        add(buttonJPanel);

        setVisible(true);
        setButtonListeners();
        
        packageTextField.requestFocus();

    }

    /**
     * Adds listeners
     */
    private void setButtonListeners() {
        okButton.addMouseListener(new MouseAdapter() {

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseClicked(MouseEvent e) {

                File newFile = null;
                String rootFolder = rootFolderTextField.getText().trim();
                if (rootFolder.length() > 0) {
                    newFile = new File(rootFolder);
                }
                options.setOutputFolder(newFile);
                options.setPackage(packageTextField.getText().trim().length() > 0 ? packageTextField.getText().trim()
                        : null);
                options
                        .setFactoryClassName(factoryClassNameTextField.getText().trim().length() > 0 ? factoryClassNameTextField
                                .getText().trim()
                                : Constants.FACTORY_CLASS_NAME);
                options.setUseReasoner(useReasonerCheckBox.isSelected());
                if (options.getPackage() == null) {
                    JOptionPane.showMessageDialog(null, "Enter package name.", "Error", JOptionPane.ERROR_MESSAGE);

                } else {
                    generateCodeCallback.okClicked();
                }

            }
        });

        cancelButton.addMouseListener(new MouseAdapter() {

            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                generateCodeCallback.cancelClicked();
            }

        });
    }

    /** Create check box panel
     * @param comp
     * @return
     */
    private JPanel createCheckBoxPanel(Component comp) {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(BorderLayout.WEST, comp);
        southPanel.add(BorderLayout.CENTER, new JPanel());
        southPanel.setPreferredSize(new Dimension(300, 24));
        return southPanel;
    }

    public void ok() {
        File newFile = null;
        String rootFolder = rootFolderTextField.getText().trim();
        if (rootFolder.length() > 0) {
            newFile = new File(rootFolder);
        }
        options.setOutputFolder(newFile);

        options.setUseReasoner(useReasonerCheckBox.isSelected());
        options.setFactoryClassName(factoryClassNameTextField.getText());

        String pack = packageTextField.getText().trim();
        options.setPackage(pack.length() > 0 ? pack : null);
    }

    /**
     * Retrieves the folder selected and sets the path to TextField
     */
    private void selectFolder() {
        if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            rootFolderTextField.setText(file.toString());
        }
    }

    /**Returns a panel with component so that the panel is not stretchable vertically
     * @param component The JComponent to add to the panel
     * @return
     */
    private JPanel getComponentWithNonStretchingVertically(JComponent component) {
        JPanel componentPanel = new JPanel(new BorderLayout());
        componentPanel.add(component, BorderLayout.NORTH);

        JPanel flowJPanel = new JPanel(new FlowLayout());
        componentPanel.add(flowJPanel, BorderLayout.WEST);
        return componentPanel;

    }
}
