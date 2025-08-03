package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.TextField;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.Color;
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
        formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);
    }

    @Override
    public String getTitle() {
        return "Forms: actions";
    }

    @Override
    public JPanel build() {
        LabelField headerLabel = LabelField.createBoldHeaderLabel("Form fields can have customizable Actions:", 20);
        headerLabel.setBottomMargin(24);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.addFormField(headerLabel);

        formPanel.addFormField(buildAlignmentChooser());

        List<String> options = new ArrayList<>();
        options.add("This option has no extra settings");
        options.add("This option has 1 extra setting");
        options.add("This option has lot of extra settings");
        mainComboField = new ComboField<>("Show/hide extra fields:", options, 0, false);
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

    private ComboField<Alignment> buildAlignmentChooser() {
        final ComboField<Alignment> combo = new ComboField<>("Change form alignment:",
                                                             List.of(Alignment.values()), 0, false);
        combo.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formPanel.setAlignment(combo.getSelectedItem());
            }
        });
        return combo;
    }
}
