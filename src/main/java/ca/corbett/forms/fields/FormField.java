package ca.corbett.forms.fields;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.Margins;
import ca.corbett.forms.Resources;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * FormField is an abstract base class which allows for the creation of a form field that
 * wraps one or more Java Swing UI components. Most FormFields ultimately wrap a single
 * Java Swing UI component - for example, TextField wraps a JTextComponent, CheckBoxField
 * wraps a JCheckBox, and so on. However, it's possible to wrap multiple UI components into
 * a single FormField implementation - for example, FileField wraps a text box for displaying
 * the currently selected file or directory, and also a JButton for launching a file chooser dialog.
 * <p>
 * FormField is designed with extensibility in mind! If you need a specific type of form field
 * that isn't represented by any of the provided implementation classes, you can build your
 * own FormField by extending this class and wrapping whatever UI components you need.
 * Alternatively, you can use PanelField, which wraps an empty JPanel that you can populate
 * with whatever UI components you need.
 *
 * <h2>Validating a FormField</h2>
 * You can use addFieldValidator() to add any number of FieldValidator instances to the
 * FormField. When the containing FormPanel is validated, all the validators on the FormField
 * will be executed, and the results will automatically be displayed in a validation label
 * next to the FormField. Helpful tooltip text will be provided so that the user understands
 * why the field failed to validate.
 *
 * <h2>Responding to field value changes</h2>
 * Often it's handy to be able to respond to form field changes as they are made, rather than
 * waiting until the form is validated and submitted. For example, you might want to control
 * the visibility of field B depending on what value is contained in field A. You can do this
 * by using addValueChangedListener() and responding to the change events as they happen.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-23
 */
public abstract class FormField {

    protected static final Font DEFAULT_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
    protected static Font defaultFont = DEFAULT_FONT;

    protected final List<ValueChangedListener> valueChangedListeners = new ArrayList<>();
    protected final List<FieldValidator<? extends FormField>> fieldValidators = new ArrayList<>();

    protected String identifier;

    protected final JLabel fieldLabel = new JLabel();
    protected JComponent fieldComponent;
    protected final JLabel validationLabel = new JLabel();
    protected final JLabel helpLabel = new JLabel();

    protected final Margins margins = new Margins();

    protected boolean isVisible = true;
    protected boolean isEnabled = true;

    protected final Map<String, Object> extraAttributes = new HashMap<>();

    public FormField() {
        fieldLabel.setFont(defaultFont);
        fieldLabel.setForeground(LookAndFeelManager.getLafColor("Label.foreground", Color.BLACK));
        helpLabel.setIcon(Resources.getHelpIcon());
        validationLabel.setIcon(Resources.getBlankIcon()); // placeholder until validation occurs
    }

    /**
     * By default, all FormFields will show a validation label when validation results
     * are available. Some descendant classes might override this method to return
     * false if a validation label is not applicable (for example, for a LabelField).
     * <p>
     * It is <b>strongly recommended</b> that even if a FormField implementing class does not want
     * to show the validation label, it should still return true here if one or
     * more FieldValidator instances have been added to this field. Otherwise,
     * validation errors on the field won't be visible to the user.
     * Suggested implementation for such cases:
     * </p>
     * <blockquote><pre>return !fieldValidators.isEmpty();</pre></blockquote>
     */
    public boolean hasValidationLabel() {
        return true;
    }

    /**
     * By default, FormFields occupy a single "line", or row, on the form. However,
     * some FormFields may have a field component that spans multiple lines,
     * like a multi-line text box, or a list, or a custom panel. Descendant
     * classes can override the default false value here. It controls the placement
     * of the field label. For tall form fields, the field label will be anchored
     * to the top-left of its area.
     */
    public boolean isMultiLine() {
        return false;
    }

    /**
     * By default, FormPanel will allocate only the space that the field component
     * requires. Descendant classes can override the default false value here to
     * indicate that their field component should be allowed to expand as much space
     * as is available to it. For example: PanelField.
     */
    public boolean shouldExpand() {
        return false;
    }

    /**
     * Any FormField that has help text set will return true here to indicate that
     * a help label is available. If help text is unset, will return false;
     */
    public boolean hasHelpLabel() {
        return helpLabel.getToolTipText() != null && !helpLabel.getToolTipText().isBlank();
    }

    /**
     * A FormField can have an associated field label which describes the field.
     * To enable this, simply set the fieldLabel's text to a non-blank value.
     * Setting the fieldLabel text to blank or empty will cause this method to
     * return false, indicating the field does not have or want a fieldLabel.
     * For example, a CheckBox field likely does not require an explicit
     * field label, because the checkbox control itself contains a label.
     */
    public boolean hasFieldLabel() {
        return fieldLabel.getText() != null && !fieldLabel.getText().isBlank();
    }

    public JLabel getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabelFont(Font font) {
        fieldLabel.setFont(font);
    }

    /**
     * Returns the prototype Font that will be used in all new FormField constructors.
     * There is a built-in default value which can be overridden via setDefaultFont().
     */
    public static Font getDefaultFont() {
        return defaultFont;
    }

    /**
     * Sets the default Font to use with all FormField instances created after this call completes.
     * This does not affect any FormField instance that was instantiated before this call.
     * Passing null will revert this property to FormField.DEFAULT_FONT.
     */
    public static void setDefaultFont(Font newFont) {
        defaultFont = newFont == null ? DEFAULT_FONT : newFont;
    }

    /**
     * Adds a FieldValidator to this FormField.
     */
    public FormField addFieldValidator(FieldValidator<? extends FormField> validator) {
        fieldValidators.add(validator);
        return this;
    }

    /**
     * Removes the given FieldValidator from this FormField.
     */
    public void removeFieldValidator(FieldValidator<FormField> validator) {
        fieldValidators.remove(validator);
    }

    /**
     * Remove all validators from this FormField.
     */
    public void removeAllFieldValidators() {
        fieldValidators.clear();
    }

    /**
     * Adds a value changed listener that will be invoked when the field value is changed.
     */
    public FormField addValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.add(listener);
        return this;
    }

    /**
     * Removes the given value changed listener from this FormField.
     */
    public void removeValueChangedListener(ValueChangedListener listener) {
        valueChangedListeners.remove(listener);
    }

    /**
     * Returns the validation label for this FormField.
     * This is needed by FormPanel in the render() method.
     */
    public JLabel getValidationLabel() {
        return validationLabel;
    }

    /**
     * Returns the help text associated with this field, if any is set.
     */
    public String getHelpText() {
        return helpLabel.getToolTipText();
    }

    /**
     * Sets optional help text for this field. If present, the field may show
     * the helpLabel to allow the user to get help for the field. Note that
     * some fields may decide not to render the helpLabel even if helpText
     * is set for the field.
     */
    public FormField setHelpText(String helpText) {
        helpLabel.setToolTipText((helpText == null) ? "" : helpText);
        return this;
    }

    /**
     * Returns the help label for this FormField.
     * This is needed by FormPanel in the render() method.
     *
     * @return A JLabel.
     */
    public JLabel getHelpLabel() {
        return helpLabel;
    }

    public FormField setMargins(Margins margins) {
        this.margins.copy(margins);
        return this;
    }

    public Margins getMargins() {
        return margins;
    }

    /**
     * Sets the visibility status of all components of this field.
     *
     * @param visible Whether to show or hide.
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
        fieldLabel.setVisible(visible);
        if (fieldComponent != null) {
            fieldComponent.setVisible(visible);
        }
        validationLabel.setVisible(visible);
        helpLabel.setVisible(visible);
    }

    /**
     * Reports whether this FormField is visible.
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Enables or disables all components in this field.
     *
     * @param enabled whether to enable or disable the components.
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        fieldLabel.setEnabled(enabled);
        if (fieldComponent != null) {
            fieldComponent.setEnabled(enabled);
        }
        validationLabel.setEnabled(enabled);
        helpLabel.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Sets an internal String identifier for this field. Never shown to the user.
     *
     * @param id Any String which hopefully uniquely identifies this field. No validity checks.
     */
    public FormField setIdentifier(String id) {
        this.identifier = id;
        return this;
    }

    /**
     * Returns the internal String identifier for this field, if one is set.
     *
     * @return A String identifier for this field, or null if not set.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the actual JComponent that is wrapped by this field.
     *
     * @return The JComponent that this field wraps.
     */
    public JComponent getFieldComponent() {
        if (fieldComponent != null) {
            fieldComponent.setVisible(isVisible);
            fieldComponent.setEnabled(isEnabled);
        }
        return fieldComponent;
    }

    /**
     * Set an arbitrary extra attribute to this form field. The given value is not validated
     * nor used within this class. It's just extra data that can be attached by the caller.
     *
     * @param name  The unique name of the value to set. Will overwrite any previous value by that name.
     * @param value The value to set.
     */
    public FormField setExtraAttribute(String name, Object value) {
        extraAttributes.put(name, value);
        return this;
    }

    /**
     * Returns a named extra attribute's value, if it exists.
     *
     * @param name The unique name of the value in question.
     * @return The value associated with that name, or null if no such value.
     */
    public Object getExtraAttribute(String name) {
        return extraAttributes.get(name);
    }

    /**
     * Removes all extra attributes and their associated values from this FormField.
     */
    public void clearExtraAttributes() {
        extraAttributes.clear();
    }

    /**
     * Removes the value for the named extra attribute.
     *
     * @param name The unique name of the attribute in question.
     */
    public void clearExtraAttribute(String name) {
        extraAttributes.remove(name);
    }

    /**
     * Clears any extra attributes currently held by this form field and then accepts
     * the given list of attributes.
     *
     * @param newAttributes A map of String name to some arbitrary Object value.
     */
    public FormField setAllExtraAttributes(Map<String, Object> newAttributes) {
        clearExtraAttributes();
        extraAttributes.putAll(newAttributes);
        return this;
    }

    /**
     * Adds the map of extra attributes to our existing list. Any name conflicts will result
     * in the existing values being overwritten by the new values.
     *
     * @param newAttributes A map of String name to some arbitrary Object value.
     */
    public FormField addAllExtraAttributes(Map<String, Object> newAttributes) {
        extraAttributes.putAll(newAttributes);
        return this;
    }

    /**
     * Invoke this to clear the validation label off any previously validated field.
     * Useful for when resetting a form to its initial state.
     */
    public void clearValidationResults() {
        validationLabel.setIcon(null);
        validationLabel.setToolTipText(null);
    }

    /**
     * Asks all registered FieldValidators (if any) to check the current value
     * of this field to make sure it's valid. If no FieldValidators are registered, then
     * the field is valid by default (i.e. no checking is done). If any validator returns
     * false, then this method will return false.
     * <p>
     * <b>Updating the UI</b>: this method will make the validation label to the right of the
     * FormField visible automatically and will set its icon as appropriate. Tooltip text
     * will be available in the case of a failed validation, to explain why the field is invalid.
     * </p>
     *
     * @return True if the field value is valid according to all our validators, false otherwise.
     */
    public boolean validate() {
        boolean isValid = true;

        // If the field is not currently enabled, don't bother validating:
        if (!isEnabled) {
            return isValid;
        }

        List<String> validationMessages = new ArrayList<>();
        for (FieldValidator<? extends FormField> validator : fieldValidators) {
            //noinspection unchecked
            FieldValidator<FormField> theValidator = (FieldValidator<FormField>)validator;
            ValidationResult validationResult = theValidator.validate(this);
            isValid = isValid && validationResult.isValid();
            if (!validationResult.isValid()) {
                validationMessages.add(validationResult.getMessage());
            }
        }

        if (!isValid) {
            StringBuilder message = new StringBuilder();
            for (String msg : validationMessages) {
                message.append(msg);
                message.append("\n");
            }
            String toolTip = message.substring(0, message.length() - 1);
            validationLabel.setIcon(Resources.getInvalidIcon());
            validationLabel.setToolTipText(toolTip);
        }
        else {
            if (hasValidationLabel()) { // don't set the icon if this field doesn't show validation results
                validationLabel.setIcon(Resources.getValidIcon());
                validationLabel.setToolTipText(null);
            }
        }

        return isValid;
    }

    /**
     * Shorthand for validate()
     *
     * @return see validate()
     */
    public boolean isValid() {
        return validate();
    }

    /**
     * Invoke before rendering this FormField to a container, in case the FormField needs
     * to do some initialization specific to its new container (for example, matching
     * the container's background color or using the container as a parent component for a popup dialog).
     * The default implementation here does nothing. Overriding this method is optional.
     */
    public void preRender(JPanel container) {
    }

    /**
     * Invoked internally to notify all registered actions about a change
     * in the value of this field. If you are extending FormField to create your own
     * implementation, you can invoke this to easily notify all listeners that the
     * value in your field has changed.
     */
    protected void fireValueChangedEvent() {
        for (ValueChangedListener listener : valueChangedListeners) {
            listener.formFieldValueChanged(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FormField)) {
            return false;
        }

        FormField formField = (FormField)o;
        return Objects.equals(margins, formField.getMargins())
                && isVisible == formField.isVisible
                && isEnabled == formField.isEnabled
                && Objects.equals(identifier, formField.identifier)
                && Objects.equals(fieldLabel, formField.fieldLabel)
                && Objects.equals(fieldComponent, formField.fieldComponent)
                && Objects.equals(validationLabel, formField.validationLabel)
                && Objects.equals(helpLabel, formField.helpLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, margins, isVisible, isEnabled, fieldLabel, fieldComponent);
    }
}
