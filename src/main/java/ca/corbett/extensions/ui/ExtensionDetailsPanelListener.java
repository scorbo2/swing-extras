package ca.corbett.extensions.ui;

/**
 * Allows client code to listen for enable/disable checkbox events on
 * an ExtensionDetailsPanel.
 *
 * @author scorbo2
 */
public interface ExtensionDetailsPanelListener {

    void extensionEnabled(ExtensionDetailsPanel source, String className);

    void extensionDisabled(ExtensionDetailsPanel source, String className);
}
