package ca.corbett.forms.demo;

import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.TextField;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a FormPanel that contains an example of each of the basic form field types.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class BasicFormPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: The basics";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        formPanel.setStandardLeftMargin(24);

        LabelField headerLabel = LabelField.createBoldHeaderLabel("Looking for basic Swing components? No problem!",
                                                                  20);
        formPanel.addFormField(headerLabel);

        formPanel.addFormField(new TextField("Single-line text:", 15, 1, true));
        TextField textField = new TextField("Multi-line text:", 18, 4, true);
        textField.setAddScrollPaneWhenMultiLine(false);
        textField.setBottomMargin(12);
        formPanel.addFormField(textField);
        formPanel.addFormField(new CheckBoxField("Checkboxes", true));
        formPanel.addFormField(buildComboField());
        formPanel.addFormField(new ColorField("Color chooser:", Color.BLUE));

        LabelField labelField = LabelField.createPlainHeaderLabel(
                "Label fields like this don't allow input, but can help organize the form.");
        labelField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.addFormField(labelField);

        formPanel.addFormField(new FileField("File chooser:", null, 15, FileField.SelectionType.ExistingFile));
        formPanel.addFormField(
                new FileField("Directory chooser:", null, 15, FileField.SelectionType.ExistingDirectory));
        formPanel.addFormField(new NumberField("Number chooser:", 0, 0, 100, 1));

        formPanel.render();
        return formPanel;
    }

    private ComboField buildComboField() {
        List<String> options = new ArrayList<>();
        options.add("Option 1");
        options.add("Option 2");
        options.add("Option 3");
        return new ComboField("Comboboxes:", options, 0, false);
    }
}
