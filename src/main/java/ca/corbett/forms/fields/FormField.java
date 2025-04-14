package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for a form field. These form fields are intended to wrap common Swing
 * components such as JTextField or JComboBox or whatever, but with the added advantage
 * (working with FormPanel) of being able to very easily generate all the GridBagLayout code
 * required to get them onto a form panel without writing a tonne of code, and also to handle
 * form validation through FieldValidator implementations in a standardized way.
 *
 * @author scorbo2
 * @since 2019-11-23
 */
public abstract class FormField {

    /**
     * A reference to a checkmark icon for showing next to validated form fields. *
     */
    protected static final URL validImageUrl = FormField.class.getResource(
            "/ca/corbett/swing-forms/images/formfield-valid.png");

    /**
     * A reference to an X icon for showing next to invalid form fields. *
     */
    protected static final URL invalidImageUrl = FormField.class.getResource(
            "/ca/corbett/swing-forms/images/formfield-invalid.png");

    /**
     * You can specify an Action(s) that will be invoked when the field value is modified. *
     */
    protected final List<AbstractAction> valueChangedActions = new ArrayList<>();

    /**
     * You can specify a FieldValidator(s) that will check the value of this field. *
     */
    protected final List<FieldValidator<FormField>> validators = new ArrayList<>();

    /**
     * An internal name or id for this field; never shown to the user. *
     */
    protected String identifier;

    /**
     * A JLabel component to show a label for this field. *
     */
    protected JLabel fieldLabel;

    /**
     * The Font to use for the field label. *
     */
    protected Font fieldLabelFont = new Font("SansSerif", Font.PLAIN, 12);

    /**
     * The actual JComponent that we wrap. *
     */
    protected JComponent fieldComponent;

    /**
     * A JLabel component to show the validation results for this field. *
     */
    protected final JLabel validationLabel = new JLabel();

    /**
     * Most of the time, the FormPanel itself can deal with the rendering of
     * the validation and help labels, and individual FormField implementations
     * don't need to worry about it. But, some fields (like multi-line text
     * fields for example) want to render the extra labels themselves in
     * a nonstandard way. So, if a FormField implementation sets this flag
     * to true, the FormPanel will leave the rendering of the validation
     * and help labels up to the FormField.
     */
    protected boolean isExtraLabelRenderedByField = false;

    /**
     * A JLabel to show help text for this field.
     */
    protected final JLabel helpLabel = new JLabel();

    /**
     * Optional help text for this field.
     */
    protected String helpText = "";

    /**
     * Margin to apply above the field. *
     */
    protected int topMargin = 4;

    /**
     * Margin to apply below the field. *
     */
    protected int bottomMargin = 4;

    /**
     * Margin to apply to the left of the field. *
     */
    protected int leftMargin = 4;

    /**
     * Margin to apply to the right of the field. *
     */
    protected int rightMargin = 4;

    /**
     * Margin to apply within the field (that is, margin between the label and component and
     * validation label) *
     */
    protected int componentSpacing = 4;

    /**
     * Whether to show or hide the validation label (makes no sense for some components, like labels
     * or checkboxes)
     */
    protected boolean showValidationLabel = true;

    /**
     * Indicates whether or not we're currently visible *
     */
    protected boolean isVisible = true;

    /**
     * Indicates whether or not we're currently enabled *
     */
    protected boolean isEnabled = true;

    /**
     * Adds the given FieldValidator to the list of validators for this field.
     * All validators assigned to a field must return a valid response in order
     * for the field value to be considered valid. All validators are invoked
     * in no particular order when validate() is invoked.
     *
     * @param validator The FieldValidator to add to this field.
     */
    public void addFieldValidator(FieldValidator<FormField> validator) {
        // Some fields disable the validation label as they normally
        // aren't validated (eg. checkboxes). But, if we're adding a
        // field validator, we'll want to override that:
        showValidationLabel = true;

        if (!validators.contains(validator)) {
            validators.add(validator);
        }
    }

    /**
     * You can remove a particular FieldValidator from this field, if present.
     *
     * @param validator The FieldValidator to remove.
     */
    public void removeFieldValidator(FieldValidator<FormField> validator) {
        validators.remove(validator);
    }

    /**
     * Remove all validators from this FormField.
     */
    public void removeAllFieldValidators() {
        validators.clear();
    }

    /**
     * Adds an Action that will be invoked when the field value is changed.
     * You can use this to update the value of some other form field, or some other
     * component outside of this form.
     *
     * @param action An Action to be invoked when this field's value changes.
     */
    public void addValueChangedAction(AbstractAction action) {
        valueChangedActions.add(action);
    }

    /**
     * You can remove a particular Action from this field if it is no longer needed.
     *
     * @param action The Action to remove. Will no longer receive updates from this field.
     */
    public void removeValueChangedAction(AbstractAction action) {
        valueChangedActions.remove(action);
    }

    /**
     * Controls whether the validation label will be shown or not when the form field is validated.
     * Some controls may wish to turn this off as it may make no sense (eg. labels or checkboxes).
     * The label will only be shown if a validation error is present for that field.
     *
     * @param show Whether to show or hide the validation label.
     */
    public void setShowValidationLabel(boolean show) {
        showValidationLabel = show;
    }

    /**
     * Reports whether the validation label will be shown or not when the form field is validated.
     * Some controls may wish to turn this off as it may make no sense (eg. labels or checkboxes).
     * The label will only be shown if a validation error is present for that field.
     *
     * @return Whether to show or hide the validation label.
     */
    public boolean getShowValidationLabel() {
        return showValidationLabel;
    }

    /**
     * Most of the time, the FormPanel itself can deal with the rendering of
     * the validation and help labels, and individual FormField implementations
     * don't need to worry about it. But, some fields (like multi-line text
     * fields for example) want to render the extra labels themselves in
     * a nonstandard way. So, if a FormField implementation sets this flag
     * to true, the FormPanel will leave the rendering of the validation
     * and help labels up to the FormField.
     */
    public boolean isExtraLabelRenderedByField() {
        return isExtraLabelRenderedByField;
    }

    /**
     * Returns the validation label for this FormField.
     * This is needed by FormPanel in the render() method.
     *
     * @return A JLabel.
     */
    public JLabel getValidationLabel() {
        return validationLabel;
    }

    /**
     * Returns the help text associated with this field, if any is set.
     *
     * @return Help text for this field, or an empty string if no help text is set.
     */
    public String getHelpText() {
        return helpText;
    }

    /**
     * Sets optional help text for this field. If present, the field may show
     * the helpLabel to allow the user to get help for the field. Note that
     * some fields may decide not to render the helpLabel even if helpText
     * is set for the field. It's up to each FormField implementation.
     *
     * @param helpText The help text to show, or null for no help text.
     */
    public void setHelpText(String helpText) {
        this.helpText = (helpText == null) ? "" : helpText;
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

    /**
     * Returns the Font used for the field label.
     *
     * @return The Font used for the field label.
     */
    public Font getFieldLabelFont() {
        return fieldLabelFont;
    }

    /**
     * Set the label text to appear beside the form field.
     *
     * @param newText The label text.
     */
    public void setFieldLabelText(String newText) {
        if (fieldLabel != null) {
            fieldLabel.setText(newText);
        }
    }

    /**
     * Sets the Font to use for the field label. Be sure to invoke this before rendering.
     *
     * @param font The Font to use.
     */
    public void setFieldLabelFont(Font font) {
        fieldLabelFont = font;
    }

    /**
     * Sets the margin to apply around the field and in between components of the field.
     *
     * @param top    Margin to set above all components in this field.
     * @param left   Margin to apply to the left of the leftmost component of this field.
     * @param bottom Margin to set below all components of this field.
     * @param right  Margin to apply to the right of the rightmost component of this field.
     * @param inner  Margin to apply in between components of this field.
     */
    public void setMargins(int top, int left, int bottom, int right, int inner) {
        topMargin = top;
        leftMargin = left;
        bottomMargin = bottom;
        rightMargin = right;
        componentSpacing = inner;
    }

    public void setLeftMargin(int margin) {
        leftMargin = margin;
    }

    public void setTopMargin(int margin) {
        topMargin = margin;
    }

    public void setRightMargin(int margin) {
        rightMargin = margin;
    }

    public void setBottomMargin(int margin) {
        bottomMargin = margin;
    }

    public void setComponentSpacing(int spacing) {
        componentSpacing = spacing;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public int getBottomMargin() {
        return bottomMargin;
    }

    public int getComponentSpacing() {
        return componentSpacing;
    }

    /**
     * Sets the visibility status of all components of this field.
     *
     * @param visible Whether to show or hide.
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
        fieldLabel.setVisible(visible);
        fieldComponent.setVisible(visible);
        validationLabel.setVisible(visible);
        helpLabel.setVisible(visible);
    }

    /**
     * Enables or disables all components in this field.
     *
     * @param enabled whether to enable or disable the components.
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        fieldLabel.setEnabled(enabled);
        fieldComponent.setEnabled(enabled);
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
    public void setIdentifier(String id) {
        this.identifier = id;
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
     * Returns the actual JComponent that is wrapped by this field. It is usually
     * better to go through a more specific method in the implementation class.
     *
     * @return The JComponent that this field wraps.
     */
    public JComponent getFieldComponent() {
        return fieldComponent;
    }

    /**
     * Invoke this to render this field into the given containing panel using the given
     * GridBagConstraints object. You are likely better off going through the render()
     * method in FormPanel rather than manually rendering individual FormFields, but you
     * could use this to render a FormField into some other container if you need to.
     * <p>
     * Note for those creating a new FormField implementation: the GridBagConstraints
     * object that you are given here is ready to go. You have two options
     * for gridx: FormPanel.LABEL_COLUMN and FormPanel.CONTROL_COLUMN. You should
     * avoid rendering into any other gridx value. You <b>can</b> increment
     * gridy if you really want a multi-row component, but beware that you
     * will likely have to handle the rendering of the "extra" labels (validation
     * and help) yourself. See the multi-line TextField implementation for
     * a reference.
     * </p>
     *
     * @param container   The containing form panel.
     * @param constraints The GridBagConstraints object to use.
     */
    public abstract void render(JPanel container, GridBagConstraints constraints);

    /**
     * Invoke this to clear the validation label off any previously validated field.
     * Useful for when resetting a form to its initial state.
     */
    public void clearValidationResults() {
        validationLabel.setIcon(null);
        validationLabel.setToolTipText(null);
    }

    /**
     * Invoke this to ask all registered FieldValidators (if any) to check the current value
     * of this field to make sure it's valid. If no FieldValidators are registered, then
     * the field is valid by default (i.e. no checking is done).
     *
     * @return True if the field value is valid according to our validators, false otherwise.
     */
    public boolean validate() {
        boolean isValid = true;

        // If the field is not currently enabled, don't bother validating:
        if (!isEnabled) {
            return isValid;
        }

        List<String> validationMessages = new ArrayList<>();
        for (FieldValidator<FormField> validator : validators) {
            ValidationResult validationResult = validator.validate();
            isValid = isValid && validationResult.isValid();
            if (!validationResult.isValid()) {
                validationMessages.add(validationResult.getMessage());
            }
        }

        if (!isValid) {
            StringBuilder message = new StringBuilder();
            for (String msg : validationMessages) {
                message.append(msg);
                message.append(" \n ");
            }
            String toolTip = message.substring(0, message.length() - 1);
            if (invalidImageUrl != null) {
                validationLabel.setIcon(new ImageIcon(invalidImageUrl));
                validationLabel.setToolTipText(toolTip);
            }
        }
        else if (showValidationLabel) { // skip if the control normally doesn't show validation labels
            if (validImageUrl != null) {
                validationLabel.setIcon(new ImageIcon(validImageUrl));
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
     * Invoked internally to notify all registered actions about a change
     * in the value of this field.
     */
    protected void fireValueChangedEvent() {
        for (AbstractAction action : valueChangedActions) {
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "valueChanged"));
        }
    }
}
