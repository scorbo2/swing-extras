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
 * FormPanel wraps a collection of FormFields and manages the following
 * functions:
 * <ul>
 * <li><b>Layout management</b> - you don't need to write manual GridBagLayout
 * code
 * to use a FormPanel. Just add your FormFields to the panel and the layout will
 * be handled automatically.
 * <li><b>Form validation</b> - assuming you've added FieldValidator instances
 * to
 * your FormFields as needed, you don't need to write much manual validation
 * code,
 * and you don't need to write any UI code to show validation results. Just
 * call formField.validate() and the validators will be invoked automatically.
 * <li><b>Optional inline help</b> - every FormField can have optional help text
 * which the FormPanel will render inline in the form of an information icon
 * next to the field, which will show tooltip help text.
 * <li><b>Generic FormField handling</b> - the FormField class is highly
 * extensible, so you can very easily provide your own FormField implementation
 * for custom display and editing of data.
 * </ul>
 *
 * <p>
 * Handling oversized forms
 * </p>
 * <br>
 * You can easily add a FormPanel to a JScrollPane to provide for scrollbars in
 * the
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
    private final Margins formPanelMargins = new Margins(0);
    private boolean renderInProgress = false;
    private int multiLineTopMargin = 4;

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
     * An optional pixel margin for the FormPanel itself to keep it away from the
     * edges
     * of its container. The default value is zero, meaning no extra margins will
     * be applied. Note that any value given here is added to whatever margins are
     * already present on
     * each individual FormField. This value does not replace those values. So, you
     * can still indent
     * a FormField from the others by setting its left margin on a left-aligned
     * form, even if the
     * FormPanel itself already specifies a left margin. The two values are added
     * together in that case.
     *
     * @param margin The margin, in pixels, to apply to the FormPanel. Negative
     *               values are treated as 0.
     */
    public FormPanel setBorderMargin(int margin) {
        formPanelMargins.setAll(margin);
        return this;
    }

    /**
     * Sets optional pixel margins for the FormPanel itself to keep it away from the
     * edges
     * of its container. The default value is zero, meaning no extra margins will
     * be applied. Note that any value given here is added to whatever margins are
     * already present on
     * each individual FormField. This value does not replace those values. So, you
     * can still indent
     * a FormField from the others by setting its left margin on a left-aligned
     * form, even if the
     * FormPanel itself already specifies a left margin. The two values are added
     * together in that case.
     * <p>
     * <b>Note:</b> the Margins class defines an internalSpacing property. That
     * property is ignored
     * for FormPanel margin calculations. We only care about top, left, bottom, and
     * right.
     * </p>
     */
    public FormPanel setBorderMargin(Margins margins) {
        formPanelMargins.copy(margins);
        return this;
    }

    /**
     * Returns the optional border margin to be applied as described in
     * setBorderMargin.
     * The actual instance is returned, so callers can modify individual properties.
     */
    public Margins getBorderMargin() {
        return formPanelMargins;
    }

    /**
     * Sets the top margin to be applied to multi-line form fields.
     * The default value is 4 pixels.
     *
     * @param margin The margin in pixels.
     */
    public FormPanel setMultiLineTopMargin(int margin) {
        this.multiLineTopMargin = margin;
        return this;
    }

    /**
     * Returns a copy of the list of FormFields contained in this panel.
     * A copy of the list is returned to avoid client modification of the list
     * itself.
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
            removeAllFormFields(); // don't invoke if this call came from our own render() method, else infinite
                                   // loop
        }
    }

    /**
     * Overridden here so we can enable/disable all FormField instances when
     * we receive a setEnabled request.
     *
     * @param enabled true if this component should be enabled, false otherwise
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (FormField field : formFields) {
            field.setEnabled(enabled);
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
     * Validates each FormField by invoking all FieldValidators that are attached to
     * it,
     * then returns the overall result. The return will be true if all
     * FieldValidators for
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
        forceRerender();

        return this;
    }

    /**
     * Returns the Alignment property of this FormPanel.
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * If it is required to re-render the form panel for some reason, you can do it
     * manually here.
     * Generally, this should not be necessary, as a re-render will happen
     * automatically as fields
     * are added or removed. But, if a field has changed structurally in some way
     * that requires
     * a re-render, this option exists.
     */
    public void forceRerender() {
        render();

        // swing wonkiness... changing layouts requires rejiggering the container:
        final Component component = this;
        SwingUtilities.invokeLater(() -> {
            component.invalidate();
            component.revalidate();
            component.repaint();
        });
    }

    /**
     * Invoked internally as needed to remove all UI components and re-do the form
     * layout.
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
     * Invoked internally to render the field label for the given FormField, if it
     * has one.
     */
    private void renderFieldLabel(FormField field, int row, Margins margins) {
        if (!field.hasFieldLabel()) {
            return;
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = FORM_FIELD_START_COLUMN;
        constraints.gridy = row;
        constraints.anchor = field.isMultiLine() ? GridBagConstraints.FIRST_LINE_START : GridBagConstraints.WEST;
        int extraTopMargin = field.isMultiLine() ? multiLineTopMargin : 0; // TODO don't hard-code this value
        constraints.insets = new Insets(margins.getTop() + extraTopMargin,
                margins.getLeft(),
                margins.getBottom(),
                margins.getInternalSpacing());
        add(field.getFieldLabel(), constraints);
    }

    /**
     * Invoked internally to render the field component for the given FormField, if
     * it has one.
     */
    private void renderFieldComponent(FormField field, int row, Margins margins) {
        if (field.getFieldComponent() == null) {
            return;
        }

        // If a field label exists, then left margin has already been set. Otherwise,
        // set it now.
        int leftMargin = field.hasFieldLabel() ? margins.getInternalSpacing() : margins.getLeft();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(margins.getTop(),
                leftMargin,
                margins.getBottom(),
                margins.getInternalSpacing());
        constraints.gridx = CONTROL_COLUMN;
        constraints.gridy = row;
        constraints.anchor = field.isMultiLine() ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;

        if (field.shouldExpand()) {
            constraints.weightx = 4; // this feels a bit hacky but it does force the component to form width
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
     * Invoked internally to render the help label for the given FormField, if it
     * has one.
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
     * Invoked internally to render the validation label for the given FormField, if
     * it has one.
     */
    private void renderValidationLabel(FormField field, int row, Margins margins) {
        // We need to render it unconditionally because it might have a right margin
        // that we need to honor
        // if (!field.hasValidationLabel()) {
        // return;
        // }

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
        } else if (alignment.isCenteredVertically()) {
            constraints.weighty = 0.5; // Center the form
        } else if (alignment.isBottomAligned()) {
            constraints.weighty = 1; // Force the form to the bottom of the panel
        }
        this.add(new JLabel(), constraints);
    }

    /**
     * Adds a left margin to the given grid row to align the form field horizontally
     * on that row.
     */
    private void addLeftMargin(int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = LEFT_SPACER_COLUMN;
        constraints.gridy = row;
        constraints.fill = GridBagConstraints.BOTH;
        if (alignment.isLeftAligned()) {
            constraints.weightx = 0.0; // addRightMargin will force the form to the left
        } else if (alignment.isCenteredHorizontally()) {
            constraints.weightx = 0.5; // center the form
        } else if (alignment.isRightAligned()) {
            constraints.weightx = 1.0; // Force the form to the right
        }
        add(new JLabel(), constraints);
    }

    /**
     * Adds a right margin to the given grid row to align the form field
     * horizontally on that row.
     */
    private void addRightMargin(int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = RIGHT_SPACER_COLUMN;
        constraints.gridy = row;
        constraints.fill = GridBagConstraints.BOTH;
        if (alignment.isLeftAligned()) {
            constraints.weightx = 1.0; // Force the form to the left
        } else if (alignment.isCenteredHorizontally()) {
            constraints.weightx = 0.5; // center the form
        } else if (alignment.isRightAligned()) {
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
        } else if (alignment.isCenteredVertically()) {
            constraints.weighty = 0.5; // Center the form
        } else if (alignment.isBottomAligned()) {
            constraints.weighty = 0.0; // addHeaderMargin will force the form to the bottom
        }
        this.add(new JLabel(), constraints);
    }

    /**
     * Applies our borderMargin to the FormField at the given fieldIndex based on
     * its
     * position within our form field list. The first field in the list is the
     * topmost
     * field, so it will receive an extra top margin, for example. All fields in the
     * list
     * will receive an extra left and right margin, and the last field in the list
     * will receive an extra bottom margin. The borderMargin value is added to the
     * FormField's existing margins. The FormField's margins are not modified as a
     * result of this calculation. Instead, a new Margins object will be created and
     * returned.
     */
    private Margins calculateFieldMargins(int fieldIndex) {
        FormField field = formFields.get(fieldIndex);
        Margins margins = new Margins(field.getMargins());

        if (fieldIndex == 0) {
            margins.setTop(margins.getTop() + formPanelMargins.getTop());
        }

        margins.setLeft(margins.getLeft() + formPanelMargins.getLeft());
        margins.setRight(margins.getRight() + formPanelMargins.getRight());

        if (fieldIndex == formFields.size() - 1) {
            margins.setBottom(margins.getBottom() + formPanelMargins.getBottom());
        }

        return margins;
    }

}
