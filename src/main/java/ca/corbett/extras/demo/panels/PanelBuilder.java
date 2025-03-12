package ca.corbett.extras.demo.panels;

import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Font;

/**
 * Provides an easy abstract way to create new demo panels and load them into the DemoApp.
 *
 * @author scorbo2
 * @since 2025-03-11
 */
public abstract class PanelBuilder {

    /**
     * Returns a name for this PanelBuilder.
     *
     * @return A hopefully descriptive name.
     */
    public abstract String getTitle();

    /**
     * Builds and returns the FormPanel (or technically any JPanel) for this builder.
     *
     * @return A populated JPanel (or FormPanel).
     */
    public abstract JPanel build();

    /**
     * A shorthand method for creating labels with a consistent margin and font size.
     *
     * @param text The text for the label.
     */
    protected LabelField createSimpleLabelField(String text) {
        LabelField field = new LabelField(text);
        field.setMargins(2, 2, 2, 2, 0);
        field.setExtraMargins(0, 0);
        field.setFieldLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return field;
    }
}
