package ca.corbett.updates;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific version of a given Application.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ApplicationVersion {

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
}
