package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PasswordField;
import ca.corbett.forms.fields.ShortTextField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a FormPanel that contains an example of each of the basic form field types.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2029-11-25
 */
public class BasicFormPanel extends PanelBuilder {

    private FormPanel formPanel;

    @Override
    public String getTitle() {
        return "Forms: basic fields";
    }

    @Override
    public JPanel build() {
        formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        LabelField headerLabel = LabelField.createBoldHeaderLabel("Looking for basic form components? No problem!",
                                                                  20, 0, 8);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(headerLabel);

        formPanel.add(LabelField.createBoldHeaderLabel("Text input"));

        formPanel.add(new ShortTextField("Single-line text:", 16).setText("Hello."));
        formPanel.add(new PasswordField("Password entry:", 12).setPassword("password"));
        formPanel.add(LongTextField.ofFixedSizeMultiLine("Multi-line text:", 3, 21)
                                   .setText("Text fields are great for long text entry."));
        LongTextField textField = LongTextField.ofFixedSizeMultiLine("With pop-out edit:", 3, 21);
        textField.setAllowPopoutEditing(true);
        textField.setText("You can hit the \"Pop out\" button to edit this text in a resizable popup window.");
        formPanel.add(textField);

        formPanel.add(LabelField.createBoldHeaderLabel("General input components"));
        formPanel.add(new CheckBoxField("Checkboxes", true));
        formPanel.add(buildComboField());
        formPanel.add(new ColorField("Color chooser:", ColorSelectionType.SOLID).setColor(Color.BLUE));
        LabelField linkField = new LabelField("Hyperlink:", "Yes, you can add hyperlinks to your forms!");
        linkField.getMargins().setTop(10).setBottom(10);
        linkField.setHyperlink(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "You clicked the link! Hooray!");
            }
        });
        formPanel.add(linkField);
        formPanel.add(new NumberField("Number chooser:", 0, 0, 100, 1));

        formPanel.add(LabelField.createBoldHeaderLabel("File and directory choosers"));
        CheckBoxField showHiddenField = new CheckBoxField("Show hidden files", false);
        showHiddenField.addValueChangedListener(new ValueChangedListener() {
            @Override
            public void formFieldValueChanged(FormField field) {
                boolean isChecked = ((CheckBoxField)field).isChecked();
                formPanel.getFormFields()
                         .stream()
                         .filter(f -> f instanceof FileField)
                         .forEach(f -> ((FileField)f).setFileHidingEnabled(!isChecked));
            }
        });
        formPanel.add(showHiddenField);
        formPanel.add(new FileField("File chooser:", null, 15, FileField.SelectionType.AnyFile));
        FileField fileField = new FileField("With image preview:", null, 15, FileField.SelectionType.AnyFile);
        fileField.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || ImageUtil.isImageFile(f);
            }

            @Override
            public String getDescription() {
                return "Image files (png, jpg, bmp, gif)";
            }
        });
        fileField.setAccessory(new FileField.ImagePreviewAccessory());
        formPanel.add(fileField);
        formPanel.add(new FileField("Dir chooser:", null, 15, FileField.SelectionType.ExistingDirectory));

        return formPanel;
    }

    private void showHiddenFilesCheckboxClicked() {

    }

    private ComboField<String> buildComboField() {
        List<String> options = new ArrayList<>();
        options.add("Option 1");
        options.add("Option 2");
        options.add("Option 3");
        return new ComboField<>("Comboboxes:", options, 0, false);
    }
}
