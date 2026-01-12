package ca.corbett.extras.properties;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.Action;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

/**
 * An extremely simple property which represents a static label.
 * Labels are not input fields, so they do not save or load anything
 * to or from properties. If you are looking for a way to display
 * some read-only programmatically configurable field on a properties
 * dialog, you should use a read-only text field instead of a label.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2024-12-30
 */
public class LabelProperty extends AbstractProperty {

    private int extraTopMargin;
    private int extraBottomMargin;
    private Font labelFont;
    private Color labelColor;
    private Action hyperlinkAction;
    private String fieldLabelText;

    public LabelProperty(String name, String label) {
        this(name, label, null, null, -1, -1);
    }

    public LabelProperty(String name, String label, Font font) {
        this(name, label, font, null, -1, -1);
    }

    public LabelProperty(String name, String label, Font font, Color color) {
        this(name, label, font, color, -1, -1);
    }

    public LabelProperty(String name, String label, Font font, Color color, int extraTopMargin, int extraBottomMargin) {
        super(name, label);
        if (extraTopMargin < 0) {
            extraTopMargin = LabelField.getExtraTopMarginNormal();
        }
        if (extraBottomMargin < 0) {
            extraBottomMargin = LabelField.getExtraBottomMarginNormal();
        }
        if (font != null) {
            setFont(font);
        }
        if (color != null) {
            setColor(color);
        }
        setExtraMargins(extraTopMargin, extraBottomMargin);
        fieldLabelText = ""; // by default, we don't show our field label, just our value label
    }

    /**
     * A static convenience factory method to create a "header" label with sensible
     * defaults for a section header label. The default values are 16 point bold black
     * text with a slightly larger top and bottom margin.
     * <p>
     *     You can control the extra margin above/below the generated label by using
     *     LabelField.setHeaderLabelExtraMargins() before invoking this.
     * </p>
     *
     * @param name The fully qualified field name
     * @param text The label text
     * @return A LabelField suitable for use as a header.
     */
    public static LabelProperty createHeaderLabel(String name, String text) {
        Color labelColor = LookAndFeelManager.getLafColor("Label.foreground", Color.BLACK);
        return new LabelProperty(name,
                                 text,
                                 LabelField.getDefaultHeaderFont(),
                                 labelColor,
                                 LabelField.getExtraTopMarginHeader(),
                                 LabelField.getExtraBottomMarginHeader());
    }

    /**
     * A static convenience factory method to create a "normal" label with sensible
     * defaults for a form label. The default values are 12 point plain black text
     * with a slightly larger top and bottom margin.
     * <p>
     *     You can control the extra margin above/below the generated label by using
     *     LabelField.setHeaderLabelExtraMargins() before invoking this.
     * </p>
     *
     * @param name The fully qualified field name
     * @param text The label text
     * @return A LabelField suitable for use as a regular form label.
     */
    public static LabelProperty createLabel(String name, String text) {
        Color labelColor = LookAndFeelManager.getLafColor("Label.foreground", Color.BLACK);
        return new LabelProperty(name,
                                 text,
                                 LabelField.getDefaultLabelFont(),
                                 labelColor,
                                 LabelField.getExtraTopMarginNormal(),
                                 LabelField.getExtraBottomMarginNormal());
    }

    @Override
    public void saveToProps(Properties props) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

    @Override
    public void loadFromProps(Properties props) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

    /**
     * Optionally sets a hyperlink action, which will convert the generated LabelField into
     * a hyperlink field. When the user clicks the label, the specified Action will be invoked,
     * and a change event will fire from this property (so listeners know that it was clicked).
     * <p>
     * <B>TO UPDATE THE FORM WHEN THE LINK IS CLICKED:</B> you can send in an empty Action
     * and instead rely on the change event that will fire from this property. The
     * PropertyFormFieldValueChangedEvent will contain the FormField and FormPanel that
     * where the click happened, and you can use that FormPanel to look up other FormFields
     * and take whatever action is necessary (show/hide fields, enable/disable fields, etc).
     * </p>
     *
     * @param action any arbitrary Action.
     */
    public LabelProperty setHyperlink(Action action) {
        hyperlinkAction = action;
        return this;
    }

    /**
     * Optionally set extra margin padding to go above and below the label.
     */
    public LabelProperty setExtraMargins(int top, int bottom) {
        extraTopMargin = top;
        extraBottomMargin = bottom;
        return this;
    }

    /**
     * Returns the extra top margin for this label.
     */
    public int getExtraTopMargin() {
        return extraTopMargin;
    }

    /**
     * Returns the extra bottom margin for this label.
     */
    public int getExtraBottomMargin() {
        return extraBottomMargin;
    }

    public LabelProperty setFont(Font f) {
        labelFont = f;
        return this;
    }

    public LabelProperty setColor(Color c) {
        labelColor = c;
        return this;
    }

    /**
     * Returns our field label text. Defaults to empty string, meaning no field label is shown.
     */
    public String getFieldLabelText() {
        return fieldLabelText;
    }

    /**
     * Optionally sets text for our field label. Normally, we don't show our field label, only
     * our value label. But you can set this to a non-empty string to show a field label as well.
     */
    public LabelProperty setFieldLabelText(String fieldLabelText) {
        this.fieldLabelText = fieldLabelText;
        return this;
    }

    @Override
    protected FormField generateFormFieldImpl() {
        LabelField field = new LabelField(propertyLabel);
        field.getFieldLabel().setText(fieldLabelText);
        field.getMargins().setTop(field.getMargins().getTop() + extraTopMargin);
        field.getMargins().setBottom(field.getMargins().getBottom() + extraBottomMargin);
        if (labelFont != null) {
            field.setFont(labelFont);
        }
        if (labelColor != null) {
            ((JLabel)field.getFieldComponent()).setForeground(labelColor);
        }
        if (hyperlinkAction != null) {
            field.setHyperlink(hyperlinkAction);
        }
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

}
