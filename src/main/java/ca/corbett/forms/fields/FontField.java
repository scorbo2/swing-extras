package ca.corbett.forms.fields;

import ca.corbett.forms.FontDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

/**
 * A FormField implementation that provides the ability to select a Font, with
 * optional abilities to select font size, font color, and font background color.
 * This form field presents options for choosing a font from any of the installed
 * fonts on the system, or by using the built-in Java Font abstraction names,
 * like "SansSerif" or "Monospaced", which will map to some system-installed font.
 * See also FontDialog, which is used by this FormField but which can also
 * be used standalone if you need to select a Font.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2025-04-07
 */
public final class FontField extends FormField {

    private final JLabel sampleLabel;
    private final JButton button;
    private final JPanel wrapperPanel;
    private ActionListener actionListener;
    private Font selectedFont;
    private Color textColor;
    private Color bgColor;
    private boolean showSizeField = true;

    /**
     * Creates a FontField with the given label text and default initial settings.
     * Text color and background color will not be editable.
     *
     * @param labelText The text to show for the field label.
     */
    public FontField(String labelText) {
        this(labelText, FontDialog.INITIAL_FONT, null, null);
    }

    /**
     * Creates a FontField with the given label text and the given initial Font.
     * Text color and background color will not be editable.
     *
     * @param labelText   The text to show for the field label.
     * @param initialFont The Font to set for the initial value.
     */
    public FontField(String labelText, Font initialFont) {
        this(labelText, initialFont, null, null);
    }

    /**
     * Creates a FontField with the given label text and the given initial Font,
     * and allows changing the text color but not the background color.
     *
     * @param labelText   The text to show for the field label.
     * @param initialFont The Font to set for the initial value.
     * @param textColor   The initial text color to set.
     */
    public FontField(String labelText, Font initialFont, Color textColor) {
        this(labelText, initialFont, textColor, null);
    }

    /**
     * Creates a FontField with the given label text and the given initial Font,
     * and allows changing both the text color and the background color.
     *
     * @param labelText   The text to show for the field label.
     * @param initialFont The Font to set for the initial value.
     * @param textColor   The initial text color to set.
     * @param bgColor     The initial background color to set.
     */
    public FontField(String labelText, Font initialFont, Color textColor, Color bgColor) {
        selectedFont = (initialFont == null) ? FontDialog.INITIAL_FONT : initialFont;
        this.textColor = textColor;
        this.bgColor = bgColor;
        fieldLabel.setText(labelText);
        button = new JButton("Change");
        button.setPreferredSize(new Dimension(95, 23));
        button.setFont(button.getFont().deriveFont(Font.PLAIN));
        wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        sampleLabel = new JLabel();
        sampleLabel.setOpaque(true);
        sampleLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        updateSampleLabel();
        fieldComponent = wrapperPanel;
        wrapperPanel.add(sampleLabel);
        wrapperPanel.add(button);
    }

    /**
     * Controls the visibility of the size chooser on the popup font chooser dialog.
     * This must be set before the field is rendered. Some use cases require
     * choosing just the font and not also the size.
     */
    public void setShowSizeField(boolean show) {
        showSizeField = show;
    }

    /**
     * Reports whether or not the size chooser is visible in this field.
     */
    public boolean isShowSizeField() {
        return showSizeField;
    }

    /**
     * Returns the Font selected by the user, or the default Font if the user
     * did not make a selection.
     *
     * @return The selected Font.
     */
    public Font getSelectedFont() {
        return selectedFont;
    }

    /**
     * Sets the selected Font. Overwrites whatever the user has chosen before now.
     *
     * @param font The Font to select.
     */
    public FontField setSelectedFont(Font font) {
        if (Objects.equals(selectedFont, font)) {
            return this; // don't accept no-op changes
        }
        selectedFont = font;
        updateSampleLabel();
        fireValueChangedEvent();
        return this;
    }

    /**
     * If text color modification is allowed, returns the selected text color,
     * otherwise null.
     *
     * @return A text color, or null if color editing not enabled.
     */
    public Color getTextColor() {
        return textColor;
    }

    /**
     * Sets the selected text color, if text color is editable in this field,
     * otherwise does nothing.
     *
     * @param textColor The new text color (overrides any previous selection).
     */
    public FontField setTextColor(Color textColor) {
        if (Objects.equals(this.textColor, textColor)) {
            return this; // reject no-op changes
        }
        if (this.textColor == null) {
            return this; // reject null
        }
        this.textColor = textColor;
        updateSampleLabel();
        fireValueChangedEvent();
        return this;
    }

    /**
     * If background color modification is allowed, returns the selected text color,
     * otherwise null.
     *
     * @return A text color, or null if color editing not enabled.
     */
    public Color getBgColor() {
        return bgColor;
    }

    /**
     * Sets the selected background color, if bg color is editable in this field,
     * otherwise does nothing.
     *
     * @param bgColor The new background color (overrides any previous selection).
     */
    public FontField setBgColor(Color bgColor) {
        if (Objects.equals(this.bgColor, bgColor)) {
            return this; // reject no-op changes
        }
        if (this.bgColor == null) {
            return this; // reject null
        }
        this.bgColor = bgColor;
        updateSampleLabel();
        fireValueChangedEvent();
        return this;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled);
    }

    @Override
    public void preRender(JPanel container) {
        wrapperPanel.setBackground(container.getBackground());

        // Remove old action listener:
        if (actionListener != null) {
            button.removeActionListener(actionListener);
        }

        // Now create and add a new one - it needs the container to use as a parent for the popup:
        actionListener = getActionListener(container);
        button.addActionListener(actionListener);
    }

    /**
     * Invoked internally to update the sample label when changes are made to
     * any of our font or color properties.
     */
    private void updateSampleLabel() {
        sampleLabel.setFont(selectedFont.deriveFont(12f));
        sampleLabel.setText(trimFontName(selectedFont.getFamily()));
        if (textColor != null) {
            sampleLabel.setForeground(textColor);
        }
        if (bgColor != null) {
            sampleLabel.setBackground(bgColor);
        }
    }

    /**
     * Creates and returns a new ActionListener suitable for our form field.
     *
     * @param panel The owning panel (used to position the popup dialog)
     * @return An ActionListener that can be attached to a button.
     */
    private ActionListener getActionListener(final JPanel panel) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FontDialog dialog = new FontDialog(panel, selectedFont, textColor, bgColor);
                dialog.setShowSizeField(showSizeField);
                dialog.setVisible(true);
                if (dialog.wasOkayed()) {
                    setSelectedFont(dialog.getSelectedFont());
                    setTextColor(dialog.getSelectedTextColor());
                    setBgColor(dialog.getSelectedBgColor());
                }
            }
        };
    }

    /**
     * Some fonts have egregiously long names, and we want to avoid our sample label getting
     * too long, so trim it and add an ellipses as needed.
     *
     * @param fontName Any font name
     * @return A possibly shortened version of the input font name
     */
    private String trimFontName(String fontName) {
        final int LIMIT = 18;
        return fontName.length() > LIMIT ? fontName.substring(0, LIMIT) + "..." : fontName;
    }
}
