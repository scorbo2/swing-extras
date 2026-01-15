package ca.corbett.forms;

import ca.corbett.forms.fields.FormField;

import javax.swing.JLabel;
import javax.swing.JPanel;
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
 *         call validateForm() or isFormValid(), and the validators will be invoked automatically.
 *     <li><b>Optional inline help</b> - every FormField can have optional help text
 *         which the FormPanel will render inline in the form of an information icon
 *         next to the field, which will show tooltip help text.
 *     <li><b>Generic FormField handling</b> - the FormField class is highly
 *         extensible, so you can very easily provide your own FormField implementation
 *         for custom display and editing of data.
 * </ul>
 *
 * <h2>Handling oversized forms</h2>
 * <p>
 * You can easily add a FormPanel to a JScrollPane to provide for scrollbars in the
 * case where the form is unreasonably large.
 * </p>
 *
 * <h2>Alignment</h2>
 * <p>
 *     The Alignment property of the FormPanel controls how the entire form is aligned
 *     within the panel. You can choose left, center, or right horizontal alignment,
 *     and top, center, or bottom vertical alignment.
 * </p>
 * <p>
 *     Additionally, the setBorderMargin() method allows you to specify extra pixel margins
 *     around the entire form, to keep it away from the edges of its container.
 * </p>
 *
 * <h2>More documentation</h2>
 * <p>
 *     The <a href="https://www.corbett.ca/swing-extras-book/">swing-extras-book</a>
 *     contains much more documentation regarding the use of FormPanel, FormField,
 *     and related classes. Additionally, the built-in demo application showcases
 *     many of the features of this library.
 * </p>
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
    private int multiLineFieldExtraTopMargin = 4;

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
        render();
    }

    /**
     * An optional pixel margin for the FormPanel itself to keep it away from the edges
     * of its container. The default value is zero, meaning no extra margins will
     * be applied. Note that any value given here is added to whatever margins are already present on
     * each individual FormField. This value does not replace those values. So, you can still indent
     * a FormField from the others by setting its left margin on a left-aligned form, even if the
     * FormPanel itself already specifies a border margin. The two values are added together in that case.
     *
     * @param margin The margin, in pixels, to apply to the FormPanel. Negative values are treated as 0.
     * @return This FormPanel instance, to allow method chaining.
     */
    public FormPanel setBorderMargin(int margin) {
        formPanelMargins.setAll(margin);
        render();
        return this;
    }

    /**
     * Sets optional pixel margins for the FormPanel itself to keep it away from the edges
     * of its container. The default value is zero, meaning no extra margins will
     * be applied. Note that any value given here is added to whatever margins are already present on
     * each individual FormField. This value does not replace those values. So, you can still indent
     * a FormField from the others by setting its left margin on a left-aligned form, even if the
     * FormPanel itself already specifies a border margin. The two values are added together in that case.
     * <p>
     * <b>Note:</b> the Margins class defines an internalSpacing property. That property is ignored
     * for FormPanel margin calculations. We only care about top, left, bottom, and right.
     * "internalSpacing" is only relevant within individual FormField instances.
     * </p>
     *
     * @param margins The Margins instance defining the margins to apply. Negative values are treated as 0.
     * @return This FormPanel instance, to allow method chaining.
     */
    public FormPanel setBorderMargin(Margins margins) {
        formPanelMargins.copy(margins);
        render();
        return this;
    }

    /**
     * Returns the optional border margin to be applied as described in setBorderMargin.
     * The actual instance is returned, so callers can modify individual properties.
     */
    public Margins getBorderMargin() {
        return formPanelMargins;
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
     * Synonym for getFormField(String identifier).
     *
     * @param identifier The identifier to search for.
     * @return A FormField matching that identifier, or null if not found.
     */
    public FormField findFormField(String identifier) {
        return getFormField(identifier);
    }

    /**
     * Removes all FormFields from this FormPanel and re-renders it.
     *
     * @return This FormPanel instance, to allow method chaining.
     */
    public FormPanel removeAllFormFields() {
        formFields.clear();
        render();
        return this;
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
        // No need to re-render here, since enabling/disabling does not change layout
    }

    /**
     * Synonym for addAll(List<FormField> fields).
     */
    public FormPanel add(List<FormField> fields) {
        addAll(fields);
        return this;
    }

    /**
     * Adds the specified list of FormFields to this FormPanel.
     */
    public FormPanel addAll(List<FormField> fields) {
        this.formFields.addAll(fields);
        render();
        return this;
    }

    /**
     * Adds the specified FormField to this FormPanel.
     */
    public FormPanel add(FormField field) {
        this.formFields.add(field);
        render();
        return this;
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
    public FormPanel clearValidationResults() {
        // We don't need to re-render here, since clearing validation results
        // does not change layout (FormField will handle hiding its validation label internally):
        for (FormField field : formFields) {
            field.clearValidationResults();
        }
        return this;
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
        // No need to re-render here, since validation does not change layout
        // (FormField will handle showing its validation label internally)
        return isValid;
    }

    /**
     * Shorthand for isFormValid()
     */
    public FormPanel validateForm() {
        isFormValid();
        return this;
    }

    /**
     * Changes the Alignment property of this FormPanel and re-renders it.
     */
    public FormPanel setAlignment(Alignment alignment) {
        this.alignment = alignment;
        render();

        return this;
    }

    /**
     * Returns the Alignment property of this FormPanel.
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * This is an obsolete method from an earlier version of swing-forms. It is
     * scheduled for removal in a future release.
     *
     * @Deprecated As of swing-extras 2.7. This method will be removed in a future release.
     */
    @Deprecated(since = "swing-extras 2.7", forRemoval = true)
    public FormPanel forceRerender() {
        // What was the use case for making this public? No usages found. Probably safe to remove.
        // We'll do it in 2.8, after I'm 100% convinced no one is using it.
        render();
        return this;
    }

    /**
     * Returns the extra top margin, in pixels, that will be applied to multi-line
     * field labels to better align them with their associated field components.
     */
    public int getMultiLineFieldExtraTopMargin() {
        return multiLineFieldExtraTopMargin;
    }

    /**
     * There's a very slight misalignment issue with field labels on multi-line
     * FormField implementations due to the way GridBagLayout anchors components. This
     * property allows you to specify an extra top margin, in pixels, to be applied
     * to multi-line field labels to better align them with their associated field components.
     * The default value is 4 pixels, which is usually sufficient. You can set this to zero
     * if you don't want any extra margin applied.
     *
     * @param multiLineFieldExtraTopMargin The extra top margin, in pixels, for multi-line field labels.
     * @return This FormPanel instance, to allow method chaining.
     */
    public FormPanel setMultiLineFieldExtraTopMargin(int multiLineFieldExtraTopMargin) {
        this.multiLineFieldExtraTopMargin = multiLineFieldExtraTopMargin;
        render();
        return this;
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

        // Finally, invalidate/revalidate/repaint to ensure UI updates properly:
        invalidate();
        revalidate();
        repaint();
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
        int extraTopMargin = field.isMultiLine() ? multiLineFieldExtraTopMargin : 0;
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
        constraints.anchor = field.isMultiLine() ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;

        if (field.shouldExpand()) {
            constraints.weightx = 4; // this feels a bit hacky, but it does force the component to form width
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
     * position within our form field list. The first field in the list is the topmost
     * field, so it will receive an extra top margin, for example. All fields in the list
     * will receive an extra left and right margin, and the last field in the list
     * will receive an extra bottom margin. The borderMargin value is added to the
     * FormField's existing margins. The FormField's margins are not modified as a
     * result of this calculation. Instead, a new Margins object will be created and returned.
     */
    private Margins calculateFieldMargins(int fieldIndex) {
        FormField field = formFields.get(fieldIndex);
        Margins margins = new Margins(field.getMargins());

        // First field gets the top margin:
        if (fieldIndex == 0) {
            margins.setTop(margins.getTop() + formPanelMargins.getTop());
        }

        // All fields get left and right margins:
        margins.setLeft(margins.getLeft() + formPanelMargins.getLeft());
        margins.setRight(margins.getRight() + formPanelMargins.getRight());

        // Last field gets the bottom margin:
        if (fieldIndex == formFields.size() - 1) {
            margins.setBottom(margins.getBottom() + formPanelMargins.getBottom());
        }

        return margins;
    }

}
