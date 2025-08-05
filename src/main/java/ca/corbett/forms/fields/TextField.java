package ca.corbett.forms.fields;

import ca.corbett.forms.validators.NonBlankFieldValidator;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;

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
    private JScrollPane scrollPane;

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
        fieldLabel.setText(label);
        if (rows > 1) {
            textComponent = new JTextArea(rows, cols);
            ((JTextArea)textComponent).setLineWrap(true);
            ((JTextArea)textComponent).setWrapStyleWord(true);
            scrollPane = new JScrollPane(textComponent);
            //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            //textComponent.setSize(textComponent.getPreferredSize());
            textComponent.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            multiLine = true;
            fieldComponent = scrollPane;
        }
        else {
            textComponent = new JTextField();
            ((JTextField)textComponent).setColumns(cols);
            multiLine = false;
            fieldComponent = textComponent;
        }
        textComponent.getDocument().addDocumentListener(changeListener);
        if (!allowBlank) {
            addFieldValidator(new NonBlankFieldValidator());
        }
        multiLineTextBoxLeftMargin = 0;
        multiLineTextBoxTopMargin = 0;
        multiLineTextBoxBottomMargin = 0;
        multiLineTextBoxRightMargin = 0;
        scrollPaneWidth = -1;
        scrollPaneHeight = -1;
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
        if (scrollPane == null) {
            return;
        }
        scrollPaneWidth = w;
        scrollPaneHeight = h;
        if (scrollPaneWidth > 0 && scrollPaneHeight > 0) {
            scrollPane.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        }
        else {
            scrollPane.setPreferredSize(null);
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

    public JTextComponent getTextComponent() {
        return textComponent;
    }

    @Override
    public boolean isMultiLine() {
        return multiLine;
    }
}
