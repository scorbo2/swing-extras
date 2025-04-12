package ca.corbett.forms.fields;

import ca.corbett.forms.FormPanel;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 * A FormField wrapping a JComboBox.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class ComboField extends FormField {

    private final JComboBox<String> comboBox;
    private final ItemListener itemListener = e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            fireValueChangedEvent();
        }
    };

    /**
     * Creates a new ComboField with the given parameters.
     *
     * @param label         The label to use with this field.
     * @param options       The options to display in the dropdown.
     * @param selectedIndex The index to select by default.
     * @param isEditable    Whether to allow editing of the field.
     */
    public ComboField(String label, List<String> options, int selectedIndex, boolean isEditable) {
        fieldLabel = new JLabel(label);
        fieldLabel.setFont(fieldLabelFont);
        comboBox = new JComboBox<>(options.toArray(new String[]{}));
        comboBox.setFont(fieldLabelFont);
        comboBox.setSelectedIndex(selectedIndex);
        comboBox.setEditable(isEditable);
        comboBox.addItemListener(itemListener);
        fieldComponent = comboBox;
        showValidationLabel = false;
    }

    /**
     * Sets the available options in this field, overwriting whatever options were there before.
     *
     * @param options       The options to display in the dropdown.
     * @param selectedIndex The index to select by default.
     */
    public void setOptions(List<String> options, int selectedIndex) {
        comboBox.setModel(new DefaultComboBoxModel<>(options.toArray(new String[]{})));
        comboBox.setSelectedIndex(selectedIndex);
    }

    /**
     * Returns the currently selected item as a string.
     *
     * @return The current item.
     */
    public String getSelectedItem() {
        return (String)comboBox.getSelectedItem();
    }

    /**
     * Returns the index of the currently selected item.
     *
     * @return The index of the currently selected item.
     */
    public int getSelectedIndex() {
        return comboBox.getSelectedIndex();
    }

    /**
     * Sets the selected item. This is simply a passthrough to JComboBox.
     *
     * @param item The item to select.
     */
    public void setSelectedItem(String item) {
        comboBox.setSelectedItem(item);
    }

    /**
     * Sets the selected item index. This is simply a passthrough to JComboBox.
     *
     * @param index The index to select.
     */
    public void setSelectedIndex(int index) {
        comboBox.setSelectedIndex(index);
    }

    /**
     * Renders this field into the given container.
     *
     * @param container   The containing form panel.
     * @param constraints The GridBagConstraints to use.
     */
    @Override
    public void render(JPanel container, GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = FormPanel.LABEL_COLUMN;
        constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, componentSpacing);
        fieldLabel.setFont(fieldLabelFont);
        container.add(fieldLabel, constraints);

        constraints.gridx = FormPanel.CONTROL_COLUMN;
        constraints.insets = new Insets(topMargin, componentSpacing, bottomMargin, componentSpacing);
        container.add(comboBox, constraints);
    }

}
