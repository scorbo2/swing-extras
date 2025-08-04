package ca.corbett.forms.fields;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * A FormField wrapping a JComboBox.
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public class ComboField<T> extends FormField {

    private final JComboBox<T> comboBox;
    private final DefaultComboBoxModel<T> comboModel;

    public ComboField(String label) {
        fieldLabel.setText(label);
        comboModel = new DefaultComboBoxModel<>();
        comboBox = new JComboBox<>(comboModel);
        comboBox.setEditable(false);
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                fireValueChangedEvent();
            }
        });
        fieldComponent = comboBox;
    }

    public ComboField(String label, List<T> options, int selectedIndex) {
        this(label);
        comboModel.addAll(options);
        comboBox.setSelectedIndex(selectedIndex);
    }

    /**
     * Creates a new ComboField with the given parameters.
     *
     * @param label         The label to use with this field.
     * @param options       The options to display in the dropdown.
     * @param selectedIndex The index to select by default.
     * @param isEditable    Whether to allow editing of the field.
     */
    public ComboField(String label, List<T> options, int selectedIndex, boolean isEditable) {
        this(label, options, selectedIndex);
        comboBox.setEditable(isEditable);
    }

    /**
     * Overridden here as we generally don't want to show a validation label on a combo box.
     * Will return true only if one or more FieldValidators have been explicitly assigned.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }

    /**
     * Sets the available options in this field, overwriting whatever options were there before.
     *
     * @param options       The options to display in the dropdown.
     * @param selectedIndex The index to select by default.
     */
    public ComboField<T> setOptions(List<T> options, int selectedIndex) {
        comboModel.removeAllElements();
        comboModel.addAll(options);
        comboBox.setSelectedIndex(selectedIndex);
        return this;
    }

    /**
     * Returns the currently selected item as a string.
     *
     * @return The current item.
     */
    public T getSelectedItem() {
        //noinspection unchecked
        return (T)comboBox.getSelectedItem();
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
    public ComboField<T> setSelectedItem(T item) {
        comboBox.setSelectedItem(item);
        return this;
    }

    /**
     * Sets the selected item index. This is simply a passthrough to JComboBox.
     *
     * @param index The index to select.
     */
    public ComboField<T> setSelectedIndex(int index) {
        comboBox.setSelectedIndex(index);
        return this;
    }
}
