package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.NumberField;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property that can store some integer value.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2024-12-30
 */
public class IntegerProperty extends AbstractProperty {

    private static final Logger logger = Logger.getLogger(IntegerProperty.class.getName());

    protected int value;
    protected int minValue;
    protected int maxValue;
    protected int stepValue;
    protected final boolean isSimpleValue;

    public IntegerProperty(String name, String label) {
        this(name, label, 0);
    }

    public IntegerProperty(String name, String label, int value) {
        super(name, label);
        isSimpleValue = true;
        this.value = value;
        this.minValue = Integer.MIN_VALUE;
        this.maxValue = Integer.MAX_VALUE;
        this.stepValue = 1;
    }

    public IntegerProperty(String name, String label, int value, int min, int max, int step) {
        super(name, label);
        isSimpleValue = false;
        this.value = value;
        this.minValue = min;
        this.maxValue = max;
        this.stepValue = step;
    }

    public int getValue() {
        return value;
    }

    public IntegerProperty setValue(int value) {
        if (value < minValue || value > maxValue) {
            return this;
        }
        this.value = value;
        return this;
    }

    public int getMinValue() {
        return minValue;
    }

    public IntegerProperty setMinValue(int minValue) {
        this.minValue = minValue;
        return this;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public IntegerProperty setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public int getStepValue() {
        return stepValue;
    }

    public IntegerProperty setStepValue(int stepValue) {
        this.stepValue = stepValue;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        if (!isSimpleValue) {
            props.setInteger(fullyQualifiedName + ".min", minValue);
            props.setInteger(fullyQualifiedName + ".max", maxValue);
            props.setInteger(fullyQualifiedName + ".step", stepValue);
        }
        props.setInteger(fullyQualifiedName + ".value", value);
    }

    @Override
    public void loadFromProps(Properties props) {
        if (!isSimpleValue) {
            minValue = props.getInteger(fullyQualifiedName + ".min", minValue);
            maxValue = props.getInteger(fullyQualifiedName + ".max", maxValue);
            stepValue = props.getInteger(fullyQualifiedName + ".step", stepValue);
        }
        value = props.getInteger(fullyQualifiedName + ".value", value);
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
            logger.log(Level.SEVERE, "IntegerProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        if (!field.isValid()) {
            logger.log(Level.WARNING, "IntegerProperty.loadFromFormField: received an invalid value from field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        value = ((NumberField)field).getCurrentValue().intValue();
    }

}
