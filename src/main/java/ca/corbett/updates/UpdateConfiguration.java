package ca.corbett.updates;

import java.util.ArrayList;
import java.util.List;

/**
 * Your application can optionally package a json file which represents one or more "update sources" - that is,
 * remote URLs where your application can check for new versions, and also to enable discovery and installation
 * of new extensions, or new versions of already-installed extensions. To enable this feature, package an
 * update configuration json file with your application, as shown in the following example:
 * <pre>
 *     {
 *         "applicationName": "MyAmazingApplication",
 *         "updateSources": [
 *             {
 *                 "versionManifestUrl": "http://www.test.example/versions.json",
 *                 "publicKeyUrl": "http://www.test.example/public.key"
 *             }
 *         ]
 *     }
 * </pre>
 * <p>
 *     The above example shows an application with a single remote update source. The public key is optional,
 *     but recommended to allow for digital signature verification on downloaded extension jars.
 * </p>
 * <p>
 *     For documentation on setting up an update source, see TODO insert doc link here
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class UpdateConfiguration {
    private final String applicationName;
    private final List<UpdateSource> updateSources;

    public UpdateConfiguration(String applicationName) {
        this.applicationName = applicationName;
        this.updateSources = new ArrayList<>();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public List<UpdateSource> getUpdateSources() {
        return new ArrayList<>(updateSources);
    }

    public void addUpdateSource(UpdateSource updateSource) {
        updateSources.add(updateSource);
    }
}
