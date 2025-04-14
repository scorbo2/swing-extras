package ca.corbett.forms.fields;

import ca.corbett.forms.FormPanel;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * A FormField to wrap a JCheckBox.
 * <p>
 * A note about validation: checkboxes don't generally "count" when a FormPanel validates itself.
 * That is, they won't show a little checkbox label to indicate that the selected value is "correct".
 * However, if you invoked addFieldValidator(), then the field will automatically be included
 * in any calls to formPanel.isFormValid().
 * </p>
 *
 * @author scorbo2
 * @since 2019-11-27
 */
public final class CheckBoxField extends FormField {

    public CheckBoxField(String labelText, boolean isChecked) {
        fieldLabel = new JLabel();
        fieldLabel.setFont(fieldLabelFont);
        fieldComponent = new JCheckBox(labelText, isChecked);
        fieldComponent.setFont(fieldLabelFont);
        ((JCheckBox)fieldComponent).addItemListener(e -> fireValueChangedEvent());
        showValidationLabel = false;
    }

    public boolean isChecked() {
        return ((JCheckBox)fieldComponent).isSelected();
    }

    public void setChecked(boolean checked) {
        ((JCheckBox)fieldComponent).setSelected(checked);
    }

    @Override
    public void render(JPanel container, GridBagConstraints constraints) {
        // Note we don't add the fieldLabel here because a checkbox has its own label built in.

        constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, componentSpacing);
        constraints.gridx = FormPanel.LABEL_COLUMN;
        constraints.gridwidth = 2;
        constraints.gridy = constraints.gridy + 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        fieldComponent.setFont(fieldLabelFont);
        container.add(fieldComponent, constraints);
    }
}
