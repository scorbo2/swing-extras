package ca.corbett.extensions;

import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Allows an extension to provide some basic metadata about itself.
 * Name, version, targetAppName, and targetAppVersion should be considered
 * mandatory, with the remaining fields being optional. Extra application-specific
 * fields can be thrown into the custom field map, but you are restricted
 * to simple single-line string values. Every extension should package
 * an extInfo.json file as a resource into its jar file so that it can
 * be discovered and interrogated by ExtensionManager.
 * <p>
 * See ExtensionManager.extractExtInfo() for more.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2023-11-11
 */
public class AppExtensionInfo {

    protected static Gson gson;

    protected final String name;
    protected final String version;
    protected final String extensionUrl;
    protected final String targetAppName;
    protected final String targetAppVersion;
    protected final String author;
    protected final String authorUrl;
    protected final String releaseNotes;
    protected final String shortDescription;
    protected final String longDescription;
    protected final Map<String, String> customFields;

    protected AppExtensionInfo(Builder builder) {
        this.name = builder.name;
        this.extensionUrl = builder.extensionUrl;
        this.author = builder.author;
        this.authorUrl = builder.authorUrl;
        this.version = builder.version;
        this.targetAppName = builder.targetAppName;
        this.targetAppVersion = builder.targetAppVersion;
        this.shortDescription = builder.shortDescription;
        this.longDescription = builder.longDescription;
        this.releaseNotes = builder.releaseNotes;
        customFields = builder.customFields;
    }

    public String toJson() {
        return getGson().toJson(this);
    }

    /**
     * Attempts to parse an AppExtensionInfo out of the given json. Any field not mentioned in
     * the json will be returned as null.
     *
     * @param json A json representation of an AppExtensionInfo object.
     * @return An AppExtensionInfo object, or null if parsing was not possible.
     */
    public static AppExtensionInfo fromJson(String json) {
        try {
            return getGson().fromJson(json, AppExtensionInfo.class);
        }
        catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Attempts to parse an AppExtensionInfo instance out of json read from the given
     * InputStream. Example usage in an extension:
     * <BLOCKQUOTE><PRE>
     * AppExtensionInfo.fromStream(this.getClass().getClassLoader().getResourceAsStream("/path/extInfo.json"));
     * </PRE></BLOCKQUOTE>
     *
     * @param stream An InputStream containing json.
     * @return An AppExtensionInfo object, or null if parsing was not possible.
     */
    public static AppExtensionInfo fromStream(InputStream stream) {
        try {
            return fromJson(FileSystemUtil.readStreamToString(stream));
        }
        catch (RuntimeException | IOException ignored) {
            return null;
        }
    }

    /**
     * Attempts to use the given Class to read extInfo from the given jar resource. This is intended
     * to be invoked by extension jars, where resources have to be loaded from the class that is present
     * in the jar that contains them.
     *
     * @param extensionClass the extension class responsible for loading the resource.
     * @param resource       the jar resource to be loaded (full resource path and name of extInfo.json)
     * @return A parsed AppExtensionInfo object, or null if the resource could not be read.
     */
    public static AppExtensionInfo fromExtensionJar(Class<? extends AppExtension> extensionClass, String resource) {
        try (InputStream in = extensionClass.getResourceAsStream(resource)) {
            if (in != null) {
                return fromStream(in);
            }
        }
        catch (IOException ignored) {
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public String getExtensionUrl() {
        return extensionUrl;
    }

    public String getVersion() {
        return version;
    }

    public String getTargetAppName() {
        return targetAppName;
    }

    public String getTargetAppVersion() {
        return targetAppVersion;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public List<String> getCustomFieldNames() {
        List<String> list = new ArrayList<>();
        if (customFields != null) {
            list.addAll(customFields.keySet());
            list.sort(null);
        }
        return list;
    }

    public String getCustomFieldValue(String fieldName) {
        return customFields == null ? null : customFields.get(fieldName);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AppExtensionInfo that)) { return false; }
        return Objects.equals(name, that.name)
                && Objects.equals(version, that.version)
                && Objects.equals(extensionUrl, that.extensionUrl)
                && Objects.equals(targetAppName, that.targetAppName)
                && Objects.equals(targetAppVersion, that.targetAppVersion)
                && Objects.equals(author, that.author)
                && Objects.equals(authorUrl, that.authorUrl)
                && Objects.equals(releaseNotes, that.releaseNotes)
                && Objects.equals(shortDescription, that.shortDescription)
                && Objects.equals(longDescription, that.longDescription)
                && Objects.equals(customFields, that.customFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, extensionUrl, targetAppName, targetAppVersion, author, authorUrl,
                            releaseNotes, shortDescription, longDescription, customFields);
    }

    protected static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        return gson;
    }

    public static class Builder {

        protected final String name;
        protected String version;
        protected String extensionUrl;
        protected String author;
        protected String authorUrl;
        protected String targetAppName;
        protected String targetAppVersion;
        protected String shortDescription;
        protected String longDescription;
        protected String releaseNotes;
        protected final Map<String, String> customFields;

        public Builder(String name) {
            this.name = name;
            customFields = new HashMap<>();
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder setAuthorUrl(String authorUrl) {
            this.authorUrl = authorUrl;
            return this;
        }

        public Builder setExtensionUrl(String extensionUrl) {
            this.extensionUrl = extensionUrl;
            return this;
        }

        public Builder setTargetAppName(String name) {
            this.targetAppName = name;
            return this;
        }

        public Builder setTargetAppVersion(String min) {
            this.targetAppVersion = min;
            return this;
        }

        public Builder setShortDescription(String desc) {
            this.shortDescription = desc;
            return this;
        }

        public Builder setLongDescription(String desc) {
            this.longDescription = desc;
            return this;
        }

        public Builder setReleaseNotes(String notes) {
            this.releaseNotes = notes;
            return this;
        }

        public Builder addCustomField(String name, String value) {
            this.customFields.put(name, value);
            return this;
        }

        public AppExtensionInfo build() {
            return new AppExtensionInfo(this);
        }
    }
}
