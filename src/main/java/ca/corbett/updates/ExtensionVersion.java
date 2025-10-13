package ca.corbett.updates;

import ca.corbett.extensions.AppExtensionInfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single version of a specific extension, along with metadata to describe it
 * and the target version of its associated application. This structure is used in
 * Extension.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ExtensionVersion {
    private AppExtensionInfo extInfo;
    private URL downloadURL;
    private final List<URL> screenshots = new ArrayList<>();

    public AppExtensionInfo getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(AppExtensionInfo extInfo) {
        this.extInfo = extInfo;
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(URL downloadURL) {
        this.downloadURL = downloadURL;
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
                && Objects.equals(downloadURL, that.downloadURL)
                && Objects.equals(screenshots, that.screenshots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extInfo, downloadURL, screenshots);
    }
}
