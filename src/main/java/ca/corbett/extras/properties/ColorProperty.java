package ca.corbett.extras.properties;

import ca.corbett.extras.gradient.GradientColorField;
import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.forms.fields.FormField;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property that can either be a solid colour, a gradient, or both.
 *
 * @author scorbo2
 * @since 2024-12-30
 */
public class ColorProperty extends AbstractProperty {

  private final static Logger logger = Logger.getLogger(ColorProperty.class.getName());

  public enum ColorType {
    SOLID,
    GRADIENT,
    BOTH
  };

  protected ColorType colorType;
  protected Color color;
  protected GradientConfig gradient;

  public ColorProperty(String name, String label, ColorType colorType) {
    this(name, label, colorType, null, null);
  }

  public ColorProperty(String name, String label, ColorType colorType, Color color) {
    this(name, label, colorType, color, null);
  }

  public ColorProperty(String name, String label, ColorType colorType, GradientConfig gradient) {
    this(name, label, colorType, null, gradient);
  }

  public ColorProperty(String name, String label, ColorType colorType, Color color, GradientConfig gradient) {
    super(name, label);
    this.colorType = colorType;
    this.color = color == null ? Color.BLACK : color;
    this.gradient = gradient == null ? new GradientConfig() : gradient;
  }

  public ColorType getColorType() {
    return colorType;
  }

  public Color getColor() {
    return color;
  }

  public GradientConfig getGradientConfig() {
    return gradient;
  }

  public void setColor(Color color) {
    this.color = color == null ? Color.BLACK : color;
  }

  public void setGradient(GradientConfig gradient) {
    this.gradient = gradient == null ? new GradientConfig() : gradient;
  }

  @Override
  public void saveToProps(Properties props) {
    props.setString(fullyQualifiedName + ".colorType", colorType.name());
    if (colorType != ColorType.GRADIENT) {
      props.setColor(fullyQualifiedName + ".color", color);
    }
    if (colorType != ColorType.SOLID) {
      gradient.saveToProps(props, fullyQualifiedName + ".gradient.");
    }
  }

  @Override
  public void loadFromProps(Properties props) {
    String colorTypeStr = props.getString(fullyQualifiedName + ".colorType", colorType.name());
    colorType = ColorType.valueOf(colorTypeStr);
    if (colorType != ColorType.GRADIENT) {
      color = props.getColor(fullyQualifiedName + ".color", color);
    }
    if (colorType != ColorType.SOLID) {
      gradient.loadFromProps(props, fullyQualifiedName + ".gradient.");
    }
  }

  @Override
  public FormField generateFormField() {
    GradientColorField field;

    switch (colorType) {
      case SOLID:
        field = new GradientColorField(propertyLabel, color);
        break;

      case GRADIENT:
        field = new GradientColorField(propertyLabel, gradient);
        break;

      case BOTH:
      default:
        field = new GradientColorField(propertyLabel, color, gradient, true);
    }

    field.setIdentifier(fullyQualifiedName);
    field.setEnabled(!isReadOnly);
    field.setHelpText(helpText);
    return field;
  }

  @Override
  public void loadFromFormField(FormField field) {
    if (field.getIdentifier() == null
            || !field.getIdentifier().equals(fullyQualifiedName)
            || !(field instanceof GradientColorField)) {
      logger.log(Level.SEVERE, "ColorProperty.loadFromFormField: received the wrong field \"{0}\"", field.getIdentifier());
      return;
    }

    GradientColorField cField = (GradientColorField)field;
    switch (colorType) {
      case SOLID:
        setColor(cField.getColor());
        break;

      case GRADIENT:
        setGradient(cField.getGradient());
        break;

      default:
        Object something = cField.getSelectedValue();
        if (something instanceof Color) {
          setColor((Color)something);
        }
        else {
          setGradient((GradientConfig)something);
        }
    }
  }

}
