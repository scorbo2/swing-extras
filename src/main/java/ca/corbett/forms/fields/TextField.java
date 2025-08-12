package ca.corbett.forms.fields;

import ca.corbett.extras.CoalescingDocumentListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.NonBlankFieldValidator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A FormField implementation specifically for text input.
 * The wrapped field is either a JTextField or a JTextArea depending on whether
 * you specify multi-line input or not. Yes, that's right! All the niggly
 * details of setting up text input are hugely simplified here.
 * <p>
 * The underlying JTextComponent can be accessed directly if needed by
 * using getFieldComponent() and casting the result to JTextArea (for multi-line
 * fields) or JTextField (for single-line fields).
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2019-11-23
 */
public final class TextField extends FormField {

    public enum TextFieldType {
        SINGLE_LINE,
        MULTI_LINE_FIXED_ROWS_COLS,
        MULTI_LINE_FIXED_PIXELS,
        MULTI_LINE_DYNAMIC
    }

    private final boolean multiLine;
    private JScrollPane scrollPane;
    private JPanel popoutPanel;

    private boolean shouldExpandMultiLine;
    private boolean allowPopupEditing;
    private final JTextComponent textComponent;

    /**
     * Invoked internally by the static factory methods.
     */
    private TextField(String label, boolean isMultiLine) {
        if (isMultiLine) {
            JTextArea textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textComponent = textArea;
            textComponent.setSize(textComponent.getPreferredSize());
            scrollPane = new JScrollPane(textComponent);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            JPanel wrapperPanel = new JPanel(new BorderLayout());
            wrapperPanel.add(scrollPane, BorderLayout.CENTER);
            popoutPanel = buildPopoutPanel();
            wrapperPanel.add(popoutPanel, BorderLayout.SOUTH);
            fieldComponent = wrapperPanel;
        }
        else {
            textComponent = new JTextField();
            fieldComponent = textComponent;
        }
        multiLine = isMultiLine;
        fieldLabel.setText(label);
        textComponent.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        CoalescingDocumentListener listener = new CoalescingDocumentListener(textComponent,
                                                                             e -> fireValueChangedEvent());
        textComponent.getDocument().addDocumentListener(listener);
        allowPopupEditing = false; // arbitrary default
        shouldExpandMultiLine = false; // arbitrary default
    }

    public static TextField ofSingleLine(String label, int cols) {
        TextField field = new TextField(label, false);
        ((JTextField)field.textComponent).setColumns(cols);
        return field;
    }

    public static TextField ofFixedSizeMultiLine(String label, int rows, int cols) {
        TextField field = new TextField(label, true);
        ((JTextArea)field.textComponent).setRows(rows);
        ((JTextArea)field.textComponent).setColumns(cols);
        return field;
    }

    public static TextField ofFixedPixelSizeMultiLine(String label, int width, int height) {
        TextField field = new TextField(label, true);
        field.scrollPane.setPreferredSize(new Dimension(width, height));
        return field;
    }

    public static TextField ofDynamicSizingMultiLine(String label, int rows) {
        TextField field = new TextField(label, true);
        ((JTextArea)field.textComponent).setRows(rows);
        field.shouldExpandMultiLine = true;
        return field;
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
    public TextField setText(String text) {
        if (Objects.equals(textComponent.getText(), text)) {
            return this; // reject no-op changes
        }
        if (text == null) {
            text = ""; // if null, assume empty string
        }
        textComponent.setText(text);
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
    public TextField setAllowBlank(boolean allow) {
        if (allow) {
            removeNonBlankValidatorIfPresent();
        }
        else {
            addNonBlankValidatorIfNotPresent();
        }
        return this;
    }

    public boolean isAllowPopupEditing() {
        return allowPopupEditing;
    }

    public TextField setAllowPopupEditing(boolean allow) {
        allowPopupEditing = allow;
        if (popoutPanel != null) {
            popoutPanel.setVisible(allow);
        }
        return this;
    }

    /**
     * Returns the JScrollPane used for multi-line fields, or null if this is a single-line field.
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Returns the underlying JTextComponent for this field, which is a JTextArea for multi-line
     * fields or a JTextField for single-line fields.
     */
    public JTextComponent getTextComponent() {
        return textComponent;
    }

    @Override
    public boolean isMultiLine() {
        return multiLine;
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
