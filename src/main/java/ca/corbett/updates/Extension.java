package ca.corbett.updates;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single AppExtension with all of its versions.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class Extension {

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
