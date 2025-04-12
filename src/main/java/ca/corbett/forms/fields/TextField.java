package ca.corbett.forms.fields;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.validators.NonBlankFieldValidator;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * A FormField implementation specifically for text input.
 * The wrapped field is either a JTextField or a JTextArea depending on whether
 * you specify multi-line input or not. Yes, that's right! All the niggly
 * details of setting up text input are hugely simplified here.
 *
 * @author scorbo2
 * @since 2019-11-23
 */
public final class TextField extends FormField {

    private final boolean multiLine;
    private int multiLineTextBoxLeftMargin;
    private int multiLineTextBoxTopMargin;
    private int multiLineTextBoxBottomMargin;
    private int multiLineTextBoxRightMargin;
    private boolean expandMultiLineHorizontally;
    private boolean addScrollPaneWhenMultiLine;

    private int scrollPaneWidth;
    private int scrollPaneHeight;

    private final JTextComponent textComponent;
    private final DocumentListener changeListener = new DocumentListener() {
        @Override
        public void changedUpdate(DocumentEvent e) {
            fireValueChangedEvent();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            fireValueChangedEvent();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            fireValueChangedEvent();
        }

    };

    /**
     * Creates a new TextField with the specified parameters.
     *
     * @param label      The label to place next to the field.
     * @param cols       The number of columns to set for the JTextField.
     * @param rows       The number of rows. If greater than 1, a JTextArea will be used instead of
     *                   JTextField.
     * @param allowBlank If false, a FieldValidator will be attached to ensure the value isn't blank.
     */
    public TextField(String label, int cols, int rows, boolean allowBlank) {
        fieldLabel = new JLabel(label);
        fieldLabel.setFont(fieldLabelFont);
        if (rows > 1) {
            textComponent = new JTextArea(rows, cols);
            ((JTextArea)textComponent).setLineWrap(true);
            ((JTextArea)textComponent).setWrapStyleWord(true);
            textComponent.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            multiLine = true;
        }
        else {
            textComponent = new JTextField();
            ((JTextField)textComponent).setColumns(cols);
            multiLine = false;
        }
        fieldComponent = textComponent;
        textComponent.getDocument().addDocumentListener(changeListener);
        if (!allowBlank) {
            addFieldValidator(new NonBlankFieldValidator(this));
        }
        multiLineTextBoxLeftMargin = 0;
        multiLineTextBoxTopMargin = 0;
        multiLineTextBoxBottomMargin = 0;
        multiLineTextBoxRightMargin = 0;
        addScrollPaneWhenMultiLine = true;
        scrollPaneWidth = -1;
        scrollPaneHeight = -1;
        isExtraLabelRenderedByField = true;
    }

    /**
     * You can tell the multi-line textbox to take up additional horizontal space if the
     * parent container is resized. Does nothing for single-line text fields.
     *
     * @param expand Whether to do horizontal expansion of the multiline text box.
     */
    public void setExpandMultiLineTextBoxHorizontally(boolean expand) {
        expandMultiLineHorizontally = expand;
    }

    /**
     * You can prevent a JScrollPane from being used when the text field is multi-line (default true).
     * If false, the text pane will grow vertically as new lines are added. If true, the text
     * pane will stay at its original size and a vertical scrollbar will appear as needed.
     * True is usually the way to go. This method is provided in case any application code
     * relies on the old behaviour of defaulting this to false with no way to change it.
     *
     * @param value The new value for this option. Must be set before the form is rendered.
     */
    public void setAddScrollPaneWhenMultiLine(boolean value) {
        addScrollPaneWhenMultiLine = value;
    }

    /**
     * Optionally set a preferred size for the scroll pane for use with multi-line text fields.
     * If the given width or height values are less than or equal to zero, the old behaviour
     * is used, where the text box fills its grid bag cell. Setting both of these values to
     * non-zero positive numbers will enable the scroll pane and fix its size to the given
     * values. Does nothing for single-line text fields.
     *
     * @param w Preferred pixel width of the text box's scroll pane.
     * @param h Preferred pixel height of the text box's scroll pane.
     */
    public void setScrollPanePreferredSize(int w, int h) {
        scrollPaneWidth = w;
        scrollPaneHeight = h;
        if (scrollPaneWidth > 0 && scrollPaneHeight > 0) {
            addScrollPaneWhenMultiLine = true;
        }
    }

    /**
     * You can horizontally indent and/or vertically space out the multiline text box
     * if you want, to visually set it out from other form components and make the form
     * look a bit less busy.
     *
     * @param top    The top margin
     * @param left   The left margin
     * @param bottom The bottom margin
     * @param right  The right margin - only used if expandMultiLineTextBoxHorizontally is set.
     */
    public void setMultiLineTextBoxMargins(int top, int left, int bottom, int right) {
        multiLineTextBoxLeftMargin = left;
        multiLineTextBoxTopMargin = top;
        multiLineTextBoxBottomMargin = bottom;
    }

    /**
     * Returns the text currently in this field.
     *
     * @return The current text value.
     */
    public String getText() {
        return textComponent.getText();
    }

    /**
     * Sets the text in this field. Will overwrite any previous text.
     *
     * @param text The new text.
     */
    public void setText(String text) {
        textComponent.setText(text);
    }

    /**
     * Renders this field into the given panel.
     *
     * @param container   The containing form panel.
     * @param constraints The constraints to use.
     */
    @Override
    public void render(JPanel container, GridBagConstraints constraints) {
        constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, componentSpacing);
        constraints.gridy = constraints.gridy + 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = FormPanel.LABEL_COLUMN;
        constraints.gridwidth = multiLine ? 2 : 1;
        fieldLabel.setFont(fieldLabelFont);
        container.add(fieldLabel, constraints);

        if (multiLine) {
            if (!helpText.isBlank()) {
                constraints.gridx = FormPanel.HELP_COLUMN;
                constraints.gridwidth = 1;
                constraints.fill = GridBagConstraints.NONE;
                constraints.insets = new Insets(topMargin, componentSpacing, componentSpacing, componentSpacing);
                if (FormPanel.helpImageUrl != null) {
                    helpLabel.setIcon(new ImageIcon(FormPanel.helpImageUrl));
                }
                helpLabel.setToolTipText(helpText);
                container.add(helpLabel, constraints);
            }

            if (expandMultiLineHorizontally) {
                constraints.insets = new Insets(topMargin, componentSpacing, componentSpacing, rightMargin);
                constraints.gridwidth = 1;
                constraints.fill = GridBagConstraints.NONE;
                constraints.gridx = FormPanel.VALIDATION_COLUMN;
                container.add(validationLabel, constraints);
            }

            constraints.gridx = FormPanel.LABEL_COLUMN;
            constraints.gridwidth = expandMultiLineHorizontally ? 4 : 2;
            constraints.gridy = constraints.gridy + 1;
            if (scrollPaneWidth > 0 && scrollPaneHeight > 0) {
                constraints.fill = GridBagConstraints.NONE;
            }
            else {
                constraints.fill = GridBagConstraints.BOTH;
            }
            int rightMarginValue = rightMargin + (expandMultiLineHorizontally ? multiLineTextBoxRightMargin : componentSpacing);
            constraints.insets = new Insets(topMargin + multiLineTextBoxTopMargin,
                                            leftMargin + multiLineTextBoxLeftMargin,
                                            bottomMargin + multiLineTextBoxBottomMargin,
                                            rightMarginValue);
            constraints.weightx = expandMultiLineHorizontally ? 0.05 : 0.0;
            if (addScrollPaneWhenMultiLine) {
                JScrollPane scrollPane = new JScrollPane(textComponent);
                if (scrollPaneWidth > 0 && scrollPaneHeight > 0) {
                    scrollPane.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
                }
                container.add(scrollPane, constraints);
            }
            else {
                container.add(textComponent, constraints);
            }
            constraints.weightx = 0.0;
            constraints.gridwidth = 1;

            if (!expandMultiLineHorizontally) {
                constraints.insets = new Insets(0, 0, 0, rightMargin);
                constraints.fill = GridBagConstraints.NONE;
                constraints.gridx = FormPanel.VALIDATION_COLUMN;
                constraints.anchor = GridBagConstraints.NORTHWEST;
                container.add(validationLabel, constraints);
                constraints.anchor = GridBagConstraints.WEST;
            }
        }
        else {
            constraints.insets = new Insets(topMargin, componentSpacing, bottomMargin, componentSpacing);
            constraints.gridwidth = 1;
            constraints.gridx = FormPanel.CONTROL_COLUMN;
            container.add(textComponent, constraints);

            if (!helpText.isBlank()) {
                constraints.gridx = FormPanel.HELP_COLUMN;
                constraints.gridwidth = 1;
                constraints.fill = GridBagConstraints.NONE;
                constraints.insets = new Insets(topMargin, componentSpacing, bottomMargin, componentSpacing);
                if (FormPanel.helpImageUrl != null) {
                    helpLabel.setIcon(new ImageIcon(FormPanel.helpImageUrl));
                }
                helpLabel.setToolTipText(helpText);
                container.add(helpLabel, constraints);
            }

            constraints.insets = new Insets(topMargin, componentSpacing, bottomMargin, rightMargin);
            constraints.gridwidth = 1;
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = FormPanel.VALIDATION_COLUMN;
            container.add(validationLabel, constraints);
        }
    }

}
