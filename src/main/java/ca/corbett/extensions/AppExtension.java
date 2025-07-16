package ca.corbett.extensions;

import ca.corbett.extras.properties.AbstractProperty;

import java.util.ArrayList;
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
public abstract class AppExtension {

    protected final List<AbstractProperty> configProperties;

    public AppExtension() {
        List<AbstractProperty> props = createConfigProperties();
        configProperties = props == null ? new ArrayList<>() : props;
    }

    /**
     * Should return an AppExtensionInfo object that describes this extension.
     *
     * @return AppExtensionInfo
     */
    public abstract AppExtensionInfo getInfo();

    /**
     * Return a list of configuration properties for this extension. This list may be
     * empty if the extension has no config properties. This method is final to
     * prevent extensions from overriding its behaviour. The intention is to force
     * extensions to implement createConfigProperties() to create the list.
     * The createConfigProperties() method is guaranteed to only be invoked
     * by the ExtensionManager once, whereas getConfigProperties() can be invoked
     * any number of times. It's therefore best if extensions refrain from
     * creating their config property list in the getConfigProperties() method.
     *
     * @return A List of 0 or more configuration properties for this extension.
     */
    public final List<AbstractProperty> getConfigProperties() {
        return new ArrayList<>(configProperties);
    }

    /**
     * Hook invoked either as the application is starting up, or when the
     * extension is enabled within the application. Implementations should
     * ideally avoid doing costly operations here if possible.
     */
    public void onActivate() {
    }

    /**
     * Hook invoked either as the application is shutting down, or when the
     * extension is disabled within the application. Implementations can't
     * stop the shutdown, but they can respond to it by doing whatever
     * cleanup is needed.
     */
    public void onDeactivate() {
    }

    /**
     * Extensions must implement this method to create and return a list of
     * AbstractProperty instances representing the config for this extension.
     * It's fine to return null or an empty list if your extension does
     * not require any configuration.
     *
     * @return A List of AbstractProperty instance. May be null or empty.
     */
    protected abstract List<AbstractProperty> createConfigProperties();
}
