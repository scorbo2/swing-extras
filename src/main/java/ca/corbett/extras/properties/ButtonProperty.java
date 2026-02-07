package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.FormField;

import java.awt.Dimension;

/**
 * Wraps a ButtonField, and allows you to add buttons to a FormPanel dynamically.
 * Note that this property stores no data! As such, the loadFromFormField method
 * here does nothing. This property exists solely
 * to allow buttons to be added to a FormPanel via the property mechanism.
 * You can add a FormFieldGenerationListener to this property to be notified
 * when the empty ButtonField is generated, and then add buttons to it as needed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class ButtonProperty extends AbstractProperty {

    private int alignment = ButtonField.DEFAULT_ALIGNMENT;
    private int hgap = ButtonField.DEFAULT_HGAP;
    private int vgap = ButtonField.DEFAULT_VGAP;
    private Dimension buttonPreferredSize;

    public ButtonProperty(String fullyQualifiedName) {
        this(fullyQualifiedName, "");
    }

    public ButtonProperty(String fullyQualifiedName, String propertyLabel) {
        super(fullyQualifiedName, propertyLabel);

        // Most properties generate FormField instances that allow user input, but we do not:
        allowsUserInput = false;
    }

    /**
     * One of the FlowLayout alignment constants: LEFT, CENTER, RIGHT, LEADING, or TRAILING.
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * One of the FlowLayout alignment constants: LEFT, CENTER, RIGHT, LEADING, or TRAILING.
     */
    public ButtonProperty setAlignment(int alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * The horizontal gap between buttons.
     */
    public int getHgap() {
        return hgap;
    }

    /**
     * The horizontal gap between buttons.
     */
    public ButtonProperty setHgap(int hgap) {
        this.hgap = hgap;
        return this;
    }

    /**
     * The vertical gap between buttons.
     */
    public int getVgap() {
        return vgap;
    }

    /**
     * The vertical gap between buttons.
     */
    public ButtonProperty setVgap(int vgap) {
        this.vgap = vgap;
        return this;
    }

    /**
     * The preferred size for buttons in this ButtonField.
     * If null, buttons will use their own preferred size.
     */
    public Dimension getButtonPreferredSize() {
        return buttonPreferredSize;
    }

    /**
     * Sets the preferred size for buttons in this ButtonField.
     * If null, buttons will use their own preferred size.
     */
    public void setButtonPreferredSize(Dimension buttonPreferredSize) {
        this.buttonPreferredSize = buttonPreferredSize;
    }

    @Override
    public void saveToProps(Properties props) {
        props.setInteger(fullyQualifiedName+".alignment", alignment);
        props.setInteger(fullyQualifiedName+".hgap", hgap);
        props.setInteger(fullyQualifiedName+".vgap", vgap);
        if (buttonPreferredSize != null) {
            props.setInteger(fullyQualifiedName + ".buttonWidth", buttonPreferredSize.width);
            props.setInteger(fullyQualifiedName + ".buttonHeight", buttonPreferredSize.height);
        }
        else {
            props.remove(fullyQualifiedName + ".buttonWidth");
            props.remove(fullyQualifiedName + ".buttonHeight");
        }
    }

    @Override
    public void loadFromProps(Properties props) {
        alignment = props.getInteger(fullyQualifiedName+".alignment", alignment);
        hgap = props.getInteger(fullyQualifiedName+".hgap", hgap);
        vgap = props.getInteger(fullyQualifiedName+".vgap", vgap);
        int width = props.getInteger(fullyQualifiedName + ".buttonWidth", -1);
        int height = props.getInteger(fullyQualifiedName + ".buttonHeight", -1);
        if (width > 0 && height > 0) {
            buttonPreferredSize = new Dimension(width, height);
        }
        else {
            buttonPreferredSize = null;
        }
    }

    /**
     * Creates and returns an empty ButtonField.
     * Add a FormFieldGenerationListener to be notified when the field is created,
     * so you can add buttons to it as needed.
     */
    @Override
    protected FormField generateFormFieldImpl() {
        ButtonField buttonField = new ButtonField();
        buttonField.setAlignment(alignment);
        buttonField.setHgap(hgap);
        buttonField.setVgap(vgap);
        buttonField.setButtonPreferredSize(buttonPreferredSize);
        if (propertyLabel != null && !propertyLabel.isBlank()) {
            buttonField.getFieldLabel().setText(propertyLabel);
        }
        return buttonField;
    }

    @Override
    public void loadFromFormField(FormField field) {
        // Nothing to do here, as this property stores no data
    }
}
