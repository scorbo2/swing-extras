package ca.corbett.forms.fields;

import ca.corbett.forms.FontDialog;
import ca.corbett.forms.FormPanel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Intended as a proper replacement for FontStyleField, which I'm not happy with.
 * This form field presents options for choosing a font from any of the installed
 * fonts on the system, along with font style and size properties.
 * Makes use of the new FontDialog for this purpose.
 *
 * @author scorbo2
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
        fieldLabel = new JLabel(labelText);
        button = new JButton("Change");
        button.setPreferredSize(new Dimension(95, 23));
        button.setFont(button.getFont().deriveFont(Font.PLAIN));
        wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.X_AXIS));
        sampleLabel = new JLabel();
        sampleLabel.setOpaque(true);
        sampleLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setSelectedFont(selectedFont);
        fieldComponent = button;
        wrapperPanel.add(sampleLabel);
        wrapperPanel.add(new JLabel(" ")); // spacer
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
    public void setSelectedFont(Font font) {
        selectedFont = font;
        updateSampleLabel();
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
    public void setTextColor(Color textColor) {
        if (this.textColor == null) {
            return;
        }
        this.textColor = textColor;
        updateSampleLabel();
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
    public void setBgColor(Color bgColor) {
        if (this.bgColor == null) {
            return;
        }
        this.bgColor = bgColor;
        updateSampleLabel();
    }

    @Override
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
        wrapperPanel.setVisible(isVisible);
    }

    /**
     * Renders this field into the given container.
     *
     * @param container   The containing form panel.
     * @param constraints The GridBagConstraints to use.
     */
    @Override
    public void render(JPanel container, GridBagConstraints constraints) {
        constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, componentSpacing);
        constraints.gridy++;
        constraints.gridx = FormPanel.LABEL_COLUMN;
        fieldLabel.setFont(fieldLabelFont);
        container.add(fieldLabel, constraints);

        constraints.gridx = FormPanel.CONTROL_COLUMN;
        wrapperPanel.setBackground(container.getBackground());
        if (actionListener != null) {
            button.removeActionListener(actionListener);
        }
        actionListener = getActionListener(container);
        button.addActionListener(actionListener); // UTIL-147 avoid adding it twice

        constraints.fill = 0;
        constraints.insets = new Insets(topMargin, componentSpacing, bottomMargin, componentSpacing);
        container.add(wrapperPanel, constraints);
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
                    fireValueChangedEvent();
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
