package ca.corbett.updates;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Represents the version manifest for an application, with all of its versions and all
 * of its extensions. The result of this is a version manifest json file that can be
 * hosted somewhere and pointed to by the application's UpdateSources json. This allows
 * the application to retrieve the VersionManifest dynamically, to learn about what
 * extensions are available and what the latest version of each one is.
 * <p><b>Example manifest</b></p>
 * <pre>
 * {
 *   "manifestGenerated": "2025-10-16T00:53:33.946196621Z",
 *   "applicationName": "ExampleApplication",
 *   "applicationVersions": [
 *     {
 *       "version": "1.0",
 *       "extensions": [
 *         {
 *           "name": "MyFirstExtension",
 *           "versions": [
 *             {
 *               "extInfo:" { ... },
 *               "downloadPath": "MyFirstExtension-1.0.0.jar",
 *               "signaturePath": "MyFirstExtension-1.0.0.sig",
 *               "screenshots": [
 *                 "MyFirstExtension-1.0.0_screenshot1.jpg"
 *               ]
 *             },
 *             {
 *               "extInfo:" { ... },
 *               "downloadPath": "MyFirstExtension-1.0.1.jar",
 *               "signaturePath": "MyFirstExtension-1.0.1.sig",
 *               "screenshots": [
 *                 "MyFirstExtension-1.0.1_screenshot1.jpg",
 *                 "MyFirstExtension-1.0.1_screenshot2.png"
 *               ]
 *             }
 *           ]
 *         }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 * <p>
 *     The above example shows a simple application VersionManifest with a single application version,
 * which has a single extension called MyFirstExtension. This extension has two versions available,
 * 1.0.0 and 1.0.1 - both of these versions have been digitally signed (we can tell this because they
 * both provide a signaturePath to be used for verification of the jar).
 * </p>
 * <p>
 *     Notice that all paths in the version manifest are relative! But relative to what?
 *     A VersionManifest can only properly be interpreted through an UpdateSource, which contains
 *     a baseUrl - all paths are relative to this baseUrl. The reason for this is that one application
 *     may have many UpdateSources, but we don't want to have to generate a VersionManifest for
 *     each one, when the contents would be identical except for file locations. So, we generate
 *     the VersionManifest just once, and an application can understand it in conjunction with the
 *     baseUrl from any corresponding UpdateSource.
 * </p>
 * <p>
 *     <b>How do I set all this up?</b> - There's a helper application called
 *     <a href="https://github.com/scorbo2/ext-packager">ExtPackager</a> that can walk you through the process
 *     of setting up your UpdateSources json and your VersionManifest, and can also help you with things like
 *     digitally signing your extension jars, providing screenshots for each version, and uploading to your
 *     web host via FTP. You don't have to write this json by hand!
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class VersionManifest {

    private String manifestGenerated;
    private String applicationName;
    private final List<ApplicationVersion> applicationVersions = new ArrayList<>();
    private static final Gson gson;

    static {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static VersionManifest fromFile(File sourceFile) throws IOException, JsonSyntaxException {
        return fromJson(FileSystemUtil.readFileToString(sourceFile));

    }

    public static VersionManifest fromJson(String json) throws JsonSyntaxException {
        return gson.fromJson(json, VersionManifest.class);
    }

    /**
     * Temp code? Authoring should move to ext-packager repo
     */
    public void save(File destFile) throws IOException {
        FileSystemUtil.writeStringToFile(gson.toJson(this), destFile);
    }

    public Instant getManifestGenerated() {
        return Instant.parse(manifestGenerated);
    }

    public void setManifestGenerated(String timestampString) {
        manifestGenerated = timestampString;
    }

    public void setManifestGenerated(Instant instant) {
        manifestGenerated = instant.toString();
    }

    /**
     * Get the manifestGenerated timestamp in some local time zone, for example: ZoneId.of("America/Edmonton").
     */
    public LocalDateTime getManifestGeneratedAsLocalTime(ZoneId zoneId) {
        return LocalDateTime.ofInstant(getManifestGenerated(), zoneId);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Returns a list of all ApplicationVersions for this Application.
     */
    public List<ApplicationVersion> getApplicationVersions() {
        return new ArrayList<>(applicationVersions);
    }

    /**
     * Returns a list of all ApplicationVersions for the given major version number of this Application.
     */
    public List<ApplicationVersion> getApplicationVersionsForMajorVersion(int majorVersion) {
        return applicationVersions.stream()
                                  .filter(av -> av.getMajorVersion() == majorVersion)
                                  .sorted(Comparator.comparing(ApplicationVersion::getVersion,
                                                               new VersionStringComparator()))
                                  .toList();
    }

    /**
     * Returns a list of all unique extension names across all application versions for this Application.
     */
    public List<String> getUniqueExtensionNames() {
        return applicationVersions.stream()
                                  .flatMap(av -> av.getExtensions().stream())
                                  .map(Extension::getName)
                                  .distinct()
                                  .sorted(String.CASE_INSENSITIVE_ORDER)
                                  .toList();
    }

    /**
     * Returns a list of all unique extension names for the given major version number of this Application.
     */
    public List<String> getUniqueExtensionNamesForMajorVersion(int majorVersion) {
        return applicationVersions.stream()
                                  .filter(av -> av.getMajorVersion() == majorVersion)
                                  .flatMap(av -> av.getExtensions().stream())
                                  .map(Extension::getName)
                                  .distinct()
                                  .sorted(String.CASE_INSENSITIVE_ORDER)
                                  .toList();
    }

    /**
     * Returns the highest ExtensionVersion for the given extension name, across all application versions.
     */
    public Optional<ExtensionVersion> getHighestVersionForExtension(String extensionName) {
        List<ExtensionVersion> versions = new ArrayList<>();
        for (ApplicationVersion appVersion : applicationVersions) {
            for (Extension extension : appVersion.getExtensions()) {
                if (extension.getName().equalsIgnoreCase(extensionName)) {
                    versions.addAll(extension.getVersions());
                }
            }
        }
        versions.sort(Comparator.comparing(ev -> ev.getExtInfo().getVersion(), new VersionStringComparator()));
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(versions.size() - 1));
    }

    /**
     * Returns the highest ExtensionVersion for the given extension name, across all application versions
     * that match the given major version number.
     */
    public Optional<ExtensionVersion> getHighestVersionForExtensionInMajorAppVersion(String extensionName, int majorAppVersion) {
        List<ExtensionVersion> versions = new ArrayList<>();
        for (ApplicationVersion appVersion : applicationVersions) {
            if (appVersion.getMajorVersion() == majorAppVersion) {
                for (Extension extension : appVersion.getExtensions()) {
                    if (extension.getName().equalsIgnoreCase(extensionName)) {
                        Optional<ExtensionVersion> highestVersion = extension.getHighestVersion();
                        if (highestVersion.isPresent() && highestVersion.get().getExtInfo() != null) {
                            versions.add(highestVersion.get());
                        }
                    }
                }
            }
        }
        versions.sort(Comparator.comparing(ev -> ev.getExtInfo().getVersion(), new VersionStringComparator()));
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(versions.size() - 1));
    }

    /**
     * Returns a sorted list of the highest-version ExtensionVersion for each Extension that is available
     * for the specified major version of this Application. The list is sorted by extension name.
     */
    public List<ExtensionVersion> getHighestExtensionVersionsForMajorAppVersion(int majorAppVersion) {
        // What extensions are available for this major version?
        List<String> extensionNames = getUniqueExtensionNamesForMajorVersion(majorAppVersion);

        // For each one, find its highest version:
        List<ExtensionVersion> highestVersions = new ArrayList<>();
        for (String extensionName : extensionNames) {
            Optional<ExtensionVersion> highestVersion = getHighestVersionForExtensionInMajorAppVersion(extensionName,
                                                                                                       majorAppVersion);
            highestVersion.ifPresent(highestVersions::add);
        }

        // Sort by extension name and return:
        highestVersions.sort(Comparator.comparing(ev -> ev.getExtInfo().getName(), String.CASE_INSENSITIVE_ORDER));
        return highestVersions;
    }

    /**
     * This is a bit wonky, but because ExtensionVersion does not know about the Extension that contains it,
     * I have to have this lookup function to find the Extension for a given ExtensionVersion.
     */
    public Optional<Extension> findExtensionForExtensionVersion(ExtensionVersion extVersion) {
        for (ApplicationVersion appVersion : applicationVersions) {
            for (Extension extension : appVersion.getExtensions()) {
                if (extension.getVersions().contains(extVersion)) {
                    return Optional.of(extension);
                }
            }
        }
        return Optional.empty();
    }

    public void addApplicationVersion(ApplicationVersion version) {
        this.applicationVersions.add(version);
    }

    public void removeApplicationVersion(ApplicationVersion version) {
        this.applicationVersions.remove(version);
    }

    public void clearApplicationVersions() {
        this.applicationVersions.clear();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof VersionManifest that)) { return false; }
        return Objects.equals(applicationName, that.applicationName)
                && Objects.equals(applicationVersions, that.applicationVersions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, applicationVersions);
    }

    /**
     * Returns the ApplicationVersion with the highest version number.
     * Will return null if there are no ApplicationVersions here.
     */
    public ApplicationVersion findLatestApplicationVersion() {
        if (applicationVersions.isEmpty()) {
            return null;
        }

        return applicationVersions
                .stream()
                .max(Comparator.comparing(ApplicationVersion::getVersion, new VersionStringComparator()))
                .orElse(null);
    }

    /**
     * Represents a single version of the application in question, along with all of its
     * compatible extensions.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    public static class ApplicationVersion {

        private String version;
        private final List<Extension> extensions = new ArrayList<>();

        public String getVersion() {
            return version;
        }

        /**
         * Extracts just the major version number from the version string, if possible.
         * Will return 0 if it cannot be determined.
         */
        public int getMajorVersion() {
            return AppExtensionInfo.extractMajorVersion(version);
        }

        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * Returns a copy of the list of all extensions for this application version.
         */
        public List<Extension> getExtensions() {
            return new ArrayList<>(extensions);
        }

        /**
         * For each Extension in this application version, find and return its highest ExtensionVersion.
         */
        public List<ExtensionVersion> getHighestExtensionVersions() {
            return extensions.stream()
                             .sorted(Comparator.comparing(Extension::getName, String.CASE_INSENSITIVE_ORDER))
                             .map(Extension::getHighestVersion)
                             .filter(Optional::isPresent)
                             .map(Optional::get)
                             .toList();
        }

        public void addExtension(Extension extension) {
            this.extensions.add(extension);
        }

        public void removeExtension(Extension extension) {
            this.extensions.remove(extension);
        }

        public void clearExtensions() {
            this.extensions.clear();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof ApplicationVersion that)) { return false; }
            return Objects.equals(version, that.version) && Objects.equals(extensions, that.extensions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, extensions);
        }
    }

    /**
     * Represents a single extension for the application in question, along with all
     * of the versions of this extension.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    public static class Extension {

        private String name;
        private final List<ExtensionVersion> versions = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<ExtensionVersion> getVersions() {
            return new ArrayList<>(versions);
        }

        /**
         * Find and return the highest ExtensionVersion for this extension.
         */
        public Optional<ExtensionVersion> getHighestVersion() {
            return this.versions.stream()
                                .filter(ev -> ev.getExtInfo() != null && ev.getExtInfo().getVersion() != null)
                                .max(Comparator.comparing(ev -> ev.getExtInfo().getVersion(),
                                                          new VersionStringComparator()));
        }

        public void addVersion(ExtensionVersion newVersion) {
            this.versions.add(newVersion);
        }

        public void removeVersion(ExtensionVersion version) {
            this.versions.remove(version);
        }

        public void clearVersions() {
            this.versions.clear();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Extension extension)) { return false; }
            return Objects.equals(name, extension.name) && Objects.equals(versions, extension.versions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, versions);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Represents a single version of a single extension. At this level, we can specify a
     * path for downloading the jar file, the optional signature file, and any screenshots.
     * All paths are relative to the baseUrl defined in the applicable UpdateSource!
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    public static class ExtensionVersion {
        private AppExtensionInfo extInfo;
        private String downloadPath;
        private String signaturePath;
        private final List<String> screenshots = new ArrayList<>();

        public AppExtensionInfo getExtInfo() {
            return extInfo;
        }

        public void setExtInfo(AppExtensionInfo extInfo) {
            this.extInfo = extInfo;
        }

        public String getDownloadPath() {
            return downloadPath;
        }

        public void setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
        }

        public String getSignaturePath() {
            return signaturePath;
        }

        public void setSignaturePath(String signaturePath) {
            this.signaturePath = signaturePath;
        }

        public List<String> getScreenshots() {
            return new ArrayList<>(screenshots);
        }

        public void addScreenshot(String screenshotPath) {
            this.screenshots.add(screenshotPath);
        }

        public void clearScreenshots() {
            this.screenshots.clear();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof ExtensionVersion that)) { return false; }
            return Objects.equals(extInfo, that.extInfo)
                    && Objects.equals(downloadPath, that.downloadPath)
                    && Objects.equals(signaturePath, that.signaturePath)
                    && Objects.equals(screenshots, that.screenshots);
        }

        @Override
        public int hashCode() {
            return Objects.hash(extInfo, downloadPath, signaturePath, screenshots);
        }
    }
}
