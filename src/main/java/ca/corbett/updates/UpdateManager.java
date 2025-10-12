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
 * WORK IN PROGRESS
 * <p>
 *     <b>The general idea</b> - Applications should have a way of detecting if they are out of date. That
 *     means hitting some remote url on a trusted host to compare the running application version to whatever
 *     is listed as the latest available version. Additionally, applications should have a way of detecting
 *     that any installed extensions are out of date, OR if there are additional extensions available that
 *     are not currently installed.
 * </p>
 * <p><b>Step 1 - defining the update configuration json</b> - complete. See UpdateConfiguration class.</p>
 * <p><b>Step 2 - Defining the versions.json file</b></p>
 * <p>
 *     The versions.json contains the latest available version so that applications can quickly see
 * if they are out of date. Additionally, a list of all versions of the application that have downloadable
 * extensions could be provided. Each version would have a list of extensions, and each extension would
 * have a list of versions (along with an indicator of which version is the latest version). Each extension
 * version entry would look roughly like this:
 * <ul>
 *     <li>The extension name
 *     <li>Maybe extension short description? Should we just import the extInfo.json here?
 *     <li>A url for downloading the actual extension jar
 *     <li>A sha-1 hash of the extension jar (for verifying successful download)
 * </ul>
 * <p><b>Very rough example</b></p>
 * <pre>
 * {
 *   "applicationName": "ImageViewer",
 *   "latestVersion": "2.3",
 *   "versions": [
 *     {
 *       "version": "2.3",
 *       "extensions": [
 *         {
 *           "name": "ICE",
 *           "latestVersion": "2.3.1",
 *           "versions": [
 *             {
 *               "version": "2.3.0",
 *               "url": "http://www.corbett.ca/apps/ext-iv-ice-2.3.0.jar"
 *             },
 *             {
 *               "version": "2.3.1",
 *               "url": "http://www.corbett.ca/apps/ext-iv-ice-2.3.1.jar"
 *             }
 *           ]
 *         }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 * <p>
 *     In this example, we can see a single version of ImageViewer is listed, with one extension that has
 *     two available versions. Applications that request and parse this file can quickly learn that the
 *     latest version of ImageViewer is 2.3, and the latest version of the ICE extension is 2.3.1 - an
 *     application that has an older version of ICE could report that to the user, and if the user opts
 *     to upgrade, the application could download and install the 2.3.1 jar, replacing the older 2.3.0 jar,
 *     then signal for an application restart, and boom, updated.
 * </p>
 * <p>
 *     This same general approach could work for new extension discovery - if ICE isn't installed, the application
 *     can learn that it exists, and present its information to the user for possible download and installation.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class UpdateManager {

    protected final Gson gson;
    protected final File sourceFile;
    protected final UpdateConfiguration updateConfiguration;

    public UpdateManager(File sourceFile) throws JsonSyntaxException, IOException {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.sourceFile = sourceFile;
        this.updateConfiguration = gson.fromJson(FileSystemUtil.readFileToString(sourceFile),
                                                 UpdateConfiguration.class);
    }

    public String getApplicationName() {
        return updateConfiguration.getApplicationName();
    }

    public List<UpdateSource> getUpdateSources() {
        return new ArrayList<>(updateConfiguration.getUpdateSources());
    }

    public void addUpdateSource(UpdateSource source) throws IOException {
        updateConfiguration.addUpdateSource(source);
        save();
    }

    public void save() throws IOException {
        FileSystemUtil.writeStringToFile(gson.toJson(updateConfiguration), sourceFile);
    }
}
