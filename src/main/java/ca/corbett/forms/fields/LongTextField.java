package ca.corbett.forms.fields;

import ca.corbett.extras.PopupTextDialog;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.NonBlankFieldValidator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A FormField implementation specifically for long (multi-line) text input.
 * The wrapped component is a JTextArea, which you can access directly
 * if needed via the getTextArea() method.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class LongTextField extends FormField implements DocumentListener {

    public enum TextFieldType {
        MULTI_LINE_FIXED_ROWS_COLS,
        MULTI_LINE_FIXED_PIXELS,
        MULTI_LINE_DYNAMIC
    }

    private final JTextArea textArea;
    private final JScrollPane scrollPane;
    private final JPanel popoutPanel;

    private boolean shouldExpandMultiLine;
    private boolean allowPopoutEditing;

    /**
     * Invoked internally by the static factory methods.
     */
    private LongTextField(String label) {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize());
        textArea.setFont(getDefaultFont());
        scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        popoutPanel = buildPopoutPanel();
        wrapperPanel.add(popoutPanel, BorderLayout.SOUTH);
        fieldComponent = wrapperPanel;
        fieldLabel.setText(label);
        textArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        textArea.getDocument().addDocumentListener(this);
        allowPopoutEditing = false; // arbitrary default
        shouldExpandMultiLine = false; // arbitrary default
    }

    /**
     * Creates a JTextArea with the specified number of rows and columns.
     */
    public static LongTextField ofFixedSizeMultiLine(String label, int rows, int cols) {
        LongTextField field = new LongTextField(label);
        field.getTextArea().setRows(rows);
        field.getTextArea().setColumns(cols);
        return field;
    }

    /**
     * Creates a JTextArea whose scroll pane will be fixed to the given pixel dimensions.
     * Usage of explicit pixel dimensions is discouraged, as it doesn't play with with
     * font size changes or with certain Look and Feels.
     */
    public static LongTextField ofFixedPixelSizeMultiLine(String label, int width, int height) {
        LongTextField field = new LongTextField(label);
        field.scrollPane.setPreferredSize(new Dimension(width, height));
        return field;
    }

    /**
     * Creates a JTextArea with dynamic width and with the specified number of rows. The width
     * of the text area will expand to fill the available width in the container panel.
     */
    public static LongTextField ofDynamicSizingMultiLine(String label, int rows) {
        LongTextField field = new LongTextField(label);
        field.getTextArea().setRows(rows);
        field.getTextArea().setColumns(10); // not a limit! setting this eases the swing layout calcs / avoids flicker
        field.shouldExpandMultiLine = true;
        return field;
    }

    /**
     * Overridden here so we can also enable or disable our text area.
     */
    @Override
    public FormField setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textArea.setEnabled(enabled);
        if (!enabled) {
            textArea.setEditable(false); // explicit if disabled
        }
        return this;
    }

    /**
     * This allows the text component (and popout editor) to be read-only
     * without marking the entire field as disabled.
     */
    public LongTextField setEditable(boolean editable) {
        textArea.setEditable(editable);
        return this;
    }

    /**
     * Indicates whether the text area (and popout editor) are editable.
     * This may be false if the entire field is disabled, or if the
     * field is enabled but setEditable(false) has been invoked.
     */
    public boolean isEditable() {
        return textArea.isEditable();
    }

    /**
     * Returns the text currently in this field.
     *
     * @return The current text value.
     */
    public String getText() {
        return textArea.getText();
    }

    /**
     * Sets the text in this field. Will overwrite any previous text.
     *
     * @param text The new text.
     */
    public LongTextField setText(String text) {
        if (Objects.equals(textArea.getText(), text)) {
            return this; // reject no-op changes
        }
        if (text == null) {
            text = ""; // if null, assume empty string
        }
        textArea.setText(text);
        return this;
    }

    /**
     * Reports whether a NonBlankFieldValidator has been added to this TextField.
     */
    public boolean isAllowBlank() {
        for (FieldValidator<? extends FormField> validator : fieldValidators) {
            if (validator instanceof NonBlankFieldValidator) {
                return false;
            }
        }
        return true;
    }

    /**
     * By default, TextField will allow blank values (empty text) to pass validation.
     * You can disallow that with this method - passing false will add a NonBlankFieldValidator
     * to this TextField. Passing true will remove the NonBlankFieldValidator if one is present.
     */
    public LongTextField setAllowBlank(boolean allow) {
        if (allow) {
            removeNonBlankValidatorIfPresent();
        }
        else {
            addNonBlankValidatorIfNotPresent();
        }
        return this;
    }

    /**
     * Whether to show a button to allow a popup dialog with a much larger text edit option.
     */
    public boolean isAllowPopoutEditing() {
        return allowPopoutEditing;
    }

    /**
     * Whether to show a button to allow a popup dialog with a much larger text edit option.
     */
    public LongTextField setAllowPopoutEditing(boolean allow) {
        allowPopoutEditing = allow;
        if (popoutPanel != null) {
            popoutPanel.setVisible(allow);
        }
        return this;
    }

    /**
     * Returns the JScrollPane if direct access is required.
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Returns the underlying JTextArea for this field if direct access is required..
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    @Override
    public boolean isMultiLine() {
        return true;
    }

    @Override
    public boolean shouldExpand() {
        return shouldExpandMultiLine;
    }

    private JPanel buildPopoutPanel() {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btn = new JButton("Pop out...");
        btn.setPreferredSize(new Dimension(120, 24));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PopupTextDialog editor = new PopupTextDialog(SwingUtilities.getWindowAncestor(panel),
                                                             fieldLabel.getText(),
                                                             textArea.getText(),
                                                             true);
                editor.setReadOnly(!isEditable());
                editor.setVisible(true);
                if (editor.wasOkayed()) {
                    textArea.setText(editor.getText());
                }
            }
        });
        panel.add(btn);
        panel.setVisible(false);
        return panel;
    }

    private void addNonBlankValidatorIfNotPresent() {
        boolean found = false;
        for (FieldValidator<? extends FormField> validator : fieldValidators) {
            if (validator instanceof NonBlankFieldValidator) {
                found = true;
                break;
            }
        }
        if (!found) {
            addFieldValidator(new NonBlankFieldValidator());
        }
    }

    private void removeNonBlankValidatorIfPresent() {
        List<FieldValidator<? extends FormField>> foundList = new ArrayList<>();
        for (FieldValidator<? extends FormField> fieldValidator : fieldValidators) {
            if (fieldValidator instanceof NonBlankFieldValidator) {
                foundList.add(fieldValidator);
            }
        }
        for (FieldValidator<? extends FormField> validator : foundList) {
            fieldValidators.remove(validator);
        }
    }

    // DocumentListener interface stuff below this line --------------------------------
    //
    // Yeah, it's horribly broken, but CoalescingDocumentListener is even worse,
    // and will be marked as deprecated in 2.7 (nuked for all time in 2.8).
    // So, until a better solution appears, we're stuck with DocumentListener.
    // https://github.com/scorbo2/swing-extras/issues/251 for the details.

    @Override
    public void insertUpdate(DocumentEvent e) {
        fireValueChangedEvent();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        fireValueChangedEvent();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fireValueChangedEvent();
    }
}
