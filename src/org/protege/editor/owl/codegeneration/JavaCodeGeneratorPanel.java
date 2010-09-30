package org.protege.editor.owl.codegeneration;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author z.khan
 *
 */
public class JavaCodeGeneratorPanel extends JPanel {

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

    public JavaCodeGeneratorPanel(EditableJavaCodeGeneratorOptions options,
            GenerateCodeWithOptions generateCodeWithOptions) {

        this.options = options;
        this.generateCodeWithOptions = generateCodeWithOptions;

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
        //        LabeledComponent lc = new LabeledComponent("Root output folder", rootFolderTextField);
        JButton selectFolder = new JButton("Select folder...");
        selectFolder.setAction(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                selectFolder();

            }
        });

        okButton = new JButton("Ok");

        JPanel rootOutPutFolderPanel = new JPanel();
        rootOutPutFolderPanel.setLayout(new BoxLayout(rootOutPutFolderPanel, BoxLayout.LINE_AXIS));
        rootOutPutFolderPanel.add(new JLabel("Root output folder"));
        rootOutPutFolderPanel.add(selectFolder);
        add(rootOutPutFolderPanel);
        add(rootFolderTextField);
        add(Box.createVerticalStrut(8));
        add(new JLabel("Java package"));
        add(packageTextField);
        add(Box.createVerticalStrut(8));
        add(new JLabel("Factory class name"));
        add(factoryClassNameTextField);
        add(Box.createVerticalStrut(8));
        add(createCheckBoxPanel(abstractCheckBox));
        add(Box.createVerticalStrut(8));
        add(createCheckBoxPanel(setCheckBox));
        add(Box.createVerticalStrut(8));
        add(createCheckBoxPanel(prefixCheckBox));
        add(Box.createVerticalStrut(8));
        add(okButton);
        setVisible(true);

        setButtonListeners();

    }

    private void setButtonListeners() {
        okButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
                System.out.println("ok button clicked");
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
                                : null);

                options.setAbstractMode(abstractCheckBox.isSelected());
                options.setSetMode(setCheckBox.isSelected());
                options.setPrefixMode(prefixCheckBox.isSelected());

                generateCodeWithOptions.okClicked();

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
}
