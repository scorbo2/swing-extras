package ca.corbett.updates;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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
 *               "downloadUrl": "http://www.myhost.example/MyFirstExtension-1.0.0.jar",
 *               "signatureUrl": "http://www.myhost.example/MyFirstExtension-1.0.0.sig",
 *               "screenshots": []
 *             },
 *             {
 *               "extInfo:" { ... },
 *               "downloadUrl": "http://www.myhost.example/MyFirstExtension-1.0.1.jar",
 *               "signatureUrl": "http://www.myhost.example/MyFirstExtension-1.0.1.sig",
 *               "screenshots": []
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
 * both provide a signatureUrl to be used for verification of the jar).
 * </p>
 * <p>
 *     <b>How do I set all this up?</b> - There's a helper application called
 *     <a href="https://github.com/scorbo2/ext-package">ExtPackager</a> that can walk you through the process
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

    /**
     * Temp code? Authoring should move to ext-package repo
     */
    public static VersionManifest fromJson(File sourceFile) throws IOException, JsonSyntaxException {
        return fromJson(FileSystemUtil.readFileToString(sourceFile));

    }

    public static VersionManifest fromJson(String json) throws JsonSyntaxException {
        return gson.fromJson(json, VersionManifest.class);
    }

    /**
     * Temp code? Authoring should move to ext-package repo
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

        public void setVersion(String version) {
            this.version = version;
        }

        public List<Extension> getExtensions() {
            return new ArrayList<>(extensions);
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
    }

    /**
     * Represents a single version of a single extension. At this level, we can specify an actual
     * download url for the jar file, and an optional signature file, if the jar file was digitally signed.
     * We can also supply an optional list of screenshots for this extension.
     *
     * @author <a href="https://github.com/scorbo2">scorbo2</a>
     */
    public static class ExtensionVersion {
        private AppExtensionInfo extInfo;
        private URL downloadUrl;
        private URL signatureUrl;
        private final List<URL> screenshots = new ArrayList<>();

        public AppExtensionInfo getExtInfo() {
            return extInfo;
        }

        public void setExtInfo(AppExtensionInfo extInfo) {
            this.extInfo = extInfo;
        }

        public URL getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(URL downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public URL getSignatureUrl() {
            return signatureUrl;
        }

        public void setSignatureUrl(URL signatureUrl) {
            this.signatureUrl = signatureUrl;
        }

        public List<URL> getScreenshots() {
            return new ArrayList<>(screenshots);
        }

        public void addScreenshot(URL screenshotUrl) {
            this.screenshots.add(screenshotUrl);
        }

        public void removeScreenshot(URL screenshotUrl) {
            this.screenshots.remove(screenshotUrl);
        }

        public void clearScreenshots() {
            this.screenshots.clear();
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof ExtensionVersion that)) { return false; }
            return Objects.equals(extInfo, that.extInfo)
                    && Objects.equals(downloadUrl, that.downloadUrl)
                    && Objects.equals(signatureUrl, that.signatureUrl)
                    && Objects.equals(screenshots, that.screenshots);
        }

        @Override
        public int hashCode() {
            return Objects.hash(extInfo, downloadUrl, signatureUrl, screenshots);
        }
    }
}
