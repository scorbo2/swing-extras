package ca.corbett.forms;

import ca.corbett.forms.fields.FormField;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * FormPanel wraps a collection of FormFields and manages the following functions:
 * <ul>
 *     <li><b>Layout management</b> - you don't need to write manual GridBagLayout code
 *         to use a FormPanel. Just add your FormFields to the panel and the layout will
 *         be handled automatically.
 *     <li><b>Form validation</b> - assuming you've added FieldValidator instances to
 *         your FormFields as needed, you don't need to write much manual validation code,
 *         and you don't need to write any UI code to show validation results. Just
 *         call formField.validate() and the validators will be invoked automatically.
 * </ul>
 *
 * <p>Handling oversized forms</p><br>
 * You can easily add a FormPanel to a JScrollPane to provide for scrollbars in the
 * case where the form is unreasonably large.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-24
 */
public final class FormPanel extends JPanel {

    public static final int LEFT_SPACER_COLUMN = 0;
    public static final int LABEL_COLUMN = 1;
    public static final int FORM_FIELD_START_COLUMN = LABEL_COLUMN;
    public static final int CONTROL_COLUMN = 2;
    public static final int HELP_COLUMN = 3;
    public static final int VALIDATION_COLUMN = 4;
    public static final int RIGHT_SPACER_COLUMN = 5;

    private final List<FormField> formFields = new ArrayList<>();
    private Alignment alignment;
    private int borderMargin = 0;
    private boolean renderInProgress = false;

    /**
     * Creates a new, empty FormPanel with TOP_CENTER Alignment.
     */
    public FormPanel() {
        this(Alignment.TOP_CENTER);
    }

    /**
     * Creates a new, empty FormPanel with the given Alignment.
     */
    public FormPanel(Alignment alignment) {
        this.alignment = alignment;
    }

    /**
     * An optional pixel margin that will be applied to FormFields as necessary to keep them
     * away from whichever form border is directly adjacent. For example, for left-aligned
     * forms, this margin will be applied to the left edge of all FormFields. For top-aligned
     * forms, this margin will be applied to the topmost FormField to keep it away from the
     * top border. For bottom-right aligned forms, this margin will be applied to the right
     * side of each FormField to keep them from the right border, and also to the bottom
     * FormField to keep it away from the bottom border. If the form is centered both horizontally
     * and vertically, then it touches no border, and so this value will be ignored.
     * <p>
     * The default value is zero, meaning no extra margins will be applied.
     * </p>
     * <p>
     * Note that any value given here is added to whatever margins are already present on
     * each FormField. This value does not replace those values. So, you can still indent
     * a FormField from the others by setting its left margin, for example.
     * </p>
     *
     * @param margin The margin, in pixels, to apply to FormFields as needed. Negative values are treated as 0.
     */
    public FormPanel setBorderMargin(int margin) {
        borderMargin = Math.max(0, margin); // Reject negative values
        return this;
    }

    /**
     * Returns the optional border margin to be applied as described in setBorderMargin.
     */
    public int getBorderMargin() {
        return borderMargin;
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
     * No validation of the FormField identifier is done in this class! If more than
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
     * Removes all FormFields from this FormPanel and re-renders it.
     */
    public void removeAllFormFields() {
        formFields.clear();
        render();
    }

    /**
     * Overridden here so we can also remove all FormField instances.
     * This is effectively the same as calling removeAllFormFields() and
     * it will trigger a re-render.
     */
    @Override
    public void removeAll() {
        super.removeAll();
        if (!renderInProgress) {
            removeAllFormFields(); // don't invoke if this call came from our own render() method, else infinite loop
        }
    }

    /**
     * Adds the specified list of FormFields to this FormPanel.
     */
    public void add(List<FormField> fields) {
        this.formFields.addAll(fields);
        render();
    }

    /**
     * Adds the specified FormField to this FormPanel.
     */
    public void add(FormField field) {
        this.formFields.add(field);
        render();
    }

    /**
     * Returns the number of FormFields contained in this panel.
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
     * Validates each FormField by invoking all FieldValidators that are attached to it,
     * then returns the overall result. The return will be true if all FieldValidators for
     * all FormFields validated successfully.
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
     * Changes the Alignment property of this FormPanel and re-renders it.
     */
    public FormPanel setAlignment(Alignment alignment) {
        this.alignment = alignment;
        render();

        // swing wonkiness... changing layouts requires rejiggering the container:
        final Component component = this;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                component.invalidate();
                component.revalidate();
                component.repaint();
            }
        });

        return this;
    }

    /**
     * Returns the Alignment property of this FormPanel.
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Invoked internally as needed to remove all UI components and re-do the form layout.
     */
    private void render() {
        renderInProgress = true;
        this.removeAll();
        renderInProgress = false;
        this.setLayout(new GridBagLayout());

        if (formFields.isEmpty()) {
            return;
        }

        addHeaderMargin();
        int row = 1; // starting on row 1, after header margin row

        for (int fieldIndex = 0; fieldIndex < formFields.size(); fieldIndex++, row++) {
            FormField field = formFields.get(fieldIndex);
            field.preRender(this);
            Margins fieldMargins = calculateFieldMargins(fieldIndex);

            addLeftMargin(row);
            renderFieldLabel(field, row, fieldMargins);
            renderFieldComponent(field, row, fieldMargins);
            renderHelpLabel(field, row, fieldMargins);
            renderValidationLabel(field, row, fieldMargins);
            addRightMargin(row);
        }

        addFooterMargin(row);
    }

    /**
     * Invoked internally to render the field label for the given FormField, if it has one.
     */
    private void renderFieldLabel(FormField field, int row, Margins margins) {
        if (!field.hasFieldLabel()) {
            return;
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = FORM_FIELD_START_COLUMN;
        constraints.gridy = row;
        constraints.anchor = field.isMultiLine() ? GridBagConstraints.FIRST_LINE_START : GridBagConstraints.WEST;
        int extraTopMargin = field.isMultiLine() ? 4 : 0; // TODO don't hard-code this value
        constraints.insets = new Insets(margins.getTop() + extraTopMargin,
                                        margins.getLeft(),
                                        margins.getBottom(),
                                        margins.getInternalSpacing());
        add(field.getFieldLabel(), constraints);
    }

    /**
     * Invoked internally to render the field component for the given FormField, if it has one.
     */
    private void renderFieldComponent(FormField field, int row, Margins margins) {
        if (field.getFieldComponent() == null) {
            return;
        }

        // If a field label exists, then left margin has already been set. Otherwise, set it now.
        int leftMargin = field.hasFieldLabel() ? margins.getInternalSpacing() : margins.getLeft();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(margins.getTop(),
                                        leftMargin,
                                        margins.getBottom(),
                                        margins.getInternalSpacing());
        constraints.gridx = CONTROL_COLUMN;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        if (field.shouldExpand()) {
            constraints.fill = GridBagConstraints.BOTH;
        }

        // If there is no field label, then this control will occupy both the field
        // label column and the control column:
        if (!field.hasFieldLabel()) {
            constraints.gridx = FORM_FIELD_START_COLUMN;
            constraints.gridwidth = 2;
        }
        add(field.getFieldComponent(), constraints);
    }

    /**
     * Invoked internally to render the help label for the given FormField, if it has one.
     */
    private void renderHelpLabel(FormField field, int row, Margins margins) {
        if (!field.hasHelpLabel()) {
            return;
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = HELP_COLUMN;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.insets = new Insets(margins.getTop(), margins.getInternalSpacing(), margins.getBottom(),
                                        margins.getInternalSpacing());
        add(field.getHelpLabel(), constraints);
    }

    /**
     * Invoked internally to render the validation label for the given FormField, if it has one.
     */
    private void renderValidationLabel(FormField field, int row, Margins margins) {
        // We need to render it unconditionally because it might have a right margin that we need to honor
        //if (!field.hasValidationLabel()) {
        //    return;
        //}

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = VALIDATION_COLUMN;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.insets = new Insets(margins.getTop(), margins.getInternalSpacing(), margins.getBottom(),
                                        margins.getRight());
        add(field.getValidationLabel(), constraints);
    }

    /**
     * Adds a header margin to properly vertically align the form.
     */
    private void addHeaderMargin() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = 6;
        constraints.fill = GridBagConstraints.BOTH;
        if (alignment.isTopAligned()) {
            constraints.weighty = 0.0; // addFooterMargin will force the form to the top
        }
        else if (alignment.isCenteredVertically()) {
            constraints.weighty = 0.5; // Center the form
        }
        else if (alignment.isBottomAligned()) {
            constraints.weighty = 1; // Force the form to the bottom of the panel
        }
        this.add(new JLabel(), constraints);
    }

    /**
     * Adds a left margin to the given grid row to align the form field horizontally on that row.
     */
    private void addLeftMargin(int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = LEFT_SPACER_COLUMN;
        constraints.gridy = row;
        constraints.fill = GridBagConstraints.BOTH;
        if (alignment.isLeftAligned()) {
            constraints.weightx = 0.0; // addRightMargin will force the form to the left
        }
        else if (alignment.isCenteredHorizontally()) {
            constraints.weightx = 0.5; // center the form
        }
        else if (alignment.isRightAligned()) {
            constraints.weightx = 1.0; // Force the form to the right
        }
        add(new JLabel(), constraints);
    }

    /**
     * Adds a right margin to the given grid row to align the form field horizontally on that row.
     */
    private void addRightMargin(int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = RIGHT_SPACER_COLUMN;
        constraints.gridy = row;
        constraints.fill = GridBagConstraints.BOTH;
        if (alignment.isLeftAligned()) {
            constraints.weightx = 1.0; // Force the form to the left
        }
        else if (alignment.isCenteredHorizontally()) {
            constraints.weightx = 0.5; // center the form
        }
        else if (alignment.isRightAligned()) {
            constraints.weightx = 0.0; // addLeftMargin will force the form to the right
        }
        add(new JLabel(), constraints);
    }

    /**
     * Adds a footer margin to properly vertically align the form.
     */
    private void addFooterMargin(int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = 6;
        constraints.gridy = row;
        constraints.fill = GridBagConstraints.BOTH;
        if (alignment.isTopAligned()) {
            constraints.weighty = 1.0; // Force the form to the top
        }
        else if (alignment.isCenteredVertically()) {
            constraints.weighty = 0.5; // Center the form
        }
        else if (alignment.isBottomAligned()) {
            constraints.weighty = 0.0; // addHeaderMargin will force the form to the bottom
        }
        this.add(new JLabel(), constraints);
    }

    /**
     * Applies our borderMargin to the FormField at the given fieldIndex based on its
     * position without our form field list and based on our current alignment.
     * For example, if we are left-aligned, all FormFields need to have borderMargin
     * added to their left margin. If the form is top-aligned, the first FormField in
     * the list needs to have borderMargin added to its top margin, and so on.
     * The FormField's margins are not modified as a result of this calculation.
     * Instead, a new Margins object will be created and returned.
     */
    private Margins calculateFieldMargins(int fieldIndex) {
        FormField field = formFields.get(fieldIndex);
        Margins margins = new Margins(field.getMargins());

        // If there is no border margin to add, we are done here:
        if (borderMargin == 0) {
            return margins;
        }

        boolean isFirstField = (fieldIndex == 0);
        boolean isLastField = (fieldIndex == formFields.size() - 1);

        if (alignment.isTopAligned() && isFirstField) {
            margins.setTop(margins.getTop() + borderMargin);
        }

        if (alignment.isLeftAligned()) {
            margins.setLeft(margins.getLeft() + borderMargin);
        }

        if (alignment.isRightAligned()) {
            margins.setRight(margins.getRight() + borderMargin);
        }

        if (alignment.isBottomAligned() && isLastField) {
            margins.setBottom(margins.getBottom() + borderMargin);
        }

        return margins;
    }

}
