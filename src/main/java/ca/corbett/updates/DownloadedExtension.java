package ca.corbett.updates;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Used internally when retrieving a remote extension to store all associated
 * files together into one object: the jar file, the optional signature file,
 * and the optional list of screenshots. This is all handled transparently
 * by ExtensionManagerDialog - application code will likely never have to
 * interact with this class directly.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class DownloadedExtension {

    private File jarFile;
    private File signatureFile;
    private final List<File> screenshots = new CopyOnWriteArrayList<>();

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
