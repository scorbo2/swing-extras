package ca.corbett.forms.fields;

import ca.corbett.extras.CoalescingDocumentListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.NonBlankFieldValidator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
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
public class LongTextField extends FormField {

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
        CoalescingDocumentListener listener = new CoalescingDocumentListener(textArea,
                                                                             e -> fireValueChangedEvent());
        textArea.getDocument().addDocumentListener(listener);
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
        field.shouldExpandMultiLine = true;
        return field;
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

    private static JPanel buildPopoutPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btn = new JButton("Pop out...");
        btn.setPreferredSize(new Dimension(90, 23));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO launch pop-out edit dialog
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
}
