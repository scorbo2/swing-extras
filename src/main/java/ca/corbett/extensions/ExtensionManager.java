package ca.corbett.extensions;

import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.properties.AbstractProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a mechanism for scanning for and loading instances of AppExtension
 * for any given application. Extend this class with your specific implementation type
 * of AppExtension and then point it to a directory containing jar files that
 * provide implementations of that class.
 * <p>
 * Your extension should bundle all required code and resources into a jar file,
 * and include an extInfo.json file in the jar resources. We scan for that
 * file in this class and parse it out to see if a) it exists, and b) if the
 * version requirements in the candidate extension jar are met. See extractExtInfo
 * for more details.
 * </p>
 * <p>
 * Refer to the <a href="http://www.corbett.ca/swing-extras-book/">swing-extras documentation</a> for more information.
 * </p>
 *
 * @param <T> Any class that implements AppExtension - this is the extension class we'll scan for.
 *            Your application could theoretically support more than one extension class type, but you
 *            would have to have a separate ExtensionManager derived class for each extension type.
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public abstract class ExtensionManager<T extends AppExtension> {

    protected static final Logger logger = Logger.getLogger(ExtensionManager.class.getName());

    private final String LOAD_ORDER_FILE = "ext-load-order.txt";

    private final Map<String, ExtensionWrapper> loadedExtensions;
    private final List<StartupError> startupErrors;

    private String applicationName;
    private String applicationVersion;
    private File extensionsDirectory;

    public ExtensionManager() {
        loadedExtensions = new HashMap<>();
        startupErrors = new ArrayList<>();
    }

    /**
     * Reports how many extensions have been loaded.
     *
     * @return A count of loaded extensions (enabled or not).
     */
    public int getLoadedExtensionCount() {
        return loadedExtensions.size();
    }

    /**
     * After loadExtensions is invoked, this method will return a list of
     * any StartupErrors that were detected (jar files that failed to load).
     */
    public List<StartupError> getStartupErrors() {
        return new ArrayList<>(startupErrors);
    }

    /**
     * Applications will typically have a single directory where they store all extension jars,
     * but this is not a hard requirement. This method returns the last directory that
     * was given to loadExtensions(), or whatever directory was last given to
     * setExtensionsDirectory(), whichever occurred more recently. If your application supports
     * scanning multiple extension directories, you should probably manage that in your
     * application code rather than relying on this method.
     */
    public File getExtensionsDirectory() {
        return extensionsDirectory;
    }

    /**
     * Sets the directory where the application wishes to store extension jars.
     * This is implicitly overwritten on every call to loadExtensions().
     */
    public void setExtensionsDirectory(File extensionsDirectory) {
        this.extensionsDirectory = extensionsDirectory;
    }

    /**
     * Returns the application name as it was supplied to the loadExtensions method,
     * or null if loadExtensions has not yet been invoked.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Returns the application version as it was supplied to the loadExtensions method,
     * or null if loadExtensions has not yet been invoked.
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * Reports whether an extension with the given class name is currently loaded.
     * Note you can also do getLoadedExtension(className) and check for null.
     * Even if this method returns true, the extension might not be enabled!
     * Use isExtensionEnabled() to determine if the extension is actually enabled.
     *
     * @param className The fully qualified class name of the extension to look for.
     * @return True if the named extension has been loaded by this manager.
     */
    public boolean isExtensionLoaded(String className) {
        return loadedExtensions.get(className) != null;
    }

    /**
     * Reports whether the loaded extension with the given class name is enabled.
     *
     * @param className the fully qualified class name of the extension in question.
     * @return True if the extension is enabled, false if disabled or not found.
     */
    public boolean isExtensionEnabled(String className) {
        ExtensionWrapper wrapper = loadedExtensions.get(className);
        return wrapper != null && wrapper.isEnabled;
    }

    /**
     * Enables or disables the named extension - this involves sending an onActivate or
     * onDeactivate message to the extension as needed. If the given isEnabled value
     * matches the existing value, no action is taken. If the given extension cannot
     * be found, no action is taken.
     *
     * @param className The fully qualified class name of the extension in question.
     * @param isEnabled Whether to enable or disable the extension.
     */
    public void setExtensionEnabled(String className, boolean isEnabled) {
        setExtensionEnabled(className, isEnabled, true);
    }

    /**
     * Enables or disabled the named extension - if notify is true, will send the extension
     * an onActivate or onDeactivate message as needed. If the given isEnabled value
     * matches the existing value, no action is taken. If the given extension cannot
     * be found, no action is taken.
     *
     * @param className The fully qualified class name of the extension in question.
     * @param isEnabled Whether to enable or disable the extension.
     * @param notify    Whether to send a message to the extension notifying them of the change.
     */
    public void setExtensionEnabled(String className, boolean isEnabled, boolean notify) {
        ExtensionWrapper wrapper = loadedExtensions.get(className);
        if (wrapper != null) {
            // Toggling to enabled?
            if (isEnabled && !wrapper.isEnabled) {
                wrapper.isEnabled = true;
                if (notify) {
                    // Notify the extension that it has been enabled:
                    wrapper.extension.onActivate();
                }
            }

            // Toggling to disabled?
            if (!isEnabled && wrapper.isEnabled) {
                wrapper.isEnabled = false;
                if (notify) {
                    // Notify the extension that it has been disabled:
                    wrapper.extension.onDeactivate();
                }
            }
        }
    }

    /**
     * Returns the source jar from which the given extension was loaded, or null if this
     * extension was added manually with addExtension().
     *
     * @param className the fully qualified class name of the extension in question.
     * @return The File representing the extensions source jar, or null if not found or built-in.
     */
    public File getSourceJar(String className) {
        ExtensionWrapper wrapper = loadedExtensions.get(className);
        return wrapper != null ? wrapper.sourceJar : null;
    }

    /**
     * Returns the extension instance for the given class name, if one exists.
     *
     * @param className the fully qualified class name of the extension in question.
     * @return The extension instance, or null if not found.
     */
    public T getLoadedExtension(String className) {
        ExtensionWrapper wrapper = loadedExtensions.get(className);
        return wrapper != null ? wrapper.extension : null;
    }

    /**
     * Searches for any loaded extension with a matching name (case-sensitive) and returns
     * the first match. Will return null if no extensions are loaded or no match is found.
     *
     * @param name The extension name (as reported by {@code extension.getInfo().getName()}).
     * @return The extension instance, or null if not found.
     */
    public T findExtensionByName(String name) {
        for (String key : loadedExtensions.keySet()) {
            ExtensionWrapper wrapper = loadedExtensions.get(key);
            if (wrapper.extension != null
                    && wrapper.extension.getInfo() != null
                    && wrapper.extension.getInfo().getName() != null
                    && wrapper.extension.getInfo().getName().equals(name)) {
                return wrapper.extension;
            }
        }
        return null;
    }

    /**
     * Similar to findExtensionByName, but will specifically return the Jar file from which
     * the given extension was loaded, assuming it was loaded from a jar file.
     * If the named extension is not found, null is returned.
     * <p>
     * <b>NOTE:</b> Built-in extensions will return null! This does not mean that the extension
     * in question does not exist or was not found. We return null because
     * built-in extensions were not loaded from a jar file, so there's nothing to return here.
     * To test for the existence of an extension in this ExtensionManager, it is far safer
     * to use isExtensionLoaded() or isExtensionEnabled() instead.
     * </p>
     */
    public File findExtensionJarByExtensionName(String name) {
        for (String key : loadedExtensions.keySet()) {
            ExtensionWrapper wrapper = loadedExtensions.get(key);
            if (wrapper.extension != null
                    && wrapper.extension.getInfo() != null
                    && wrapper.extension.getInfo().getName() != null
                    && wrapper.extension.getInfo().getName().equals(name)) {
                return wrapper.sourceJar;
            }
        }
        return null;
    }

    /**
     * Returns a list of all loaded extensions - beware that this method will return
     * extensions even if they are marked as disabled! If you only want to get the
     * extensions that are currently enabled, use getEnabledLoadedExtensions() instead.
     * The list is sorted by extension name (not by class name).
     *
     * @return A List of zero or more extensions sorted by extension name.
     */
    public List<T> getAllLoadedExtensions() {
        List<ExtensionWrapper> wrapperList = getAllLoadedExtensionWrappers();
        List<T> extList = new ArrayList<>();
        for (ExtensionWrapper wrapper : wrapperList) {
            extList.add(wrapper.extension);
        }
        return extList;
    }

    /**
     * Returns a list of all loaded extensions that are marked as enabled.
     * This list is computed each time this method is called as extensions can
     * be enabled and disabled at pretty much any point.
     * The list is sorted by extension name (not by class name).
     *
     * @return A List of zero or more enabled and loaded extensions sorted by extension name.
     */
    public List<T> getEnabledLoadedExtensions() {
        List<ExtensionWrapper> wrapperList = getAllLoadedExtensionWrappers();
        List<T> extList = new ArrayList<>();
        for (ExtensionWrapper wrapper : wrapperList) {
            if (wrapper.isEnabled) {
                extList.add(wrapper.extension);
            }
        }
        return extList;
    }

    /**
     * Invoke this to interrogate each enabled extension for their config properties, if any,
     * and return them in a list. The properties are grouped together into a single list
     * that is ordered first by extension name (not class name), and then by the order
     * in which the properties were defined within each extension.
     * <p>
     * <b>Note:</b> If two or more extensions define a property with the same fully-qualified
     * name, only the first one found will be returned. This is to avoid conflicts
     * that would arise from having multiple properties with the same name.
     * </p>
     *
     * @return The combined list of properties of all enabled extensions.
     */
    public List<AbstractProperty> getAllEnabledExtensionProperties() {
        List<AbstractProperty> propList = new ArrayList<>();
        List<ExtensionWrapper> wrapperList = getAllLoadedExtensionWrappers();
        for (ExtensionWrapper wrapper : wrapperList) {
            if (wrapper.extension == null || !wrapper.isEnabled) {
                continue;
            }
            List<AbstractProperty> list = wrapper.extension.getConfigProperties();
            if (!list.isEmpty()) { // AppExtension ensures it will never be null, but it may be empty
                logger.fine("ExtensionManager.getAllEnabledExtensionProperties(): extension \""
                                    + wrapper.extension.getInfo().name
                                    + "\" returned " + list.size() + " properties.");
                propList.addAll(list);
            }
            else {
                logger.fine("ExtensionManager.getAllEnabledExtensionProperties(): extension \""
                                    + wrapper.extension.getInfo().name + "\" had no config properties.");
            }
        }

        // Weed out duplicates based on fully qualified name:
        // (see https://github.com/scorbo2/swing-extras/issues/39 for gruesome details)
        Set<String> nonDuplicateIds = new HashSet<>(propList.size());
        List<AbstractProperty> nonDuplicateProps = new ArrayList<>(propList.size());
        for (AbstractProperty prop : propList) {
            String name = prop.getFullyQualifiedName();
            if (nonDuplicateIds.contains(name)) {
                logger.fine("ExtensionManager: ignoring duplicate extension config property \"" + name + "\"");
                continue;
            }
            nonDuplicateIds.add(name);
            nonDuplicateProps.add(prop);
        }

        return nonDuplicateProps;
    }

    /**
     * Programmatically adds an extension to our list - this was originally intended for testing
     * purposes, but might be useful as a way for applications to supply built-in extensions
     * without having to package them in separate jar files with the distribution. Remember that
     * extension load order matters! If more than one extension provides the same functionality,
     * it's up to the application to decide which extension's version of it should be used.
     * By convention, the first loaded extension that supplies a given piece of functionality is
     * the one that will be used. You can use the ext-load-order.txt file in your extension
     * jar directory to explicitly decide which extensions are loaded in which order, but
     * built-in extensions added through his mechanism bypass the ext-load-order check.
     * So, consider carefully if your built-in extensions should be loaded BEFORE externally-loaded
     * extensions (thereby giving them higher priority), or AFTER externally-loaded extensions
     * (thereby giving them lower priority). There is no "default" behavior here... it's
     * up to the application to decide whether to invoke addExtension() before
     * or after loadExtensions().
     * <p>
     * Note: The supplied extension will not receive an onActivate() notification from this method.
     * Use activateAll() to start up extensions.
     * </p>
     * <p>
     * Note: the same extension validation rules apply here as for externally-loaded extensions.
     * Your extension must supply a well-formed AppExtensionInfo instance containing extension
     * name, version, target app name, and target app version. If any of these are missing or invalid,
     * the extension is rejected and NOT loaded.
     * </p>
     *
     * @param extension The extension instance to be added.
     * @param isEnabled Whether to enable this extension immediately or not.
     * @return Whether the extension was successfully added.
     */
    public boolean addExtension(T extension, boolean isEnabled) {
        if (extension == null) {
            logger.warning("addExtension: null extension supplied; unable to load.");
            return false;
        }
        if (extension.getInfo() == null || !extension.getInfo().isValid()) {
            logger.warning("addExtension: rejecting extension because it does not have well-formed extension info.");
            return false;
        }
        ExtensionWrapper wrapper = new ExtensionWrapper();
        wrapper.sourceJar = null;
        wrapper.isEnabled = isEnabled;
        wrapper.extension = extension;
        List<AbstractProperty> configProperties = extension.createConfigProperties();
        extension.configProperties = configProperties == null ? new ArrayList<>() : configProperties;
        loadedExtensions.put(extension.getClass().getName(), wrapper);
        logger.info("Extension loaded internally: "
                            + extension.getInfo().name + " " + extension.getInfo().version);
        return true;
    }

    /**
     * Sends an onActivate() message to each enabled extension, to let them know that we're
     * starting up - use deactivateAll() to signal shutdown.
     */
    public void activateAll() {
        for (ExtensionWrapper wrapper : getAllLoadedExtensionWrappers()) {
            if (wrapper.extension == null) {
                continue;
            }
            if (wrapper.isEnabled) {
                wrapper.extension.onActivate();
            }
        }
    }

    /**
     * Sends an onDeactivate() message to each enabled extension, to let them know that we're
     * shutting down - use activateAll() to signal startup.
     */
    public void deactivateAll() {
        for (ExtensionWrapper wrapper : getAllLoadedExtensionWrappers()) {
            if (wrapper.extension == null) {
                continue;
            }
            if (wrapper.isEnabled) {
                wrapper.extension.onDeactivate();
            }
        }
    }

    /**
     * Removes all extensions that were previously loaded in this ExtensionManager,
     * and returns it to an empty state. Client applications
     * should use this with caution - if you have queried for configuration properties
     * from all extensions before invoking this, then you should once again invoke
     * getAllEnabledExtensionProperties() because the list of properties will
     * likely decrease as extensions are unloaded. Your UI should remove the properties
     * that no longer exist.
     *
     * @return The count of extensions that were actually removed as a result of this call.
     */
    public int unloadAllExtensions() {
        List<String> extensionClasses = new ArrayList<>(loadedExtensions.keySet());
        int removedCount = 0;
        for (String extensionClass : extensionClasses) {
            if (unloadExtension(extensionClass)) {
                removedCount++;
            }
        }
        return removedCount;
    }

    /**
     * Unregisters the given extension, if it was registered. The extension will receive a
     * deactivate() message before it is unloaded, if it was enabled. Client applications
     * should use this with caution - if you have queried for configuration properties
     * from all extensions before invoking this, then you should once again invoke
     * getAllEnabledExtensionProperties() because the list of properties will
     * likely decrease as extensions are unloaded. Your UI should remove the properties
     * that no longer exist.
     *
     * @param className The fully qualified class name of the extension to be unregistered.
     * @return true if an extension was actually removed as a result of this call.
     */
    public boolean unloadExtension(String className) {
        ExtensionWrapper wrapper = loadedExtensions.get(className);
        boolean removedSomething = false;
        if (wrapper != null) {
            if (wrapper.extension != null && wrapper.isEnabled) {
                wrapper.extension.onDeactivate();
            }
            loadedExtensions.remove(className);
            removedSomething = true;
        }
        return removedSomething;
    }

    /**
     * Scans the given directory looking for candidate jar files that contain an extension matching
     * the given parameters. For each jar that is found, an attempt will be made to load the
     * extension class out of that jar file. All successfully loaded extension classes will
     * then be loaded into this ExtensionManager.
     * <p>
     * Note that this is a shorthand way of doing this more manually (or jar by jar) via
     * the findCandidateExtensionJars, extractExtInfo, and jarFileMeetsRequirements methods.
     * Generally, this is the better entry point, but if you have a specific jar file that
     * you want to scan and load from, those other methods can be used instead.
     * </p>
     * <p>
     * A note about versioning: In swing-extras 2.5 and previous releases, the extension's
     * target application version must match the application's version exactly in order
     * for the extension to be considered a match. This was found to be too restrictive,
     * because each time an application releases a new version, all existing extensions
     * must be re-released even if they are perfectly compatible with the new version.
     * Starting in the 2.6 release of swing-extras, an extension will be considered compatible
     * if its target application version matches the major version of the application.
     * This requires applications to adopt a versioning convention where they release
     * a minor version only if there are no extension-breaking changes, otherwise they
     * must release a new major version. For example, if an application is at version 3.2, then
     * extensions targeting version 3.0, 3.1, 3.5, etc. will be considered compatible,
     * but extensions targeting version 2.x or 4.x will not be considered compatible.
     * </p>
     *
     * @param directory      The directory to scan. Must not be null.
     * @param extClass       The AppExtension implementation class to look for.
     * @param appName        The application name to match against, or null to skip this check.
     * @param requiredVersion The required app version that the extension must target.
     *                        (Only the major version is considered here). May be null to skip this check.
     * @return The count of extensions that were loaded by this operation.
     */
    public int loadExtensions(File directory, Class<T> extClass, String appName, String requiredVersion) {
        if (directory == null) {
            logger.warning("ExtensionManager.loadExtensions: given directory is null, no extensions loaded.");
            return 0;
        }
        if (!directory.exists() || !directory.isDirectory() || !directory.canRead()) {
            logger.warning("ExtensionManager.loadExtensions: given directory "
                                   + directory.getAbsolutePath()
                                   + " does not exist, is not a directory, or can't be read. No extensions loaded.");
            return 0;
        }

        // Make a note of these for later:
        this.applicationName = appName;
        this.applicationVersion = requiredVersion;
        this.extensionsDirectory = directory;

        Map<File, AppExtensionInfo> map = findCandidateExtensionJars(directory, appName, requiredVersion);
        if (map.isEmpty()) {
            return 0;
        }
        List<File> jarList = sortExtensionJarSet(directory, map.keySet());
        int extensionsLoaded = 0;
        for (File jarFile : jarList) {
            T extension = loadExtensionFromJar(jarFile, extClass);
            if (extension != null) {
                ExtensionWrapper wrapper = new ExtensionWrapper();
                wrapper.sourceJar = jarFile;
                wrapper.extension = extension;
                wrapper.isEnabled = true;
                loadedExtensions.put(extension.getClass().getName(), wrapper);
                extensionsLoaded++;
                logger.info("Extension loaded externally: "
                                    + extension.getInfo().name + " " + extension.getInfo().version);
            }
        }
        return extensionsLoaded;
    }

    /**
     * Scans the given directory looking for Jar files that contain an extInfo.json file, and
     * if one is found, will check its parameters against the given appName and requiredVersion
     * to make sure the extension would work for that application. The return is a Map of
     * File to AppExtensionInfo, which can then be loaded via one of the loadExtension methods.
     * Note that this method does not actually try to load the extension, it simply scans
     * to find which jar file would be good candidates for loading. This can therefore be
     * used for autodiscovery of extension jars in a given directory safely, without actually
     * loading them. Note that both appName and requiredVersion are optional (you can pass null
     * to disable those checks), but the result may be jar files that contain extensions
     * for the wrong app, or for the wrong version of the app, or both.
     *
     * @param directory      The directory to scan (will be scanned recursively).
     * @param appName        The application name to check for, or null to skip this check.
     * @param requiredVersion The required app version, or null to skip this check.
     * @return A Map of jar files to AppExtensionInfo objects.
     */
    public Map<File, AppExtensionInfo> findCandidateExtensionJars(File directory, String appName, String requiredVersion) {
        Map<File, AppExtensionInfo> map = new HashMap<>();
        if (directory == null) {
            logger.warning(
                    "ExtensionManager.findCandidateExtensionJars: given directory is null, no candidates found.");
            return map;
        }
        if (!directory.exists() || !directory.isDirectory() || !directory.canRead()) {
            logger.warning("ExtensionManager.findCandidateExtensionJars: given directory "
                                   + directory.getAbsolutePath()
                                   + " either does not exist, is not a directory, or can't be read. No candidates found.");
            return map;
        }

        // Start by finding all jar files in the target directory:
        List<File> jarFiles = FileSystemUtil.findFiles(directory, true, "jar");

        // Now try scanning for an extInfo.json file:
        for (File jarFile : jarFiles) {
            AppExtensionInfo extInfo = extractExtInfo(jarFile);
            if (extInfo == null) {
                continue;
            }
            if (jarFileMeetsRequirements(jarFile, extInfo, appName, requiredVersion)) {
                map.put(jarFile, extInfo);
            }
        }

        return map;
    }

    /**
     * Checks if the given jar file and extension info meet the given requirements (that is,
     * that the application name and version requirements are met). This does not guarantee
     * that an extension can be successfully loaded out of the given jar file, but it is
     * a pretty good indicator.
     *
     * @param jarFile        The jar file in question.
     * @param extInfo        The extension info that was extracted from that jar via extractExtInfo
     * @param appName        The name of the application to check for.
     * @param requiredVersion The app version that the extension must target. (Only the major version is considered)
     * @return true if the jar file looks good, false otherwise.
     */
    public boolean jarFileMeetsRequirements(File jarFile, AppExtensionInfo extInfo, String appName, String requiredVersion) {
        // Basic sanity checks:
        if (jarFile == null || extInfo == null) {
            logger.warning("jarFileMeetsRequirements: jarFile or extInfo is null. Ignoring request.");
            return false;
        }

        // Check app name if one was given:
        if (appName != null && !appName.equals(extInfo.getTargetAppName())) {
            addStartupError(jarFile, "jarFileMeetsRequirements: skipping jar "
                    + jarFile.getAbsolutePath()
                    + " because target app name "
                    + "\"" + extInfo.getTargetAppName() + "\""
                    + " does not match the given app name "
                    + "\"" + appName + "\"");
            return false;
        }

        // Version compatibility check:
        // Starting in swing-extras 2.6, we only check major version compatibility.
        // The versioning convention that applications must follow is that minor application version
        // changes do not break extensions, only major version changes do.
        // So, if the extension's "target app version" major version matches the application's major version,
        // then the extension is considered compatible.
        //
        // Don't get confused! We don't care about the extension's version, and in fact we never check
        // the extension's version at all. What we care about is the extension's target app version,
        // which is the version of the application that the extension was built to work with.
        //
        // We will only do this compatibility check here if we were given a requiredVersion that we can parse:
        int requiredMajorVersion = AppExtensionInfo.extractMajorVersion(requiredVersion);
        if (requiredMajorVersion != AppExtensionInfo.INVALID) { // This is not an error - if invalid, just skip the check

            // Extract the major version from the extension's target app version:
            int targetMajorVersion = AppExtensionInfo.extractMajorVersion(extInfo.getTargetAppVersion());
            if (targetMajorVersion == AppExtensionInfo.INVALID) { // This is an error - the extension was poorly assembled
                addStartupError(jarFile, "jarFileMeetsRequirements: unable to parse extension's target app version \""
                        + extInfo.getTargetAppVersion()
                        + "\" in jar file "
                        + jarFile.getAbsolutePath()
                        + "; skipping.");
                return false;
            }

            // At this point, we have both major versions, so compare them:
            if (targetMajorVersion < requiredMajorVersion) {
                addStartupError(jarFile, "jarFileMeetsRequirements: Jar file "
                        + jarFile.getAbsolutePath()
                        + " contains an older extension targeting app major version "
                        + targetMajorVersion
                        + ", below the required major version of "
                        + requiredMajorVersion
                        + "; skipping.");
                return false;
            }
            else if (targetMajorVersion > requiredMajorVersion) {
                addStartupError(jarFile, "jarFileMeetsRequirements: Jar file "
                        + jarFile.getAbsolutePath()
                        + " contains a newer extension targeting app major version "
                        + targetMajorVersion
                        + ", above the required major version of "
                        + requiredMajorVersion
                        + "; skipping.");
                return false;
            }

            // If we get here, the major versions match, but there's no guarantee the extension will actually load.
            // It could be that the application did not follow our versioning convention and introduced breaking changes
            // in a minor version change. In that case, we will rely on our error handling in the
            // loadExtensionFromJar() method to catch any problems.
            // A common example would be a ClassNotFoundException or NoSuchMethodException when trying to
            // load the extension class. Those errors will be trapped and logged appropriately.
        }

        return true;
    }

    /**
     * Scans the given jar file looking for any classes that match extensionClass. The first matching
     * class found will be loaded as an extension of type T and returned. Multiple extension
     * implementations in the same jar file are therefore not supported! If you have several
     * extensions to provide for an application, package each one into its own jar file.
     * (This is better practice anyway, as it allows you to independently version and release each one).
     *
     * @param jarFile        The jar file to scan.
     * @param extensionClass The implementing class to look for.
     * @return An implementation of T if one could be found and loaded, otherwise null.
     */
    @SuppressWarnings("unchecked")
    public T loadExtensionFromJar(File jarFile, Class<T> extensionClass) {
        if (jarFile == null) {
            logger.warning("ExtensionManager.loadExtensionFromJar: given jarFile is null, cannot load extension.");
            return null;
        }
        if (!jarFile.exists() || !jarFile.isFile() || !jarFile.canRead()) {
            logger.warning("ExtensionManager.loadExtensionFromJar: given jarFile "
                                   + jarFile.getAbsolutePath()
                                   + " either does not exist or cannot be read, cannot load extension.");
            return null;
        }

        try {
            try (JarFile jar = new JarFile(jarFile.getAbsolutePath())) {
                Enumeration<JarEntry> e = jar.entries();
                URL[] urls = {new URL("jar:file:" + jarFile.getAbsolutePath() + "!/")};

                // Note: try-with-resources means this class loader will be released immediately
                //       after the extension is instantiated! This is a deliberate design decision
                //       to avoid resource leaks and open file handles. But it also means that
                //       extensions have to be careful about loading jar resources. If your extension
                //       wishes to load resources from its own jar file, it MUST do so either in
                //       its constructor, or in the loadJarResources() method. As soon as this method
                //       returns, the class loader will be closed and any attempt to load resources
                //       from the jar file after that will fail.
                //
                //       see AppExtension.loadJarResources() for more info, and also refer to these
                //       issues for the thought process behind this decision:
                //         https://github.com/scorbo2/swing-extras/issues/126
                //         https://github.com/scorbo2/swing-extras/issues/133
                try (URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls)) {

                    T result = null;
                    while (e.hasMoreElements()) {
                        JarEntry je = e.nextElement();
                        if (je.isDirectory() || !je.getName().endsWith(".class")) {
                            continue;
                        }
                        // -6 because of .class
                        String className = je.getName().substring(0, je.getName().length() - 6);
                        className = className.replace('/', '.');

                        // Check to make sure we don't already have one with this class name:
                        if (getLoadedExtension(className) != null) {
                            logger.log(Level.INFO, "Skipping already loaded extension: {0}", className);
                            continue;
                        }

                        // Load this class:
                        logger.fine("ExtensionManager: loading class " + className + " from jar " + jarFile.getName());
                        Class<?> candidate = urlClassLoader.loadClass(className);

                        // What I want to do:
                        //    if (T.isAssignableFrom(candidate))
                        // or:
                        //    if (candidate instanceof T)
                        // But these are both illegal in Java because of type erasure.
                        // T is just a compile-time convenience and it is discarded at runtime.
                        // So, we have to force callers to pass in the class even though we're already
                        // typed with it, sigh.
                        if (!extensionClass.isAssignableFrom(candidate) || candidate.isInterface()) {
                            //logger.warning("Class " + candidate.getName() + " is the wrong type.");
                            // We actually don't care about this case - there may be many classes
                            // in the jar file other than the extension class (support classes and such).
                            // We don't need to log this warning for each one of them. Just ignore them.
                            // BUT - we do need to execute the loadClass in the preceding code.
                            // Otherwise, those support classes won't be available when the extension loads.
                            continue;
                        }

                        try {
                            Constructor<?> constructor = candidate.getDeclaredConstructor();
                            result = (T)constructor.newInstance();

                            // Validate the extension info:
                            if (result.getInfo() == null || !result.getInfo().isValid()) {
                                addStartupError(jarFile, "Extension class "
                                        + className
                                        + " does not have well-formed extension info - extension not loaded.");
                                result = null;
                                continue;
                            }

                            // Invoke createConfigProperties() and set the configProperties list for this extension:
                            // (this was formerly invoked from the extension constructor, but that was BAD...
                            //  see issue https://github.com/scorbo2/swing-extras/issues/116 for details.
                            //  We can safely do it from here.)
                            List<AbstractProperty> configProperties = result.createConfigProperties();
                            result.configProperties = configProperties == null ? new ArrayList<>() : configProperties;

                            // We can also invoke loadJarResources now while the class loader is still open:
                            // This is an extension class's last opportunity to load resources from its jar file,
                            // because we're about to close the class loader that loaded it!
                            result.loadJarResources();
                        }
                        catch (NoSuchMethodException ignored) {
                            // Extensions must supply a no-argument constructor:
                            addStartupError(jarFile, "Class "
                                    + candidate.getName()
                                    + " has no default constructor - ignored.");
                            continue;
                        }
                        catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                            addStartupError(jarFile, "Failed to instantiate "
                                                    + candidate.getName()
                                                    + ": "
                                                    + ex.getMessage(),
                                            Level.WARNING,
                                            ex);
                            continue;
                        }
                        catch (IncompatibleClassChangeError ex) {
                            addStartupError(jarFile, "Ignoring extension with incompatible class version: "
                                                    + candidate.getName()
                                                    + " (" + ex.getMessage() + ")",
                                            Level.WARNING,
                                            ex);
                            continue;
                        }
                        logger.log(Level.FINE, "Found qualifying AppExtension class: {0} in jar: {1}",
                                   new Object[]{candidate.getCanonicalName(),
                                           jarFile.getAbsolutePath()});
                    }

                    // If we make it here, the extension is instantiated and ready to go!
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        catch (Exception e) {
            // Generic catch-all for any exception not explicitly caught above:
            // We can't give a meaningful error message because we don't know what went wrong!
            addStartupError(jarFile, "Caught exception while loading extension from jar "
                                    + jarFile.getAbsolutePath()
                                    + ": "
                                    + e.getMessage(),
                            Level.WARNING,
                            e);
            return null;
        }

        // If we make it here, we got through the entire jar file without finding a compatible class:
        addStartupError(jarFile, "Jar file "
                + jarFile.getAbsolutePath()
                + " contains no suitable extension class.");
        return null;
    }

    /**
     * Invoked internally to look for an extInfo.json file inside the given jar file and
     * attempt to parse an AppExtensionInfo object out of it. Upon success, the newly
     * created AppExtensionInfo is returned. If anything goes wrong, the error is logged
     * and null is returned.
     * <p>
     * <b>Packaging an extInfo.json file into your extension jar</b><br>
     * Your jar file should contain an extInfo.json file somewhere in its resources.
     * We'll scan every entry in the jar file looking for it, so the exact location
     * doesn't matter, but a good convention is:
     * resources/fully/qualified/main/package/extInfo.json
     * </p>
     * <p>
     * Tip: You can easily generate an extInfo.json by populating an AppExtensionInfo
     * object in code and invoking toJson() on it, instead of writing the JSON by hand.
     * </p>
     *
     * @param jarFile The jar file in question. Must not be null.
     * @return An AppExtensionInfo, or null.
     */
    public AppExtensionInfo extractExtInfo(File jarFile) {
        if (jarFile == null) {
            logger.warning("ExtensionManager.extractExtInfo: given jarFile is null, cannot extract extInfo.");
            return null;
        }
        if (!jarFile.exists() || !jarFile.isFile() || !jarFile.canRead()) {
            logger.warning("ExtensionManager.extractExtInfo: given jarFile "
                                   + jarFile.getAbsolutePath()
                                   + " either does not exist or cannot be read, cannot extract extInfo.");
            return null;
        }

        logger.log(Level.FINE, "ExtensionManager.extractExtInfo({0})", jarFile.getAbsolutePath());
        try {
            try (JarFile jar = new JarFile(jarFile.getAbsolutePath())) {
                Enumeration<JarEntry> e = jar.entries();
                while (e.hasMoreElements()) {
                    JarEntry entry = e.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith("extInfo.json")) {
                        String data = FileSystemUtil.readStreamToString(jar.getInputStream(entry), "UTF-8");
                        AppExtensionInfo extInfo = AppExtensionInfo.fromJson(data);
                        if (extInfo == null) {
                            addStartupError(jarFile, "extractExtInfo: jar file "
                                    + jarFile.getAbsolutePath()
                                    + " contains an invalid extInfo.json - skipping.");
                            return null;
                        }
                        return extInfo;
                    }
                }
            }
        }
        catch (IOException ioe) {
            addStartupError(jarFile, "extractExtInfo: unable to parse jar file "
                                    + jarFile.getAbsolutePath()
                                    + ": "
                                    + ioe.getMessage(),
                            Level.SEVERE,
                            ioe);
            return null;
        }

        // If we make it here, we got through the whole jar without finding an extInfo.json:
        addStartupError(jarFile, "extractExtInfo: jar file "
                + jarFile.getAbsolutePath()
                + " does not contain an extInfo.json file.");
        return null;
    }

    /**
     * Given a Set of jar files in a given directory, this method looks for an optional load order
     * control file and will attempt to obey any sorting directives it contains. If the file is missing
     * or incomplete, the input set will be sorted by filename. If the load order file mentions any
     * jar files that don't exist in that directory, those directives are ignored.
     * <p>
     * <b>Formatting the load order file</b><br>
     * Blank lines and lines starting with a hash character are ignored. All other lines in the file are
     * assumed to be the name (without path) of a single jar file. The order in which those jars are listed
     * in this file is the order that the extension jars will be loaded. An example file might look like this:
     * </p>
     * <pre>
     * # Extension load order for MyApplication:
     *
     * # Extension 7 is super important, so let's load it first:
     * extension7-1.0.0.jar
     *
     * extension2-1.0.0.jar
     *
     * # Extension 1 is not so important, so let's load it last:
     * extension1-1.0.0.jar
     * </pre>
     * <p>
     * <b>SPECIAL NOTE:</b> the load order for application built-in extensions can't be overridden
     * in this load order control file! Refer to the Javadocs for addExtension() for more information on this.
     * </p>
     *
     * @param directory The directory to scan
     * @param jarSet    The Set of jar files to consider within that directory
     * @return A sorted List of jar files. This list.size() will always match the input Set's size.
     */
    protected List<File> sortExtensionJarSet(File directory, Set<File> jarSet) {
        if (directory == null) {
            logger.warning("ExtensionManager.sortExtensionJarSet: given directory is null, cannot sort jars.");
            return new ArrayList<>();
        }
        if (!directory.exists() || !directory.isDirectory() || !directory.canRead()) {
            logger.warning("ExtensionManager.sortExtensionJarSet: given directory "
                                   + directory.getAbsolutePath()
                                   + " either does not exist, is not a directory, or can't be read. Cannot sort jars.");
            return new ArrayList<>();
        }
        if (jarSet == null || jarSet.isEmpty()) {
            logger.fine("ExtensionManager.sortExtensionJarSet: given jarSet is null or empty, nothing to sort.");
            return new ArrayList<>();
        }

        List<File> unsortedJars = new ArrayList<>(jarSet);
        List<File> sortedJars = new ArrayList<>(jarSet.size());

        // Do we have a load order control file?
        File loadOrderFile = new File(directory, LOAD_ORDER_FILE);
        if (loadOrderFile.exists() && loadOrderFile.isFile() && loadOrderFile.canRead()) {
            logger.log(Level.FINE, "ExtensionManager: found load order file: " + loadOrderFile.getAbsolutePath());
            try {
                BufferedReader reader = new BufferedReader(new FileReader(loadOrderFile));
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();

                    // Skip blank lines and comment lines:
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    logger.log(Level.FINE, "ExtensionManager: processing load order entry: '" + line + "'");

                    // First, try exact match (original behavior):
                    File candidate = new File(directory, line);
                    if (candidate.exists() && unsortedJars.contains(candidate) && !sortedJars.contains(candidate)) {
                        logger.log(Level.FINE,
                                   "ExtensionManager: detected sort priority for jar (exact match): " + candidate.getName());
                        unsortedJars.remove(candidate);
                        sortedJars.add(candidate);
                        continue;
                    }

                    // If exact match didn't work, try partial matching:
                    List<File> matches = new ArrayList<>();
                    for (File jarFile : unsortedJars) {
                        if (jarFile.getName().startsWith(line)) {
                            matches.add(jarFile);
                        }
                    }

                    if (matches.isEmpty()) {
                        logger.log(Level.FINE, "ExtensionManager: no jar file found matching load order entry '" 
                                   + line + "' at line " + lineNumber);
                    } else if (matches.size() == 1) {
                        File matched = matches.get(0);
                        if (!sortedJars.contains(matched)) {
                            logger.log(Level.FINE,
                                       "ExtensionManager: detected sort priority for jar (partial match): " 
                                       + matched.getName() + " (matched by '" + line + "')");
                            unsortedJars.remove(matched);
                            sortedJars.add(matched);
                        }
                    } else {
                        // Multiple matches - use the first one alphabetically and log a warning.
                        // Alphabetical ordering provides deterministic behavior when patterns are too vague.
                        // Users should make their patterns more specific to avoid ambiguity.
                        matches.sort(Comparator.comparing(File::getName));
                        File matched = matches.get(0);
                        if (!sortedJars.contains(matched)) {
                            logger.log(Level.FINE,
                                       "ExtensionManager: multiple jars match load order entry '" + line 
                                       + "' (found " + matches.size() + " matches). Using first match alphabetically: " 
                                       + matched.getName() + ". Make pattern more specific to avoid ambiguity.");
                            unsortedJars.remove(matched);
                            sortedJars.add(matched);
                        }
                    }
                }
            }
            catch (IOException ioe) {
                // Something went wrong, so we'll fall back to filesystem order:
                logger.log(Level.WARNING, "ExtensionManager: Problem reading extension load order: "
                                   + ioe.getMessage()
                                   + " - extension load order will use jar file name sort order.",
                           ioe);
            }
        }

        // Add whatever's left in alphabetical order:
        unsortedJars.sort(Comparator.comparing(File::getAbsolutePath));
        sortedJars.addAll(unsortedJars);

        return sortedJars;
    }

    /**
     * Invoked internally to return a list of all loaded extension wrappers, sorted
     * by the extension name.
     *
     * @return A List of ExtensionWrappers, sorted by extension name (not by class name)
     */
    protected List<ExtensionWrapper> getAllLoadedExtensionWrappers() {
        List<ExtensionWrapper> wrapperList = new ArrayList<>(loadedExtensions.values());
        wrapperList.sort(null);
        return wrapperList;
    }

    /**
     * This is used internally to combine a source jar file, the extension that it contained, and
     * an isEnabled status flag into one handy location. It's never exposed to client code.
     */
    protected class ExtensionWrapper implements Comparable<ExtensionWrapper> {

        boolean isEnabled;
        File sourceJar;
        T extension;

        @Override
        public int compareTo(ExtensionWrapper o) {
            // There are no null checks here because we validate extension info when loading extensions.
            return extension.getInfo().getName().compareTo(o.extension.getInfo().getName());
        }
    }

    /**
     * Invoked internally to log a startup error which can be displayed on the
     * ExtensionManagerDialog later.
     */
    protected void addStartupError(File jarFile, String msg) {
        addStartupError(jarFile, msg, Level.WARNING, null);
    }

    /**
     * Invoked internally to log a startup error which can be displayed on the
     * ExtensionManagerDialog later.
     */
    protected void addStartupError(File jarFile, String msg, Level logLevel, Throwable e) {
        startupErrors.add(new StartupError(jarFile, msg));
        if (e != null) {
            logger.log(logLevel, msg, e);
        }
        else {
            logger.log(logLevel, msg);
        }
    }

    /**
     * Tracks any jar file that failed to load when ExtensionManager started up.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     * @since swing-extras 2.5
     */
    public static class StartupError {
        private final File jarFile;
        private final String errorMessage;

        public StartupError(File jarFile, String errorMessage) {
            this.jarFile = jarFile;
            this.errorMessage = errorMessage;
        }

        public File getJarFile() {
            return jarFile;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof StartupError that)) { return false; }
            return Objects.equals(jarFile, that.jarFile) && Objects.equals(errorMessage, that.errorMessage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jarFile, errorMessage);
        }

        @Override
        public String toString() {
            return jarFile == null ? "(unknown)" : jarFile.getName();
        }
    }
}
