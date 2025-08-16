package ca.corbett.extras.properties;

import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.FormField;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a property that can either be a solid colour, a gradient, or both.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2024-12-30
 */
public class ColorProperty extends AbstractProperty {

    private final static Logger logger = Logger.getLogger(ColorProperty.class.getName());

    protected ColorSelectionType colorSelectionType;

    protected Color solidColor;
    protected Gradient gradient;

    public ColorProperty(String name, String label, ColorSelectionType colorSelectionType) {
        super(name, label);
        this.colorSelectionType = colorSelectionType;
        this.solidColor = solidColor == null ? Color.BLACK : solidColor;
        this.gradient = Gradient.createDefault();
    }

    public ColorSelectionType getColorType() {
        return colorSelectionType;
    }

    public Color getSolidColor() {
        return solidColor;
    }

    public GradientType getGradientType() {
        return gradient.type();
    }

    public Color getGradientColor1() {
        return gradient.color1();
    }

    public Color getGradientColor2() {
        return gradient.color2();
    }

    public Gradient getGradient() {
        return gradient;
    }

    public ColorProperty setSolidColor(Color solidColor) {
        if (solidColor == null) {
            return this; // ignore nulls
        }
        this.solidColor = solidColor;
        return this;
    }

    public ColorProperty setGradient(Gradient gradient) {
        if (gradient == null) {
            return this; // ignore nulls
        }
        this.gradient = gradient;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setString(fullyQualifiedName + ".colorType", colorSelectionType.name());
        if (colorSelectionType != ColorSelectionType.GRADIENT) {
            props.setColor(fullyQualifiedName + ".color", solidColor);
        }
        if (colorSelectionType != ColorSelectionType.SOLID) {
            props.setString(fullyQualifiedName + "gradientType", gradient.type().name());
            props.setColor(fullyQualifiedName + "color1", gradient.color1());
            props.setColor(fullyQualifiedName + "color2", gradient.color2());
        }
    }

    @Override
    public void loadFromProps(Properties props) {
        String colorTypeStr = props.getString(fullyQualifiedName + ".colorType", colorSelectionType.name());
        colorSelectionType = ColorSelectionType.valueOf(colorTypeStr);
        if (colorSelectionType != ColorSelectionType.GRADIENT) {
            solidColor = props.getColor(fullyQualifiedName + ".color", solidColor);
        }
        if (colorSelectionType != ColorSelectionType.SOLID) {
            GradientType gradientType = GradientType.valueOf(
                    props.getString(fullyQualifiedName + "gradientType", gradient.type().name()));
            Color gradientColor1 = props.getColor(fullyQualifiedName + "color1", gradient.color1());
            Color gradientColor2 = props.getColor(fullyQualifiedName + "color2", gradient.color2());
            gradient = new Gradient(gradientType, gradientColor1, gradientColor2);
        }
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return switch (colorSelectionType) {
            case SOLID -> new ColorField(propertyLabel, ColorSelectionType.SOLID).setColor(solidColor);
            case GRADIENT -> new ColorField(propertyLabel, ColorSelectionType.GRADIENT).setGradient(gradient);
            default ->
                    new ColorField(propertyLabel, ColorSelectionType.EITHER).setGradient(gradient).setColor(solidColor);
        };
    }

    @Override
    public void loadFromFormField(FormField field) {
        if (field.getIdentifier() == null || !field.getIdentifier()
                                                   .equals(fullyQualifiedName) || !(field instanceof ColorField)) {
            logger.log(Level.SEVERE, "ColorProperty.loadFromFormField: received the wrong field \"{0}\"",
                       field.getIdentifier());
            return;
        }

        ColorField cField = (ColorField)field;
        //@formatter:off
        switch (colorSelectionType) {
            case SOLID:    setSolidColor(cField.getColor()); break;
            case GRADIENT: setGradient(cField.getGradient()); break;

            default:
                Object something = cField.getSelectedValue();
                if (something instanceof Color) {
                    setSolidColor((Color) something);
                }
                else {
                    setGradient((Gradient) something);
                }
        }
        //@formatter:on
    }

}
