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

/**
 * @author z.khan
 * 
 */
public class JavaCodeGeneratorPanel extends JPanel {

    private static final long serialVersionUID = 4160225251535243881L;

    private GenerateCodeWithOptions generateCodeWithOptions;

    private JCheckBox abstractCheckBox;

    private JTextField factoryClassNameTextField;

    private JFileChooser fileChooser = new JFileChooser(".");

    private EditableJavaCodeGeneratorOptions options;

    private JTextField packageTextField;

    private JTextField rootFolderTextField;

    private JCheckBox setCheckBox;

    private JCheckBox prefixCheckBox;

    private JButton okButton;

    private JButton cancelButton;

    public JavaCodeGeneratorPanel(EditableJavaCodeGeneratorOptions options,
            GenerateCodeWithOptions generateCodeWithOptions) {

        this.options = options;
        this.generateCodeWithOptions = generateCodeWithOptions;

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
        rootFolderTextField.setText(fileChooser.getCurrentDirectory().toString());

        factoryClassNameTextField = new JTextField();
        if (options.getFactoryClassName() != null) {
            factoryClassNameTextField.setText(options.getFactoryClassName());
        }

        abstractCheckBox = new JCheckBox("Create abstract base files (e.g., Person_)");
        abstractCheckBox.setSelected(options.getAbstractMode());

        setCheckBox = new JCheckBox("Return Set instead of Collection");
        setCheckBox.setSelected(options.getSetMode());

        prefixCheckBox = new JCheckBox("Include prefixes in generated Java names");
        prefixCheckBox.setSelected(options.getPrefixMode());

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
        add(getComponentWithNonStrechingVertically(rootOutPutFolderPanel));
        add(Box.createVerticalStrut(8));

        JPanel packageNameJPanel = new JPanel(new BorderLayout());
        packageNameJPanel.add(new JLabel("Java package"), BorderLayout.WEST);
        add(packageNameJPanel);
        add(getComponentWithNonStrechingVertically(packageTextField));
        add(Box.createVerticalStrut(8));

        JPanel factoryClassNameJPanel = new JPanel(new BorderLayout());
        factoryClassNameJPanel.add(new JLabel("Factory class name"), BorderLayout.WEST);
        add(factoryClassNameJPanel);
        add(getComponentWithNonStrechingVertically(factoryClassNameTextField));
        add(Box.createVerticalStrut(8));

        add(createCheckBoxPanel(abstractCheckBox));
        add(Box.createVerticalStrut(8));

        add(createCheckBoxPanel(setCheckBox));
        add(Box.createVerticalStrut(8));

        add(createCheckBoxPanel(prefixCheckBox));
        add(Box.createVerticalStrut(8));

        JPanel buttonJPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonJPanel.add(okButton);
        buttonJPanel.add(cancelButton);
        add(buttonJPanel);

        setVisible(true);
        setButtonListeners();
        
        packageTextField.requestFocus();

    }

    private void setButtonListeners() {
        okButton.addMouseListener(new MouseAdapter() {

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
                                : JavaCodeGeneratorConstants.FACTORY_CLASS_NAME);

                options.setAbstractMode(abstractCheckBox.isSelected());
                options.setSetMode(setCheckBox.isSelected());
                options.setPrefixMode(prefixCheckBox.isSelected());
                if (options.getPackage() == null) {
                    JOptionPane.showMessageDialog(null, "Enter package name.", "Error", JOptionPane.ERROR_MESSAGE);

                } else {
                    generateCodeWithOptions.okClicked();
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
                generateCodeWithOptions.cancelClicked();
            }

        });
    }

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

        options.setAbstractMode(abstractCheckBox.isSelected());
        options.setSetMode(setCheckBox.isSelected());
        options.setPrefixMode(prefixCheckBox.isSelected());
        options.setFactoryClassName(factoryClassNameTextField.getText());

        String pack = packageTextField.getText().trim();
        options.setPackage(pack.length() > 0 ? pack : null);
    }

    private void selectFolder() {
        if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            rootFolderTextField.setText(file.toString());
        }
    }

    private JPanel getComponentWithNonStrechingVertically(JComponent component) {
        JPanel componentPanel = new JPanel(new BorderLayout());
        componentPanel.add(component, BorderLayout.NORTH);

        JPanel flowJPanel = new JPanel(new FlowLayout());
        componentPanel.add(flowJPanel, BorderLayout.WEST);
        return componentPanel;

    }
}
