package ca.corbett.extras;

import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
 * <p> <b>Technical ideas (rough):</b> every application could (optionally) package an update-sources.json file
 *     that ONLY contains a URL (or list of URLs for redundancy) of a versions.json file that contains
 *     information about the application and its extensions. If the application does not package such a file,
 *     then this functionality is either disabled outright OR there is some way in the UI to dynamically add
 *     a new source. This sources.json could be very, very simple:
 * </p>
 * <ul>
 *     <li>The latest available version
 *     <li>A list of one or more update sources (just a url to hit to get the versions.json file).
 * </ul>
 * <p><b>EXAMPLE:</b></p>
 * <pre>
 * {
 *   "applicationName": "ImageViewer",
 *   "updateSources": [
 *     {
 *       "url": "http://www.corbett.ca/apps/ImageViewer-versions.json"
 *     },
 *     {
 *       "url": "file://localhost/home/scorbett/Software/sc-releases/ImageViewer-versions.json"
 *     }
 *   ]
 * }
 * </pre>
 * <p>
 *     In this example, we see an update-sources.json for the ImageViewer application. Two sources are
 *     provided: a remotely hosted one, and also one on the local filesystem (this one could be useful
 *     for local testing without hammering the remote host all the time - applications would remove this
 *     before packaging).
 * </p>
 * <p><b>Defining the versions.json file</b></p>
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
 *               "sha1": "(sha1Hash)",
 *               "url": "http://www.corbett.ca/apps/ext-iv-ice-2.3.0.jar"
 *             },
 *             {
 *               "version": "2.3.1",
 *               "sha1": "(sha1Hash)",
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
 * <p>
 *     <b>Considerations</b> - having the versions.json self-report the sha-1 hash of the file to be
 *     downloaded is a bit of a security risk... A malicious host could supply evil extension jars
 *     and how would the application know? This code should really make use of proper jar signing.
 *     The alternative would be to host the sha-1 hash somewhere safe (say, GitHub) and use that
 *     as a reference when downloading jars.
 * </p>
 * <p>
 *     <b>UPDATE</b>
 *     after thinking about it, I created <a href="https://github.com/scorbo2/swing-extras/issues/141">Issue 141</a>
 *     and we now have a SignatureUtil class that can generate a signature on a file and later verify that
 *     signature using a key pair. The impact to the above design approach is as follows (and this is still a
 *     bit rough):
 * </p>
 * <ul>
 *     <li>don't bother with sha1 hashes, it's not good enough.
 *     <li>Every extension will be expected to provide a signature file as part of its download (either a
 *         separate side-by-side file, or embedded into the jar file itself)
 *     <li>There must be a public key file hosted on the same server that supplied the extension jar
 *     <li>This public key will be used to verify the signature on the downloaded jar.
 *     <li>If any of the above fails (no signature file provided, no public key provided, signature doesn't match),
 *         a HUGE FLAMING WARNING is presented to the user that we can't verify the downloaded jar.
 *         Maybe have a "proceed anyway" for those who are brave.
 * </ul>
 * <p>
 *     This still doesn't feel super high security, but it's better than just relying on sha-1 hashes and the
 *     honor system.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class UpdateManager {

    protected final Gson gson;
    protected final File sourceFile;
    protected final Source source;

    public UpdateManager(File sourceFile) throws IOException {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.sourceFile = sourceFile;
        this.source = gson.fromJson(FileSystemUtil.readFileToString(sourceFile), Source.class);
    }

    public Source getSource() {
        return source;
    }
    
    public static class Source {
        private final String name;
        private final List<String> urls;

        public Source(String name, List<String> urls) {
            this.name = name;
            this.urls = new ArrayList<>(urls);
        }

        public String getName() {
            return name;
        }

        public List<String> getUrls() {
            return new ArrayList<>(urls);
        }
    }
}
