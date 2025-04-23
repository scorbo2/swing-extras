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

    private static final int DEFAULT_TOP_MARGIN = 4;
    private static final int DEFAULT_BOTTOM_MARGIN = 4;
    private static final Font DEFAULT_HEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 16);
    private static final Font DEFAULT_LABEL_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);

    private int extraTopMargin;
    private int extraBottomMargin;
    private Font labelFont;
    private Color labelColor;

    public LabelProperty(String name, String label) {
        this(name, label, null, null, DEFAULT_TOP_MARGIN, DEFAULT_BOTTOM_MARGIN);
    }

    public LabelProperty(String name, String label, Font font) {
        this(name, label, font, null, DEFAULT_TOP_MARGIN, DEFAULT_BOTTOM_MARGIN);
    }

    public LabelProperty(String name, String label, Font font, Color color) {
        this(name, label, font, color, DEFAULT_TOP_MARGIN, DEFAULT_BOTTOM_MARGIN);
    }

    public LabelProperty(String name, String label, Font font, Color color, int extraTopMargin, int extraBottomMargin) {
        super(name, label);
        extraTopMargin = DEFAULT_TOP_MARGIN;
        extraBottomMargin = DEFAULT_BOTTOM_MARGIN;
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
     *
     * @param name The fully qualified field name
     * @param text The label text
     * @return A LabelField suitable for use as a header.
     */
    public static LabelProperty createHeaderLabel(String name, String text) {
        Color labelColor = LookAndFeelManager.getLafColor("Label.foreground", Color.BLACK);
        return new LabelProperty(name, text, DEFAULT_HEADER_FONT, labelColor, 10, 0);
    }

    /**
     * A static convenience factory method to create a "normal" label with sensible
     * defaults for a form label. The default values are 12 point plain black text
     * with a 4 pixel top and bottom margin.
     *
     * @param name The fully qualified field name
     * @param text The label text
     * @return A LabelField suitable for use as a regular form label.
     */
    public static LabelProperty createLabel(String name, String text) {
        Color labelColor = LookAndFeelManager.getLafColor("Label.foreground", Color.BLACK);
        return new LabelProperty(name, text, DEFAULT_LABEL_FONT, labelColor, 4, 4);
    }

    @Override
    public void saveToProps(Properties props) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

    @Override
    public void loadFromProps(Properties props) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

    public void setExtraMargins(int top, int bottom) {
        extraTopMargin = top;
        extraBottomMargin = bottom;
    }

    public void setFont(Font f) {
        labelFont = f;
    }

    public void setColor(Color c) {
        labelColor = c;
    }

    @Override
    public FormField generateFormField() {
        LabelField field = new LabelField(propertyLabel);
        field.setTopMargin(field.getTopMargin() + extraTopMargin);
        field.setBottomMargin(field.getBottomMargin() + extraBottomMargin);
        if (labelFont != null) {
            field.setFont(labelFont);
        }
        if (labelColor != null) {
            ((JLabel)field.getFieldComponent()).setForeground(labelColor);
        }
        field.setIdentifier(fullyQualifiedName);
        field.setEnabled(!isReadOnly);
        field.setHelpText(helpText);
        return field;
    }

    @Override
    public void loadFromFormField(FormField field) {
        // Labels are static form fields, so there's literally nothing to do here.
    }

}
