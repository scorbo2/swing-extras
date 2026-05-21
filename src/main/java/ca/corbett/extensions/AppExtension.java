package ca.corbett.extensions;

import ca.corbett.extras.properties.AbstractProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a generic starting point for application extensions.
 * Some basic hooks are provided here, but the intention is that
 * application-specific implementations will add their own hooks
 * to be invoked by the application in question.
 * <p>
 * Refer to the <a href="http://www.corbett.ca/swing-extras-book/">swing-extras documentation</a> for more information.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public abstract class AppExtension {

    /**
     * List of configuration properties for this extension.
     * ExtensionManager will automatically populate this list for you, by invoking
     * your implementation of createConfigProperties() when the extension
     * is first instantiated.
     */
    protected List<AbstractProperty> configProperties;

    /**
     * Should return an AppExtensionInfo object that describes this extension.
     *
     * @return AppExtensionInfo
     */
    public abstract AppExtensionInfo getInfo();

    /**
     * Return a list of configuration properties for this extension. This list may be
     * empty if the extension has no config properties. This method is final to
     * prevent extensions from overriding its behavior. The intention is to force
     * extensions to implement createConfigProperties() to create the list.
     * The createConfigProperties() method is guaranteed to only be invoked
     * by the ExtensionManager once, whereas getConfigProperties() can be invoked
     * any number of times. It's therefore best if extensions refrain from
     * creating their config property list in the getConfigProperties() method.
     *
     * @return A List of 0 or more configuration properties for this extension. Guaranteed not to be null.
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
     * <p>
     * Note that this method is invoked (exactly once) by ExtensionManager, <b>after</b> the
     * <code>loadJarResources</code> method is invoked. So, if your extension's config properties
     * depend on resources that need to be loaded from the extension's jar file,
     * you can safely reference those loaded resources in your implementation of this method.
     * </p>
     *
     * @return A List of AbstractProperty instance. May be null or empty.
     */
    protected abstract List<AbstractProperty> createConfigProperties();

    /**
     * This method is invoked exactly once when an extension is loaded.
     * For internal extensions, this is largely irrelevant, since resources are loaded
     * from the application's jar file and can be done from anywhere in the extension.
     * But, if your extension is externally loaded, it is important to note that the URLClassLoader
     * that loads your extension will be closed by ExtensionManager after your extension is initialized!
     * If your extension has resources (images, sound effects, icons, text files,
     * config files, or any other resource type) that you wish it to load from its jar file
     * via class.getResource() or class.getResourceAsStream(), you MUST do it either in the extension
     * constructor or in this method. Attempting to load jar resources anywhere else in you
     * extension will fail, because the URLClassLoader that loads the extension is closed.
     * No default implementation is provided so that extensions are forced to implement
     * this method (even if empty, in the case of an extension with no resources to load).
     * <p>
     * This method is invoked by ExtensionManager immediately after the extension is instantiated, and before
     * the createConfigProperties() method is invoked. So, if your extension's config properties
     * depend on resources that need to be loaded from the extension's jar file, you can
     * safely load those resources in this method, and then reference the loaded resources
     * in your implementation of createConfigProperties().
     * </p>
     * <p>
     * <b>NOTE:</b> the extension's configProperties list has not yet been initialized! Don't try
     * to access it here. ExtensionManager will catch the NullPointerException and log a warning, so it
     * won't break the load, but it is bad form and should be avoided.
     * </p>
     */
    protected abstract void loadJarResources();
}
