package ca.corbett.extensions;

import ca.corbett.extras.properties.AbstractProperty;

import java.util.List;

/**
 * Provides a generic starting point for application extensions.
 * Some basic hooks are provided here, but the intention is that
 * application-specific implementations will add their own hooks
 * to be invoked by the application in question.
 *
 * @author scorbo2
 * @since 2023-11-11
 */
public interface AppExtension {

    /**
     * Should return an AppExtensionInfo object that describes this extension.
     *
     * @return AppExtensionInfo
     */
    AppExtensionInfo getInfo();

    /**
     * Return a list of configuration properties for this extension. An empty/null list is fine,
     * if the extension has no configuration properties.
     *
     * @return A List of 0 or more configuration properties for this extension, or null.
     */
    List<AbstractProperty> getConfigProperties();

    /**
     * Hook invoked either as the application is starting up, or when the
     * extension is enabled within the application. Implementations should
     * ideally avoid doing costly operations here if possible.
     */
    void onActivate();

    /**
     * Hook invoked either as the application is shutting down, or when the
     * extension is disabled within the application. Implementations can't
     * stop the shutdown, but they can respond to it by doing whatever
     * cleanup is needed.
     */
    void onDeactivate();

}
