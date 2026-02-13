package ca.corbett.forms.fields;

import ca.corbett.forms.Margins;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * A FormField implementation for viewing or editing a Margins instance.
 * Margins instances have five properties: left, top, right, bottom, and internalSpacing.
 * This FormField provides a NumberSpinner for each one, and the resulting value
 * of this field is a Margins instance containing the current values of all five properties.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8
 */
public class MarginsField extends FormField {

    private final JSpinner leftField;
    private final JSpinner topField;
    private final JSpinner rightField;
    private final JSpinner bottomField;
    private final JSpinner internalSpacingField;
    private final JLabel headerLabel;

    public MarginsField(String label) {
        this(label, null);
    }

    public MarginsField(String label, Margins initialValue) {
        fieldLabel.setText(label);
        leftField = buildSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        topField = buildSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        rightField = buildSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        bottomField = buildSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        internalSpacingField = buildSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        headerLabel = new JLabel(""); // Placeholder, will be set by setHeaderLabel() if needed.
        fieldComponent = buildFieldComponent(initialValue == null ? new Margins() : new Margins(initialValue));
    }

    /**
     * Don't confuse this with the getMargins() method from the parent class!
     * "getMargins()" will return the actual margins for this FormField, which is not
     * what you're looking for. This method will return a Margins instance created
     * from the current values of the five spinners - the value of this field.
     *
     * @return A Margins instance containing the current values of all five spinners.
     */
    public Margins getMarginsObject() {
        Margins margins = new Margins()
                .setLeft((Integer)leftField.getValue())
                .setTop((Integer)topField.getValue())
                .setRight((Integer)rightField.getValue())
                .setBottom((Integer)bottomField.getValue())
                .setInternalSpacing((Integer)internalSpacingField.getValue());

        // Hook a listener onto it so if the caller modifies it, we can update the spinners to match:
        margins.addListener(this::setMarginsObject);
        return margins;
    }

    /**
     * Sets our field values according to the given Margins instance.
     * If the given instance is null, a new Margins instance with default values will be used instead.
     *
     * @param margins The Margins instance to copy values from, or null to use a new Margins instance with default values.
     * @return This MarginsField instance, for chaining.
     */
    public MarginsField setMarginsObject(Margins margins) {
        if (margins == null) {
            margins = new Margins();
        }
        leftField.setValue(margins.getLeft());
        topField.setValue(margins.getTop());
        rightField.setValue(margins.getRight());
        bottomField.setValue(margins.getBottom());
        internalSpacingField.setValue(margins.getInternalSpacing());
        return this;
    }

    /**
     * We override this to return true, as we will consume multiple rows in the FormPanel.
     */
    @Override
    public boolean isMultiLine() {
        return true;
    }

    /**
     * Overridden here so we can enable or disable all of our spinners and labels as needed
     * when the FormField itself is enabled or disabled.
     *
     * @param isEnabled whether to enable or disable the components.
     */
    @Override
    public FormField setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);
        JPanel wrapperPanel = (JPanel)fieldComponent;
        for (Component component : wrapperPanel.getComponents()) {
            component.setEnabled(isEnabled);
        }
        return this;
    }

    /**
     * Sets an optional header label to display above the five spinners.
     * The three options for labeling this field are:
     * <ul>
     *     <li>No label. Pass "" or null for the label parameter to the constructor, or call
     *         getFieldLabel().setText(""), and the field label will be hidden. Don't
     *         pass in a header label, or set the header label to null, and it also will be hidden.</li>
     *     <li>A field label (usual FormField behavior). Pass some label text to the constructor,
     *         and a field label will be shown to the left of the field, like a normal FormField.
     *         Don't pass in a header label, or set the header label to null, and the
     *         result will be that only the field label is shown.</li>
     *     <li>A header label. Pass "" or null for the label parameter to the constructor to
     *         hide the field label. Then, pass some label text to this method to set the header label,
     *         and the header label will be shown above the five spinners.</li>
     * </ul>
     * <p>
     *     A fourth option would be to set both the field label and the header label together, but
     *     that seems redundant. It is an option, though, if you really like labeling stuff.
     * </p>
     *
     * @param label The text to show in the header label, or null to hide the header label.
     * @return This MarginsField instance, for chaining.
     */
    public MarginsField setHeaderLabel(String label) {
        headerLabel.setText(label == null || label.isBlank() ? "" : label);
        return this;
    }

    /**
     * Returns the optional header label text, or null if the header label is not currently shown.
     *
     * @return The text of the header label, or null if the header label is not currently shown.
     */
    public String getHeaderLabel() {
        return headerLabel.getText().isBlank() ? null : headerLabel.getText();
    }

    /**
     * Builds and returns the wrapper panel that we will use to house all five fields.
     */
    private JComponent buildFieldComponent(Margins initialValue) {
        JPanel panel = new JPanel(new GridBagLayout());

        // Ideally, I'd like to lay out all five spinners in one form row,
        // but I suspect that would be too cramped, so I'll just put them in two rows:
        // (much as I hate GridBagLayout, it seems the best option for this layout):
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 5); // add some spacing between the fields

        // Render the optional header label even if null.
        // This allows us to make it visible later without rebuilding the field component.
        headerLabel.setFont(getDefaultFont().deriveFont(getDefaultFont().getStyle() | java.awt.Font.BOLD));
        gbc.gridwidth = 4; // span all four columns
        gbc.insets = new Insets(0, 0, 10, 0); // add some spacing below the header
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(headerLabel, gbc);

        // Left field and label:
        gbc.gridy = 1;
        JLabel label = new JLabel("Left:");
        label.setLabelFor(leftField);
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(leftField, gbc);
        leftField.setValue(initialValue.getLeft());

        // Top field and label:
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        label = new JLabel("Top:");
        label.setLabelFor(topField);
        panel.add(label, gbc);
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(topField, gbc);
        topField.setValue(initialValue.getTop());

        // Next row, right field and label:
        gbc.gridx = 0;
        gbc.gridy = 2;
        label = new JLabel("Right:");
        label.setLabelFor(rightField);
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(rightField, gbc);
        rightField.setValue(initialValue.getRight());

        // Bottom field and label:
        gbc.gridx = 2;
        label = new JLabel("Bottom:");
        label.setLabelFor(bottomField);
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(label, gbc);
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(bottomField, gbc);
        bottomField.setValue(initialValue.getBottom());

        // Third row, internal spacing:
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3; // push this spinner so it lines up with the ones on the right
        gbc.insets = new Insets(0, 0, 0, 5);
        label = new JLabel("Internal spacing:");
        label.setLabelFor(internalSpacingField);
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(label, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(internalSpacingField, gbc);
        internalSpacingField.setValue(initialValue.getInternalSpacing());

        return panel;
    }

    /**
     * Invoked internally to build a JSpinner and add a listener to it so that
     * we can fire change events when the value changes.
     */
    private JSpinner buildSpinner(SpinnerNumberModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.addChangeListener(e -> fireValueChangedEvent());
        spinner.setPreferredSize(new Dimension(60, 22)); // arbitrary default value, matches NumberField
        spinner.setFont(getDefaultFont());
        return spinner;
    }
}
