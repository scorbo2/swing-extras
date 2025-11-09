package ca.corbett.forms.demo;

import ca.corbett.extras.demo.SnippetAction;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JPanel;
import java.util.List;

/**
 * Builds a FormPanel that gives some examples of attaching a custom Action
 * to FormFields to do certain things when their value changes.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-25
 */
public class FormActionsPanel extends PanelBuilder {
    private FormPanel formPanel;
    private ComboField<String> mainComboField;

    @Override
    public String getTitle() {
        return "Forms: actions";
    }

    @Override
    public JPanel build() {
        formPanel = buildFormPanel("Custom form actions");

        formPanel.add(new LabelField("<html>Wiring up custom actions to form fields is quite easy."
                                             + "<br>You can supply any action you want, and that action can"
                                             + "<br>affect other form fields. See some examples below!</html>"));

        // Add a combo field that can change our layout for this demo panel:
        // (we have an enum for this, so building the combo with the right options is very easy)
        final ComboField<Alignment> alignmentCombo = new ComboField<>("Change form alignment:",
                                                                      List.of(Alignment.values()),
                                                                      0,
                                                                      false);
        alignmentCombo.addValueChangedListener(field -> formPanel.setAlignment(alignmentCombo.getSelectedItem()));
        formPanel.add(alignmentCombo);

        // Let's add a combobox that can show or hide other form fields:
        List<String> options = List.of(
                "This option has no extra settings",
                "This option has 1 extra setting",
                "This option has lot of extra settings");
        mainComboField = new ComboField<>("Show/hide extra fields:", options, 0, false);
        formPanel.add(mainComboField);

        // Add the single hidden field for the second combo option:
        final CheckBoxField extraField1 = new CheckBoxField("Extra setting", false);
        extraField1.setVisible(false);
        extraField1.getMargins().setLeft(32);
        formPanel.add(extraField1);

        // Add the group of hidden fields for the third combo option:
        final ShortTextField extraField2 = new ShortTextField("Extra text field 1:", 10);
        extraField2.setVisible(false);
        extraField2.getMargins().setLeft(32);
        formPanel.add(extraField2);
        final ShortTextField extraField3 = new ShortTextField("Extra text field 2:", 10);
        extraField3.setVisible(false);
        extraField3.getMargins().setLeft(32);
        formPanel.add(extraField3);
        final ShortTextField extraField4 = new ShortTextField("Extra text field 3:", 10);
        extraField4.setVisible(false);
        extraField4.getMargins().setLeft(32);
        formPanel.add(extraField4);

        // Now we can add a ValueChangedListener to the main combo to update field visibility as needed:
        mainComboField.addValueChangedListener(field -> {
            int selectedIndex = mainComboField.getSelectedIndex();
            extraField1.setVisible(selectedIndex == 1);
            extraField2.setVisible(selectedIndex == 2);
            extraField3.setVisible(selectedIndex == 2);
            extraField4.setVisible(selectedIndex == 2);
        });

        // Let's add two NumberFields that we can link together:
        final NumberField numberField1 = new NumberField("Linked number field 1: ", 15, 0, 9999, 1);
        formPanel.add(numberField1);
        final NumberField numberField2 = new NumberField("Linked number field 2: ", 15, 0, 9999, 1);
        formPanel.add(numberField2);

        // Add a listener to each one such that they keep each other in sync:
        numberField1.addValueChangedListener(field -> {
            if (!numberField2.getCurrentValue().equals(numberField1.getCurrentValue())) {
                numberField2.setCurrentValue(numberField1.getCurrentValue());
            }
        });
        numberField2.addValueChangedListener(field -> {
            if (!numberField1.getCurrentValue().equals(numberField2.getCurrentValue())) {
                numberField1.setCurrentValue(numberField2.getCurrentValue());
            }
        });

        formPanel.add(createSnippetLabel(new FormActionSnippetAction()));

        return formPanel;
    }

    /**
     * Shows a code snippet for adding custom actions to a FormPanel.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    private static class FormActionSnippetAction extends SnippetAction {
        @Override
        protected String getSnippet() {
            return """
                    // Add a combo field that can change our layout for this demo panel:
                    // (we have an enum for this, so building the combo with the right options is very easy)
                    final ComboField<Alignment> alignmentCombo = new ComboField<>("Change form alignment:",
                                                                                  List.of(Alignment.values()),
                                                                                  0,
                                                                                  false);
                    alignmentCombo.addValueChangedListener(field -> formPanel.setAlignment(alignmentCombo.getSelectedItem()));
                    
                    // Let's create two NumberFields that we can link together:
                    final NumberField numberField1 = new NumberField("Linked number field 1: ", 15, 0, 9999, 1);
                    final NumberField numberField2 = new NumberField("Linked number field 2: ", 15, 0, 9999, 1);
                    
                    // Add a listener to each one such that they keep each other in sync:
                    numberField1.addValueChangedListener(field -> {
                        if (!numberField2.getCurrentValue().equals(numberField1.getCurrentValue())) {
                            numberField2.setCurrentValue(numberField1.getCurrentValue());
                        }
                    });
                    numberField2.addValueChangedListener(field -> {
                        if (!numberField1.getCurrentValue().equals(numberField2.getCurrentValue())) {
                            numberField1.setCurrentValue(numberField2.getCurrentValue());
                        }
                    });
                    """;
        }
    }
}
