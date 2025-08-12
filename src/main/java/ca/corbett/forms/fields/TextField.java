package ca.corbett.forms.fields;

import ca.corbett.extras.CoalescingDocumentListener;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.NonBlankFieldValidator;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
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

    private final boolean multiLine;
    private JScrollPane scrollPane;

    private final boolean shouldExpandMultiLine;
    private boolean allowPopupEditing;
    private final JTextComponent textComponent;

    private TextField(JTextArea textArea, boolean isExpandable) {
        textComponent = textArea;
        shouldExpandMultiLine = isExpandable;
        multiLine = true;
        scrollPane = createScrollPane(textComponent);
        fieldComponent = scrollPane;
    }

    private TextField(JTextArea textArea, int pixelWidth, int pixelHeight) {
        textComponent = textArea;
        shouldExpandMultiLine = false;
        multiLine = true;
        scrollPane = createScrollPane(textComponent);
        scrollPane.setPreferredSize(new Dimension(pixelWidth, pixelHeight));
        fieldComponent = scrollPane;
    }

    private TextField(JTextField textField) {
        textComponent = textField;
        shouldExpandMultiLine = false;
        multiLine = false;
        fieldComponent = textComponent;
    }

    private void postConstructorInitialization(String label) {
        fieldLabel.setText(label);
        textComponent.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        textComponent.setSize(textComponent.getPreferredSize());
        CoalescingDocumentListener listener = new CoalescingDocumentListener(textComponent,
                                                                             e -> fireValueChangedEvent());
        textComponent.getDocument().addDocumentListener(listener);
        allowPopupEditing = false; // arbitrary default
    }

    public static TextField ofSingleLine(String label, int cols) {
        JTextField textField = new JTextField();
        textField.setColumns(cols);
        TextField field = new TextField(textField);
        field.postConstructorInitialization(label);
        return field;
    }

    public static TextField ofFixedSizeMultiLine(String label, int rows, int cols) {
        JTextArea textArea = createTextArea();
        textArea.setRows(rows);
        textArea.setColumns(cols);
        TextField field = new TextField(textArea, false);
        field.postConstructorInitialization(label);
        return field;
    }

    public static TextField ofFixedPixelSizeMultiLine(String label, int width, int height) {
        TextField field = new TextField(createTextArea(), width, height);
        field.postConstructorInitialization(label);
        return field;
    }

    public static TextField ofDynamicSizingMultiLine(String label, int rows) {
        JTextArea textArea = createTextArea();
        textArea.setRows(rows);
        TextField field = new TextField(textArea, true);
        field.postConstructorInitialization(label);
        return field;
    }

    private static JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private static JScrollPane createScrollPane(JTextComponent textComponent) {
        JScrollPane scrollPane = new JScrollPane(textComponent);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
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
        // TODO set visible/invisible the expand button
        return this;
    }

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
