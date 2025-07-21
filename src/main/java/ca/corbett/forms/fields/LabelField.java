package ca.corbett.forms.fields;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.FormPanel;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

/**
 * Represents a form field that just presents a static label without any
 * user interaction. This can either be presented as a label:text pair,
 * like a non-editable form field, or as a single label that spans the width
 * of the form, like a section header. Convenience methods are exposed here
 * so you can control the font and colour of the label text. (You could also
 * do this by invoking getFieldComponent() from the FormField parent class
 * and casting it to a JLabel, but the convenience methods here are easier).
 * To reduce confusion, the terminology is as follows: (fieldLabel):(labelText),
 * where (fieldLabel) is optional (if not set, the colon separator will
 * also be hidden).
 * <p>
 * Note that label fields ignore validation, as there's no user input to validate.
 * </p>
 * <p>
 * You can make a multiline label by wrapping the label text in html tags:
 * </p>
 * <blockquote><pre>labelField.setText("&lt;html&gt;Hello&lt;br&gt;second line&lt;br&gt;another line&lt;/html&gt;");</pre>
 * </blockquote>
 *
 * @author scorbp2
 * @since 2019-11-26
 */
public final class LabelField extends FormField {

    private static int extraTopMarginNormal = 8;
    private static int extraBottomMarginNormal = 8;
    private static int extraTopMarginHeader = 12;
    private static int extraBottomMarginHeader = 8;

    private static final Font DEFAULT_HEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 16);
    private static final Font DEFAULT_LABEL_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);

    private final JLabel label;
    private Action hyperlinkAction;
    private Font font;
    private Color color;

    /**
     * Creates a form-width label in the format "labelText".
     *
     * @param labelText The text of the label.
     */
    public LabelField(String labelText) {
        this("", labelText);
    }

    /**
     * Creates a label field in the format "fieldLabel:labelText". If fieldLabel
     * is null or blank, only "labelText" will be displayed.
     *
     * @param fieldLabel The optional string to show as a prefix. Can be null or blank to omit.
     * @param labelText  The text of the actual label.
     */
    public LabelField(String fieldLabel, String labelText) {
        if (fieldLabel == null) {
            fieldLabel = "";
        }
        label = new JLabel(labelText);
        font = label.getFont();
        color = LookAndFeelManager.getLafColor("Label.foreground", Color.BLACK);
        this.fieldLabel = new JLabel(fieldLabel);
        this.fieldLabel.setFont(fieldLabelFont);
        fieldComponent = label;
        showValidationLabel = false;
    }

    /**
     * A static convenience factory method to create a bold header label with sensible
     * defaults for a section header label. The default values are 16 point bold black
     * text with a slightly larger top margin and normal bottom margin.
     *
     * @param text The label text
     * @return A LabelField suitable for use as a header.
     */
    public static LabelField createBoldHeaderLabel(String text) {
        return createHeaderLabel(text, DEFAULT_HEADER_FONT, extraTopMarginHeader, extraBottomMarginHeader);
    }

    public static LabelField createBoldHeaderLabel(String text, int fontSize) {
        return createHeaderLabel(text, DEFAULT_HEADER_FONT.deriveFont((float)fontSize), extraTopMarginHeader,
                                 extraBottomMarginHeader);
    }

    /**
     * A static convenience factory method to create a "normal" header label with sensible
     * defaults for a form label. The default values are 12 point plain black text
     * with top and bottom margin values read from our current margin properties.
     * <p>
     *     You can control the extra top and bottom margin for generated header
     *     labels by invoking LabelField.setHeaderLabelExtraMargins() before
     *     invoking this method.
     * </p>
     *
     * @param text The label text
     * @return A LabelField suitable for use as a regular header label.
     */
    public static LabelField createPlainHeaderLabel(String text) {
        return createHeaderLabel(text, DEFAULT_LABEL_FONT, extraTopMarginNormal, extraBottomMarginNormal);
    }

    /**
     * A static convenience factory method to create a "normal" header label the
     * given font size and with top and bottom margin values read from our current
     * margin properties.
     * <p>
     * You can control the extra top and bottom margin for generated header
     * labels by invoking LabelField.setHeaderLabelExtraMargins() before
     * invoking this method.
     * </p>
     *
     * @param text The label text
     * @return A LabelField suitable for use as a regular header label.
     */
    public static LabelField createPlainHeaderLabel(String text, int fontSize) {
        return createHeaderLabel(text, DEFAULT_LABEL_FONT.deriveFont((float)fontSize), extraTopMarginNormal,
                                 extraBottomMarginNormal);
    }

    /**
     * Sets the extra top and bottom margin values that we use in this class when create*HeaderLabel
     * convenience methods are invoked.
     *
     * @param normalTop    An extra pixel margin to apply above non-bolded header labels.
     * @param normalBottom An extra pixel margin to apply below non-bolded header labels.
     * @param headerTop    An extra pixel margin to apply above bold header labels.
     * @param headerBottom An extra pixel margin to apply below bold header labels.
     */
    public static void setHeaderLabelExtraMargins(int normalTop, int normalBottom, int headerTop, int headerBottom) {
        extraTopMarginNormal = Math.max(normalTop, 0);
        extraBottomMarginNormal = Math.max(normalBottom, 0);
        extraTopMarginHeader = Math.max(headerTop, 0);
        extraBottomMarginHeader = Math.max(headerBottom, 0);
    }

    public static int getExtraTopMarginNormal() {
        return extraTopMarginNormal;
    }

    public static int getExtraBottomMarginNormal() {
        return extraBottomMarginNormal;
    }

    public static int getExtraTopMarginHeader() {
        return extraTopMarginHeader;
    }

    public static int getExtraBottomMarginHeader() {
        return extraBottomMarginHeader;
    }

    public static Font getDefaultHeaderFont() {
        return DEFAULT_HEADER_FONT.deriveFont((float)DEFAULT_HEADER_FONT.getSize());
    }

    public static Font getDefaultLabelFont() {
        return DEFAULT_LABEL_FONT.deriveFont((float)DEFAULT_LABEL_FONT.getSize());
    }

    /**
     * A static convenience factory method to create a header label with the
     * specified font and margin properties.
     *
     * @param text         The text to display.
     * @param font         The font.
     * @param topMargin    The top margin (default is 4).
     * @param bottomMargin The bottom margin (default is 4).
     * @return A header label with the specified properties.
     */
    public static LabelField createHeaderLabel(String text, Font font, int topMargin, int bottomMargin) {
        LabelField label = new LabelField(text);
        label.setFont(font);
        label.setTopMargin(topMargin);
        label.setBottomMargin(bottomMargin);
        return label;

    }

    /**
     * Reports whether this is a "header" label. That means the fieldLabel text
     * is blank or empty, so instead of a fieldLabel:labeltext pairing, we just
     * have a formwidth-spanning labeltext instead.
     *
     * @return true if this is a single bigass label instead of this:that style.
     */
    public boolean isHeaderLabel() {
        return fieldLabel.getText().isEmpty();
    }

    /**
     * Reports whether this label has been hyperlinked (see setHyperlink).
     *
     * @return true if this label contains a hyperlink.
     */
    public boolean isHyperlinked() {
        return hyperlinkAction != null;
    }

    /**
     * Converts the field label (if present) or the header label (if isHeaderLabel()) into
     * a hyperlink, by adding a custom mouse cursor and mouse listener with the given
     * ActionListener attached to the single click event. You can optionally use setFieldLabelFont()
     * to change the label colour and underline it.
     *
     * @param action The Action to fire when the label is clicked.
     */
    public void setHyperlink(Action action) {
        JLabel linkLabel = label; // whether header label or not, put the link on "label" and not "fieldLabel"
        linkLabel.setForeground(Color.BLUE);
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Map attributes = linkLabel.getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        linkLabel.setFont(linkLabel.getFont().deriveFont(attributes));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(new ActionEvent(linkLabel, 0, ""));
                fireValueChangedEvent(); // Also notify listeners that the action was triggered
            }
        });
    }

    /**
     * Removes any previously set hyperlink, if any.
     */
    public void clearHyperlink() {
        hyperlinkAction = null;
        if (isHeaderLabel()) {
            label.setForeground(color);
            label.setFont(font);
            label.setCursor(Cursor.getDefaultCursor());
        }
        else {
            fieldLabel.setForeground(LookAndFeelManager.getLafColor("Label.foreground", Color.BLACK));
            fieldLabel.setFont(fieldLabelFont);
            fieldLabel.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Returns the current label text.
     *
     * @return The text of the label.
     */
    public String getText() {
        return label.getText();
    }

    /**
     * Sets the label text.
     *
     * @param text The new label text.
     */
    public void setText(String text) {
        label.setText(text);
    }

    /**
     * Sets the font to use for the label text.
     * This is shorthand for ((JLabel)getFieldComponent()).setFont()
     *
     * @param font The new Font to use.
     */
    public void setFont(Font font) {
        this.font = font;
        label.setFont(font);
    }

    /**
     * Sets the colour for the label text.
     * This is shorthand for ((JLabel)getFieldComponent()).setForeground();
     *
     * @param c The new text colour.
     */
    public void setColor(Color c) {
        this.color = c;
        label.setForeground(c);
    }

    /**
     * Renders this label field into the given container.
     * This will either be in the format "(fieldLabel):(labelText)" in two columns,
     * or just "(labelText)" spanning the width of the form, if "fieldLabel" is blank.
     *
     * @param container   The container into which to render
     * @param constraints The GridBagConstraints to use.
     */
    @Override
    public void render(JPanel container, GridBagConstraints constraints) {
        constraints.gridy = constraints.gridy + 1;

        if (!isHeaderLabel()) {
            constraints.gridx = FormPanel.LABEL_COLUMN;
            constraints.gridwidth = 1;
            constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, componentSpacing);
            fieldLabel.setFont(fieldLabelFont);
            container.add(fieldLabel, constraints);
            constraints.gridx = FormPanel.CONTROL_COLUMN;
            constraints.insets = new Insets(topMargin, componentSpacing, bottomMargin, componentSpacing);
        }
        else {
            constraints.gridx = FormPanel.LABEL_COLUMN;
            constraints.gridwidth = 2;
            constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, componentSpacing);
        }

        label.setFont(font);
        container.add(label, constraints);
    }
}
