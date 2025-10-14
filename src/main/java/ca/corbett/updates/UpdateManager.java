package ca.corbett.updates;

import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Applications built with swing-extras have a way of detecting if they are out of date, or if any of their
 * currently-installed extensions are out of date. Further, applications have a way to discover new
 * extensions, download them, and install them. This UpdateManager class helps manage this feature.
 * <p>
 *     <b>How do I use this?</b> - Start by creating a VersionManifest for your application and all
 *     of its extensions. There is an application to help you do this! See
 *     <a href="https://github.com/scorbo2/ext-package">ExtPackager</a> on GitHub. Once your VersionManifest
 *     is ready, you can upload it to your web server. Then, you create an UpdateSources json which
 *     points to your VersionManifest, and you then bundle this UpdateSources with your application.
 *     Then, after your application is released, you can update the published VersionManifest on your
 *     web host whenever you have a new extension, or a new version of an existing extension. Your application
 *     can dynamically discover the new extension or version, and offer the ability to download and
 *     install it!
 * </p>
 * <p>
 *     <b>Digital signing</b> - you can optionally digitally sign your extension jars so that your application
 *     can verify that they come from a known good source. The ExtPackager application can walk you through
 *     the process of generating a key pair, signing your extensions with the private key, and uploading
 *     the public key to your web host. This is all entirely optional - if you are hosting on a trusted
 *     internal server (like on a local network), you can skip this step entirely. If a signature and a public
 *     key are provided, this class will automatically verify the digital signature after each download.
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
public class UpdateManager {

    protected final Gson gson;
    protected final File sourceFile;
    protected final UpdateSources updateSources;

    public UpdateManager(File sourceFile) throws JsonSyntaxException, IOException {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.sourceFile = sourceFile;
        this.updateSources = gson.fromJson(FileSystemUtil.readFileToString(sourceFile), UpdateSources.class);
    }

    public String getApplicationName() {
        return updateSources.getApplicationName();
    }

    /**
     * Requests the VersionManifest for the given UpdateSource (use getUpdateSources to enumerate the
     * available UpdateSources in this UpdateManager).
     */
    public void retrieveVersionManifest(UpdateSources.UpdateSource updateSource) {
        // TODO we can't block here and return a VersionManifest
        // TODO fire up a thread... do we need to send updates on progress?
        //      It should at most be a few KB of json data...
        //      Still... we have to update the caller via a callback/listener of some kind
    }

    public List<UpdateSources.UpdateSource> getUpdateSources() {
        return new ArrayList<>(updateSources.getUpdateSources());
    }

    public void addUpdateSource(UpdateSources.UpdateSource source) throws IOException {
        updateSources.addUpdateSource(source);
        save();
    }

    public void save() throws IOException {
        FileSystemUtil.writeStringToFile(gson.toJson(updateSources), sourceFile);
    }
}
