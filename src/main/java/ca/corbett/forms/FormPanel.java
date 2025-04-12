package ca.corbett.forms;

import ca.corbett.forms.fields.FormField;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This FormPanel wraps a collection of FormField instances and provides an
 * easy mechanism for rendering and form validation.
 * <p>
 * You can either create a new, empty FormPanel and then use addFormField to add
 * fields to it, or you can supply a List of FormFields to the constructor.
 * Either way, <b>you must invoke render() before showing the FormPanel</b>.
 * </p>
 * <p>
 * Forms have an Alignment property which defaults to TOP_CENTER. This means
 * that the form will stick to the top of its container and will try to
 * horizontally center all contents. You can adjust this alignment at
 * any time, though you will have to re-render the FormPanel if you change
 * this property after the FormPanel has already been rendered.
 * </p>
 *
 * @author scorbo2
 * @since 2019-11-24
 */
public final class FormPanel extends JPanel {

    public enum Alignment {
        TOP_CENTER,
        TOP_LEFT,
        CENTER_LEFT,
        CENTER;

        public boolean isLeftAligned() {
            return this == TOP_LEFT || this == CENTER_LEFT;
        }

        public boolean isTopAligned() {
            return this == TOP_LEFT || this == TOP_CENTER;
        }

        public boolean isCentered() {
            return this == CENTER_LEFT || this == CENTER;
        }
    }

    public static final URL helpImageUrl = FormPanel.class.getResource(
            "/ca/corbett/swing-forms/images/formfield-help.png");

    public static final int LEFT_SPACER_COLUMN = 0;
    public static final int LABEL_COLUMN = 1;
    public static final int FORM_FIELD_START_COLUMN = LABEL_COLUMN;
    public static final int CONTROL_COLUMN = 2;
    public static final int HELP_COLUMN = 3;
    public static final int VALIDATION_COLUMN = 4;
    public static final int RIGHT_SPACER_COLUMN = 5;

    private final List<FormField> formFields;
    private Alignment alignment;
    private int standardLeftMargin = 0;

    /**
     * Creates a new, blank FormPanel that will default to the TOP_CENTER Alignment.
     */
    public FormPanel() {
        this(new ArrayList<FormField>(), Alignment.TOP_CENTER);
    }

    /**
     * Creates a new, blank FormPanel with the given Alignment.
     *
     * @param alignment Describes how FormFields should be laid out on this FormPanel.
     */
    public FormPanel(Alignment alignment) {
        this(new ArrayList<FormField>(), alignment);
    }

    /**
     * Creates a new FormPanel with the given FormFields and a default TOP_CENTER Alignment.
     *
     * @param formFields A list of FormFields to add to this form.
     */
    public FormPanel(List<FormField> formFields) {
        this(formFields, Alignment.TOP_CENTER);
    }

    /**
     * Creates a new FormPanel with the given FormFields and Alignment parameters.
     *
     * @param formFields A list of FormFields to add to this form.
     * @param alignment  Describes how FormFields should be laid out on this FormPanel.
     */
    public FormPanel(List<FormField> formFields, Alignment alignment) {
        this.formFields = new ArrayList<>();
        this.formFields.addAll(formFields);
        this.alignment = alignment;
    }

    /**
     * For left-aligned forms, you can apply a standard margin to keep the form fields a bit
     * away from the left edge of the FormPanel. Does nothing for center-aligned forms.
     * This must be invoked before render()!
     *
     * @param margin The margin, in pixels, to apply to the left of all form fields.
     */
    public void setStandardLeftMargin(int margin) {
        standardLeftMargin = margin;
    }

    /**
     * Returns the left margin to apply to all form fields if the current alignment is
     * left-aligned.
     *
     * @return A left margin value in pixels.
     */
    public int getStandardLeftMargin() {
        return standardLeftMargin;
    }

    /**
     * Returns a copy of the list of FormFields contained in this panel.
     * A copy of the list is returned to avoid client modification of the list itself.
     *
     * @return A copy of the list of form fields for this form panel.
     */
    public List<FormField> getFormFields() {
        return new ArrayList<>(formFields);
    }

    /**
     * Finds and returns a specific FormField by its identifier, if it exists.
     * No validation of FormField.identifier is done in this class! If more than
     * one FormField has the same identifier, this method will return whichever
     * one it finds first. If a field does not have an identifier, it will not
     * be considered by this method.
     *
     * @param identifier The field identifier to search for.
     * @return A FormField matching that identifier, or null if not found.
     */
    public FormField getFormField(String identifier) {
        for (FormField candidate : formFields) {
            if (candidate.getIdentifier() != null && candidate.getIdentifier().equals(identifier)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Removes all FormField instances from this FormPanel and re-renders it.
     */
    public void removeAllFormFields() {
        formFields.clear();
        render();
    }

    /**
     * Adds the specified list of FormFields to this FormPanel.
     * The render() method must be invoked manually after this call to see the result.
     *
     * @param fields The FormFields to be added to this FormPanel.
     */
    public void addFormFields(List<FormField> fields) {
        this.formFields.addAll(fields);
        if (alignment.isLeftAligned() && standardLeftMargin > 0) {
            for (FormField field : fields) {
                field.setLeftMargin(field.getLeftMargin() + standardLeftMargin);
            }
        }
    }

    /**
     * Adds the specified FormField to this FormPanel.
     * The render() method must be invoked manually after this call to see the result.
     *
     * @param field The FormField to be added to this FormPanel.
     */
    public void addFormField(FormField field) {
        this.formFields.add(field);
        if (alignment.isLeftAligned() && standardLeftMargin > 0) {
            field.setLeftMargin(field.getLeftMargin() + standardLeftMargin);
        }
    }

    /**
     * Returns the number of FormFields contained in this panel.
     *
     * @return A count of FormFields contained here.
     */
    public int getFieldCount() {
        return formFields.size();
    }

    /**
     * Invoke this to clear the validation label off any previously validated field.
     * Useful for when resetting a form to its initial state.
     */
    public void clearValidationResults() {
        for (FormField field : formFields) {
            field.clearValidationResults();
        }
    }

    /**
     * Reports whether this form panel is in a valid state or not.
     *
     * @return Whether all fields in this panel are valid.
     */
    public boolean isFormValid() {
        boolean isValid = true;
        for (FormField field : formFields) {
            isValid = field.validate() && isValid;
        }
        return isValid;
    }

    /**
     * Shorthand for isFormValid()
     */
    public void validateForm() {
        isFormValid();
    }

    /**
     * Changes the Alignment property of this FormPanel - remember to invoke render() again
     * if this FormPanel has already been rendered!
     */
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    /**
     * Returns the Alignment property of this FormPanel.
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Renders this form panel by rendering each form field one by one.
     * This will clear the panel of any components from any previous render().
     */
    public void render() {
        this.removeAll();
        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 0;

        for (FormField field : formFields) {

            // Move the field to the center if we're not left-aligned
            if (!alignment.isLeftAligned()) {
                JLabel spacer = new JLabel("");
                constraints.fill = GridBagConstraints.BOTH;
                constraints.weightx = 0.5;
                constraints.gridx = LEFT_SPACER_COLUMN;
                add(spacer, constraints);
                constraints.fill = GridBagConstraints.NONE;
                constraints.weightx = 0.0;
            }

            // Tell the FormField to render itself starting in the FORM_FIELD_START_COLUMN:
            constraints.gridx = FORM_FIELD_START_COLUMN;
            field.render(this, constraints);
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.NONE;

            // Render the help label if the form field has help text:
            if (!field.getHelpText().isBlank() && !field.isExtraLabelRenderedByField()) {
                constraints.gridx = HELP_COLUMN;
                constraints.insets = new Insets(field.getTopMargin(), field.getComponentSpacing(),
                                                field.getBottomMargin(), field.getComponentSpacing());
                if (helpImageUrl != null) {
                    field.getHelpLabel().setIcon(new ImageIcon(helpImageUrl));
                }
                field.getHelpLabel().setToolTipText(field.getHelpText());
                add(field.getHelpLabel(), constraints);
            }

            // Render the validation label if the form field wants it:
            if (field.getShowValidationLabel() && !field.isExtraLabelRenderedByField()) {
                constraints.gridx = VALIDATION_COLUMN;
                constraints.insets = new Insets(field.getTopMargin(), field.getComponentSpacing(),
                                                field.getBottomMargin(), field.getRightMargin());
                add(field.getValidationLabel(), constraints);
            }

            JLabel spacer = new JLabel("");
            constraints.gridx = RIGHT_SPACER_COLUMN;
            constraints.weightx = 0.5;
            constraints.fill = GridBagConstraints.BOTH;
            add(spacer, constraints);
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = 0.0;
        }

        // Add a spacer label to take up any remaining space in the GridBagLayout:
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 6;
        constraints.fill = GridBagConstraints.BOTH;
        if (alignment.isTopAligned()) {
            constraints.weighty = 1; // Force the form to the top of the panel
        }
        constraints.weightx = 0;
        JLabel dummy = new JLabel();
        this.add(dummy, constraints);
    }

}
