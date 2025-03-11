package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.NumberField;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property that can store some integer value.
 *
 * @author scorbo2
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

  public void setValue(int value) {
    if (value < minValue || value > maxValue) {
      return;
    }
    this.value = value;
  }

  public int getMinValue() {
    return minValue;
  }

  public void setMinValue(int minValue) {
    this.minValue = minValue;
  }

  public int getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(int maxValue) {
    this.maxValue = maxValue;
  }

  public int getStepValue() {
    return stepValue;
  }

  public void setStepValue(int stepValue) {
    this.stepValue = stepValue;
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
  public FormField generateFormField() {
    NumberField field = new NumberField(propertyLabel, value, minValue, maxValue, stepValue);
    field.setIdentifier(fullyQualifiedName);
    return field;
  }

  @Override
  public void loadFromFormField(FormField field) {
    if (field.getIdentifier() == null
            || !field.getIdentifier().equals(fullyQualifiedName)
            || !(field instanceof NumberField)) {
      logger.log(Level.SEVERE, "IntegerProperty.loadFromFormField: received the wrong field \"{0}\"", field.getIdentifier());
      return;
    }

    value = ((NumberField)field).getCurrentValue().intValue();
  }

}
