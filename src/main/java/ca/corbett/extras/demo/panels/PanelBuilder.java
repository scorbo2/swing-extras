package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * Provides an easy abstract way to create new demo panels and load them into the DemoApp.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
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
     * Can be invoked internally to generate the start of a FormPanel
     * suitable for adding example FormFields and controls. This allows
     * the demo panels to have a consistent title header and margin.
     */
    protected FormPanel buildFormPanel(String title) {
        return buildFormPanel(title, 24);
    }

    /**
     * Can be invoked internally to generate the start of a FormPanel
     * suitable for adding example FormFields and controls. This allows
     * the demo panels to have a consistent title header and margin.
     *
     * @param title  An optional title for the header label in the form. Can be null for no header label.
     * @param margin The desired FormPanel margin.
     */
    protected FormPanel buildFormPanel(String title, int margin) {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(margin);
        if (title != null && !title.isBlank()) {
            formPanel.add(buildHighlightedHeaderLabel(title, 24));
        }
        return formPanel;
    }

    protected LabelField buildHighlightedHeaderLabel(String text, int pointSize) {
        LabelField label = LabelField.createBoldHeaderLabel(text, pointSize, 0, 8);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        return label;
    }
}
