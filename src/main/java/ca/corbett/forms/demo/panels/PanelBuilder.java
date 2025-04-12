package ca.corbett.forms.demo.panels;

import javax.swing.JPanel;

/**
 * Provides an easy abstract way to create new demo panels and load them into the DemoFrame.
 *
 * @author scorbo2
 * @since 2029-11-25
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

}
