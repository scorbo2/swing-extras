package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.NumberField;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property that can store some floating point value.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2024-12-30
 */
public class DecimalProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(DecimalProperty.class.getName());

    protected double value;
    protected double minValue;
    protected double maxValue;
    protected double stepValue;
    protected final boolean isSimpleValue;

    public DecimalProperty(String name, String label) {
        this(name, label, 0);
    }

    public DecimalProperty(String name, String label, double value) {
        super(name, label);
        isSimpleValue = true;
        this.minValue = Double.MIN_VALUE;
        this.maxValue = Double.MAX_VALUE;
        this.stepValue = 1.0;
        this.value = value;
    }

    public DecimalProperty(String name, String label, double value, double min, double max, double step) {
        super(name, label);
        isSimpleValue = false;
        this.value = value;
        this.minValue = min;
        this.maxValue = max;
        this.stepValue = step;
    }

    public double getValue() {
        return value;
    }

    public DecimalProperty setValue(double value) {
        if (value < minValue || value > maxValue) {
            return this;
        }
        this.value = value;
        return this;
    }

    public double getMinValue() {
        return minValue;
    }

    public DecimalProperty setMinValue(double minValue) {
        this.minValue = minValue;
        return this;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public DecimalProperty setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public double getStepValue() {
        return stepValue;
    }

    public DecimalProperty setStepValue(double stepValue) {
        this.stepValue = stepValue;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        if (!isSimpleValue) {
            props.setDouble(fullyQualifiedName + ".min", minValue);
            props.setDouble(fullyQualifiedName + ".max", maxValue);
            props.setDouble(fullyQualifiedName + ".step", stepValue);
        }
        props.setDouble(fullyQualifiedName + ".value", value);
    }

    @Override
    public void loadFromProps(Properties props) {
        if (!isSimpleValue) {
            minValue = props.getDouble(fullyQualifiedName + ".min", minValue);
            maxValue = props.getDouble(fullyQualifiedName + ".max", maxValue);
            stepValue = props.getDouble(fullyQualifiedName + ".step", stepValue);
        }
        value = props.getDouble(fullyQualifiedName + ".value", value);
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return new NumberField(propertyLabel, value, minValue, maxValue, stepValue);
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null
                || !field.getIdentifier().equals(fullyQualifiedName)
                || !(field instanceof NumberField)) {
            logger.log(Level.SEVERE, "DecimalProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        value = ((NumberField)field).getCurrentValue().doubleValue();
    }

}
