package ca.corbett.extensions;

import ca.corbett.extensions.ui.ExtensionManagerDialog;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.FileBasedProperties;
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.forms.Alignment;
import ca.corbett.updates.UpdateSources;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class encapsulates a PropertiesManager and an ExtensionManager together into
 * one handy utility that client projects can use more easily than the old approach
 * of having the client projects maintain these things separately.
 * <p>
 * <b>Enabling and disabling</b><br>
 * Both this class and ExtensionManager have methods for enabling and disabling an
 * extension. But ExtensionManager knows nothing about this class. This class therefore
 * tries to automatically reconcile these status flags across the two classes, and
 * generally you shouldn't need to worry about it. If an extension is enabled or
 * disabled in ExtensionManager, this class will notice the change and will enable
 * or disable all extension properties as needed. This means that you shouldn't see
 * properties from disabled extensions in the PropertiesDialog, but the values for
 * them will still be saved and loaded to the propsFile. When you re-enable an
 * extension, either in this class or in ExtensionManager, then its properties will
 * once again show up in the PropertiesDialog.
 * <p>
 * <b>Loading and saving</b><br>
 * Unlike sc-util 1.8, there's nothing wonky that client apps need to do in order
 * to load AppProperties and Extensions. You can simply invoke load() in this class
 * and it will all just work. Any extensions that were disabled the last time you
 * invoked save() will be correctly loaded in a disabled state - this means that their
 * property values will be loaded correctly, but they won't show up in the PropertiesDialog
 * until the extension is enabled again.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2024-12-30
 */
public abstract class AppProperties<T extends AppExtension> {

    private static final Logger logger = Logger.getLogger(AppProperties.class.getName());

    protected PropertiesManager propsManager;
    protected final ExtensionManager<T> extManager;

    private final String appName;
    private final File propsFile;

    // Subclasses can change these as they see fit:
    protected int propertiesDialogInitialWidth = PropertiesDialog.INITIAL_WIDTH;
    protected int propertiesDialogInitialHeight = PropertiesDialog.INITIAL_HEIGHT;
    protected int propertiesDialogMinimumWidth = PropertiesDialog.MINIMUM_WIDTH;
    protected int propertiesDialogMinimumHeight = PropertiesDialog.MINIMUM_HEIGHT;


    /**
     * If your application has an ExtensionManager, you can supply it here and this
     * class will handle loading and saving properties for all enabled extensions.
     *
     * @param appName    The name of this application.
     * @param propsFile  A File in which properties will be stored for this application.
     * @param extManager An instance of your ExtensionManager implementation.
     */
    protected AppProperties(String appName, File propsFile, ExtensionManager<T> extManager) {
        this.appName = appName;
        this.propsFile = propsFile;
        this.extManager = extManager;
        reinitialize();
    }

    /**
     * Offers a peek directly into the given props file without going through the usual loading mechanism.
     * This allows direct access to properties (in String form only) exactly as they currently
     * exist in the given props file. This can be useful in rare cases where an extension needs to know
     * a property value in order to initialize some other property value. The normal load mechanism prevents
     * this because property values cannot be read until the AppProperties instance is fully initialized,
     * leading to a circular problem.
     * <p>
     * If the value does not exist or an error occurs while reading the props file, empty string is returned.
     * </p>
     *
     * @param propsFile The properties file to read.
     * @param propName  The fully qualified name of the property in question.
     * @return The raw value in String form as it exists in the props file at the time of this call. May be empty.
     */
    public static String peek(File propsFile, String propName) {
        String result = "";
        try {
            FileBasedProperties tempProps = new FileBasedProperties(propsFile);
            tempProps.load();
            result = tempProps.getString(propName, result);
        }
        catch (IOException ioe) {
            logger.log(Level.WARNING, "AppProperties.peek(): encountered IOException: " + ioe.getMessage(), ioe);
        }
        return result;
    }

    /**
     * If you want to take some action after props are loaded (for example, to set window
     * dimensions or other ui state), you can override this method and put your updates
     * AFTER you invoke super load().
     */
    public void load() {
        try {
            propsManager.load();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Exception loading application properties: " + e.getMessage(), e);
        }

        // Now that we have loaded all props, figure out which extensions should be enabled/disabled:
        for (T extension : extManager.getAllLoadedExtensions()) {
            // Note we don't call isExtensionEnabled here because this is the one spot where we
            // don't care what ExtensionManager has to say on the subject... we only care
            // if the extension is disabled in our properties list, and we'll tell
            // ExtensionManager whether or not it's enabled.
            boolean isEnabled = propsManager.getPropertiesInstance()
                                            .getBoolean("extension.enabled." + extension.getClass().getName(), true);
            logger.fine(
                    "AppProperties.load(): extension \"" + extension.getInfo().name + "\" is enabled: " + isEnabled);
            extManager.setExtensionEnabled(extension.getClass().getName(), isEnabled, false);

            // Also enable or disable any properties for this extension:
            List<AbstractProperty> disabledProps = extension.getConfigProperties();
            for (AbstractProperty prop : disabledProps) {
                if (propsManager.getProperty(prop.getFullyQualifiedName()) != null) {
                    propsManager.getProperty(prop.getFullyQualifiedName()).setEnabled(isEnabled);
                }
            }
        }
    }

    /**
     * If you have specific properties you wish to set on save (window dimensions or other
     * variable stuff), you can override this method and invoke super save() AFTER you have
     * updated your property values.
     */
    public void save() {
        reconcileExtensionEnabledStatus();
        propsManager.save();
    }

    /**
     * Allows direct access to the underlying PropertiesManager, in case you need to interact
     * directly with it (for example, to invoke generateUnrenderedFormPanels()).
     *
     * @return The underlying PropertiesManager instance.
     */
    public PropertiesManager getPropertiesManager() {
        return propsManager;
    }

    /**
     * Generates and shows a PropertiesDialog to allow the user to view or change any
     * of the current properties. If the user okays the dialog, changes are automatically saved.
     *
     * @param owner The owning Frame (so we can make the dialog modal to that Frame).
     * @return true if the user OK'd the dialog and changes were made - reload your UI!
     */
    public boolean showPropertiesDialog(Frame owner) {
        return showPropertiesDialog(owner, Alignment.TOP_LEFT);
    }

    /**
     * Generates and shows a PropertiesDialog to allow the user to view or change any
     * of the current properties. If the user okays the dialog, changes are automatically saved.
     *
     * @param owner     The owning Frame (so we can make the dialog modal to that Frame).
     * @param alignment How the FormPanels should align themselves.
     * @return true if the user OK'd the dialog and changes were made - reload your UI!
     */
    public boolean showPropertiesDialog(Frame owner, Alignment alignment) {
        reconcileExtensionEnabledStatus();
        PropertiesDialog dialog = propsManager.generateDialog(owner, appName + " properties", alignment, 24);
        dialog.setSize(propertiesDialogInitialWidth, propertiesDialogInitialHeight);
        dialog.setMinimumSize(new Dimension(propertiesDialogMinimumWidth, propertiesDialogMinimumHeight));
        dialog.setVisible(true);

        if (dialog.wasOkayed()) {
            save();
        }

        return dialog.wasOkayed();
    }

    /**
     * Generates and shows an ExtensionManagerDialog to allow the user to view all
     * currently loaded extensions, and to enable or disable them.
     * <p>
     *     Note: dynamic extension discovery and download will be disabled and hidden.
     *     Use showExtensionDialog(Window, UpdateSources) instead if you want this feature.
     * </p>
     *
     * @param owner The owning Frame (so we can make the dialog modal to that Frame).
     * @return true if the user OK'd the dialog and changes were made - reload your UI!
     */
    public boolean showExtensionDialog(Window owner) {
        return showExtensionDialog(owner, null);
    }

    /**
     * Generates and shows an ExtensionManagerDialog to allow the user to view
     * all currently loaded extensions, and to enable or disable them. Additionally,
     * the given UpdateSources can be queried to find and show a list of extensions
     * available for download. The user can download new extensions or update
     * existing ones using the "available" tab on the dialog.
     */
    public boolean showExtensionDialog(Window owner, UpdateSources updateSources) {
        ExtensionManagerDialog<T> dialog = new ExtensionManagerDialog<>(extManager, owner, updateSources);
        dialog.setVisible(true);
        if (dialog.wasOkayed() && dialog.wasModified()) {
            save();
        }
        return dialog.wasOkayed() && dialog.wasModified();
    }

    /**
     * Reports whether the named extension is currently enabled. If no such extension is found,
     * this will return whatever defaultValue you specify. Note: this is shorthand for
     * propsManager.getPropertiesInstance().getBoolean("extension.enabled."+extName, defaultValue);
     * <p>
     * Note: enabled status is stored in the ExtensionManager as well as here. In the case
     * of a discrepancy, the ExtensionManager will be considered the source of truth. That means
     * that this method might have the side effect of enabling/disabling an extension here
     * in AppProperties if we check and find that ExtensionManager's answer doesn't match ours.
     *
     * @param extName      The class name of the extension to check.
     * @param defaultValue A value to return if the status can't be found.
     * @return Whether the named extension is enabled.
     */
    public boolean isExtensionEnabled(String extName, boolean defaultValue) {
        boolean enabledInProps = propsManager.getPropertiesInstance()
                                             .getBoolean("extension.enabled." + extName, defaultValue);

        boolean isActuallyEnabled = extManager.isExtensionEnabled(extName);

        // If extManager has a different opinion than we do, update ourselves:
        if (enabledInProps != isActuallyEnabled) {
            propsManager.getPropertiesInstance().setBoolean("extension.enabled." + extName, isActuallyEnabled);
            enabledInProps = isActuallyEnabled;
        }

        return enabledInProps;
    }

    /**
     * Enables or disables the specified extension. We will also update ExtensionManager,
     * if we have one.
     *
     * @param extName The class name of the extension to enable/disable
     * @param value   The new enabled status for that extension.
     */
    public void setExtensionEnabled(String extName, boolean value) {
        propsManager.getPropertiesInstance().setBoolean("extension.enabled." + extName, value);

        // Also notify ExtensionManager about this change:
        extManager.setExtensionEnabled(extName, value);
    }

    /**
     * Override this to specify whatever properties your application needs. This method
     * will be invoked automatically upon creation.
     *
     * @return A List of zero or more AbstractProperty instances.
     */
    protected abstract List<AbstractProperty> createInternalProperties();

    /**
     * Reinitializes the underlying PropertiesManager instance from scratch. This means
     * both invoking our abstract createInternalProperties() method and also interrogating
     * our ExtensionManager to get a list of all extension-supplied properties. This method
     * is invoked automatically on initial creation, but you can invoke it again later
     * if you have manually added, removed, enabled, or disabled extensions, so that the
     * list of properties is fully reinitialized to reflect the new state of things.
     * <p>
     * For an example of why you might want to do this, consider an extension that
     * supplies a bunch of options, some of which may appear as selectable
     * options inside our own createInternalProperties(). For example, we have
     * a dropdown of available application themes, but we only have one or two
     * built-in themes. The list of additional themes is extension-supplied.
     * If we disable that extension at runtime, we want to regenerate our combo
     * box (which is created in createInternalProperties()) such that it no longer
     * shows the extension-supplied options. Similarly, when we re-enable that
     * extension later, we need to regenerate that combo box again so that the
     * list of extension-supplied themes once again appear as selectable options.
     * </p>
     * <p>
     * Basically, whenever the list of currently loaded and extensions changes,
     * it's a good idea to reinitialize() this class to reflect those changes.
     * (And, of course, to reload your UI, as there may be many other changes
     * throughout your app as a result of enabling or disabling extensions).
     * </p>
     * <p>
     *     Note this method will end by invoking load() again to pick up
     *     whatever values were previously persisted.
     * </p>
     */
    public void reinitialize() {
        List<AbstractProperty> props = new ArrayList<>(createInternalProperties());

        // The name of this method is misleading, because ALL extensions are enabled by default.
        // But that's okay. We'll load all properties for all extensions, and then the load()
        // method can handling disabling extensions and hiding properties for those extensions.
        props.addAll(extManager.getAllEnabledExtensionProperties());

        propsManager = new PropertiesManager(propsFile, props, appName + " application properties");

        // Now force a load() so we can override the default values we just
        // created in createInternalProperties() above with whatever values
        // the user has in the saved properties file:
        load();
    }

    /**
     * Invoked internally to reconcile the extension enabled status between our managed
     * properties list and our ExtensionManager, if we have one. These can get out of sync
     * if the ExtensionManager enables or disables an extension. There's currently no way
     * to "push" such changes from ExtensionManager to this class, because ExtensionManager
     * doesn't know that we exist. So, before we do anything that requires us to know
     * about extensions being enabled or not, we have to "pull" the statuses to ensure
     * that our managed list is up to date with what's specified in ExtensionManager.
     * <p>
     * Side note: in your app, if you need to enable or disable an extension, you can either
     * do it using ExtensionManager.setExtensionEnabled or via the setExtensionEnabled
     * method in this class. Either way, this class will keep itself in sync with ExtensionManager.
     */
    private void reconcileExtensionEnabledStatus() {
        // Loop through all extensions and use our isExtensionEnabled method to
        // pull the current enabled stats from extManager. Yeah, methods starting
        // with "is" probably shouldn't have side effects like this, but meh.
        for (T extension : extManager.getAllLoadedExtensions()) {
            boolean isEnabled = isExtensionEnabled(extension.getClass().getName(), false);

            // Also set the enabled status of each extension property:
            List<AbstractProperty> props = extension.getConfigProperties();
            if (props == null) {
                continue;
            }
            for (AbstractProperty prop : props) {
                if (propsManager.getProperty(prop.getFullyQualifiedName()) != null) {
                    propsManager.getProperty(prop.getFullyQualifiedName()).setEnabled(isEnabled);
                }
            }
        }
    }
}
