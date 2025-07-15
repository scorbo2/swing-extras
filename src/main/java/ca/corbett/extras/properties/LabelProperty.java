package ca.corbett.extras.properties;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;

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
 * @author scorbo2
 * @since 2024-12-30
 */
public class LabelProperty extends AbstractProperty {

    private int extraTopMargin;
    private int extraBottomMargin;
    private Font labelFont;
    private Color labelColor;

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


    public LabelProperty setExtraMargins(int top, int bottom) {
        extraTopMargin = top;
        extraBottomMargin = bottom;
        return this;
    }

    public LabelProperty setFont(Font f) {
        labelFont = f;
        return this;
    }

    public LabelProperty setColor(Color c) {
        labelColor = c;
        return this;
    }

    @Override
    protected FormField generateFormFieldImpl() {
        LabelField field = new LabelField(propertyLabel);
        field.setTopMargin(field.getTopMargin() + extraTopMargin);
        field.setBottomMargin(field.getBottomMargin() + extraBottomMargin);
        if (labelFont != null) {
            field.setFont(labelFont);
        }
        if (labelColor != null) {
            ((JLabel)field.getFieldComponent()).setForeground(labelColor);
        }
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

}
