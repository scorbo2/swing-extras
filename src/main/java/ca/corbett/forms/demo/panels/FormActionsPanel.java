package ca.corbett.forms.demo.panels;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.TextField;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a FormPanel that gives some examples of attaching a custom Action
 * to FormFields to do certain things when their value changes.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class FormActionsPanel extends PanelBuilder {
    private final FormPanel formPanel;
    private ComboField mainComboField;

    public FormActionsPanel() {
        formPanel = new FormPanel();
    }

    @Override
    public String getTitle() {
        return "Form Actions";
    }

    @Override
    public JPanel build() {
        LabelField headerLabel = new LabelField("Form fields can have customizable Actions:");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setTopMargin(24);
        headerLabel.setBottomMargin(24);
        formPanel.addFormField(headerLabel);

        formPanel.addFormField(buildAlignmentChooser());

        List<String> options = new ArrayList<>();
        options.add("This option has no extra settings");
        options.add("This option has 1 extra setting");
        options.add("This option has lot of extra settings");
        mainComboField = new ComboField("Show/hide extra fields:", options, 0, false);
        formPanel.addFormField(mainComboField);

        final CheckBoxField extraField1 = new CheckBoxField("Extra setting", false);
        extraField1.setVisible(false);
        extraField1.setLeftMargin(32);
        formPanel.addFormField(extraField1);

        final TextField extraField2 = new TextField("Extra text field 1:", 10, 1, true);
        extraField2.setVisible(false);
        extraField2.setLeftMargin(32);
        formPanel.addFormField(extraField2);
        final TextField extraField3 = new TextField("Extra text field 2:", 10, 1, true);
        extraField3.setVisible(false);
        extraField3.setLeftMargin(32);
        formPanel.addFormField(extraField3);
        final TextField extraField4 = new TextField("Extra text field 3:", 10, 1, true);
        extraField4.setVisible(false);
        extraField4.setLeftMargin(32);
        formPanel.addFormField(extraField4);

        final NumberField numberField1 = new NumberField("Linked number field 1: ", 15, 0, 9999, 1);
        formPanel.addFormField(numberField1);
        final NumberField numberField2 = new NumberField("Linked number field 2: ", 15, 0, 9999, 1);
        formPanel.addFormField(numberField2);

        mainComboField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = mainComboField.getSelectedIndex();
                extraField1.setVisible(selectedIndex == 1);
                extraField2.setVisible(selectedIndex == 2);
                extraField3.setVisible(selectedIndex == 2);
                extraField4.setVisible(selectedIndex == 2);
            }
        });

        numberField1.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                numberField2.setCurrentValue(numberField1.getCurrentValue());
            }
        });

        numberField2.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                numberField1.setCurrentValue(numberField2.getCurrentValue());
            }
        });

        formPanel.render();
        return formPanel;
    }

    private ComboField buildAlignmentChooser() {
        List<String> options = new ArrayList<>();
        for (FormPanel.Alignment alignment : FormPanel.Alignment.values()) {
            options.add(alignment.name());
        }
        final ComboField combo = new ComboField("Change form alignment:", options, 0, false);
        combo.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formPanel.setAlignment(FormPanel.Alignment.valueOf(combo.getSelectedItem()));
                formPanel.render();
            }
        });
        return combo;
    }
}
