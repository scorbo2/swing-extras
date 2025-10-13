package ca.corbett.updates;

import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single Application, with all of its versions and all
 * of its extensions.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class Application {

    private String applicationName;
    private final List<ApplicationVersion> applicationVersions = new ArrayList<>();
    private static final Gson gson;

    static {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Temp code? Authoring should move to ext-package repo
     */
    public static Application fromJson(File sourceFile) throws IOException, JsonSyntaxException {
        return gson.fromJson(FileSystemUtil.readFileToString(sourceFile), Application.class);
    }

    /**
     * Temp code? Authoring should move to ext-package repo
     */
    public void save(File destFile) throws IOException {
        FileSystemUtil.writeStringToFile(gson.toJson(this), destFile);
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
        if (!(object instanceof Application that)) { return false; }
        return Objects.equals(applicationName, that.applicationName)
                && Objects.equals(applicationVersions, that.applicationVersions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationName, applicationVersions);
    }
}
