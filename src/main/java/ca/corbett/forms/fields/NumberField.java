package ca.corbett.forms.fields;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.util.Objects;

/**
 * A FormField that wraps a JSpinner to allow numeric input.
 * The underlying JSpinner is accessible by invoking getFieldComponent() and
 * casting the result to JSpinner.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2020-09-25
 */
public final class NumberField extends FormField {

    private final JSpinner spinner;

    /**
     * Creates an integer-based NumberField using the given starting values.
     *
     * @param labelText    The label to show for the field.
     * @param initialValue The starting value.
     * @param minimum      The minimum value.
     * @param maximum      The maximum value.
     * @param step         The increment value.
     */
    public NumberField(String labelText, int initialValue, int minimum, int maximum, int step) {
        this(labelText, new SpinnerNumberModel(initialValue, minimum, maximum, step));
    }

    /**
     * Creates a floating point NumberField using the given starting values.
     *
     * @param labelText    The label to show for the field.
     * @param initialValue The starting value.
     * @param minimum      The minimum value.
     * @param maximum      The maximum value.
     * @param step         The increment value.
     */
    public NumberField(String labelText, double initialValue, double minimum, double maximum, double step) {
        this(labelText, new SpinnerNumberModel(initialValue, minimum, maximum, step));
    }

    /**
     * Creates a NumberField using the given SpinnerModel. Normally you can use the other
     * constructors in this class as a convenience to avoid having to create the SpinnerModel
     * yourself, but this constructor is useful if you want to be able to change the
     * parameters of the spinner dynamically, for example to change the increment or to
     * set a new min or max value.
     *
     * @param labelText The label to show for the field.
     * @param model     A SpinnerModel instance containing our spinner parameters.
     */
    public NumberField(String labelText, SpinnerModel model) {
        spinner = new JSpinner(model);
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fireValueChangedEvent();
            }

        });
        fieldComponent = spinner;
        fieldComponent.setPreferredSize(new Dimension(60, 22)); // arbitrary default value
        fieldLabel.setText(labelText);
    }

    public Number getCurrentValue() {
        return (Number)spinner.getValue();
    }

    public NumberField setCurrentValue(Number value) {
        if (Objects.equals(spinner.getValue(), value)) {
            return this; // reject no-op changes
        }
        if (value == null) {
            return this; // reject null
        }
        spinner.setValue(value);
        return this;
    }

}
