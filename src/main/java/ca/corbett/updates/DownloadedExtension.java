package ca.corbett.updates;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Used internally when retrieving a remote extension to store all associated
 * files together into one object: the jar file, the optional signature file,
 * and the optional list of screenshots.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class DownloadedExtension {

    private File jarFile;
    private File signatureFile;
    private final List<File> screenshots = new ArrayList<>();

    public File getJarFile() {
        return jarFile;
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    public File getSignatureFile() {
        return signatureFile;
    }

    public void setSignatureFile(File signatureFile) {
        this.signatureFile = signatureFile;
    }

    public void addScreenshot(File image) {
        screenshots.add(image);
    }

    public List<File> getScreenshots() {
        return new ArrayList<>(screenshots);
    }

    public void clearScreenshots() {
        screenshots.clear();
    }
}
