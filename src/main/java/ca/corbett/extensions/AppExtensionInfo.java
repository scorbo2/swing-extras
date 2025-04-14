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
 * be discovered and interrogated by ExtensionManager. See ExtensionManager.extractExtInfo()
 * for more.
 *
 * @author scorbo2
 * @since 2023-11-11
 */
public class AppExtensionInfo {

    protected static Gson gson;

    protected final String name;
    protected final String version;
    protected final String targetAppName;
    protected final String targetAppVersion;
    protected final String author;
    protected final String releaseNotes;
    protected final String shortDescription;
    protected final String longDescription;
    protected final Map<String, String> customFields;

    protected AppExtensionInfo(Builder builder) {
        this.name = builder.name;
        this.author = builder.author;
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

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
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
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.version);
        hash = 23 * hash + Objects.hashCode(this.targetAppName);
        hash = 23 * hash + Objects.hashCode(this.targetAppVersion);
        hash = 23 * hash + Objects.hashCode(this.author);
        hash = 23 * hash + Objects.hashCode(this.releaseNotes);
        hash = 23 * hash + Objects.hashCode(this.shortDescription);
        hash = 23 * hash + Objects.hashCode(this.longDescription);
        hash = 23 * hash + Objects.hashCode(this.customFields);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppExtensionInfo other = (AppExtensionInfo)obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.targetAppName, other.targetAppName)) {
            return false;
        }
        if (!Objects.equals(this.targetAppVersion, other.targetAppVersion)) {
            return false;
        }
        if (!Objects.equals(this.author, other.author)) {
            return false;
        }
        if (!Objects.equals(this.releaseNotes, other.releaseNotes)) {
            return false;
        }
        if (!Objects.equals(this.shortDescription, other.shortDescription)) {
            return false;
        }
        if (!Objects.equals(this.longDescription, other.longDescription)) {
            return false;
        }
        return Objects.equals(this.customFields, other.customFields);
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
        protected String author;
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
