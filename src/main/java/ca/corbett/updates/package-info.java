/**
 * Contains classes and utilities to support dynamic application and extension updates from one
 * or more configured remote sources.
 * <p>
 *     <b>UpdateSources</b> - Allows your application to bundle an update sources json file describing
 * where to look for updates and extensions.
 * </p>
 * <p>
 *     <b>VersionManifest</b> - If you prepare and upload a version manifest json file on a remote
 *     web server, you can point your application to it so that your application can look for
 *     new extensions and new versions of existing extensions. You can use the
 *     <a href="https://github.com/scorbo2/ext-packager">ExtPackager</a> helper
 *     application (not included in swing-extras) to help you create this manifest.
 * </p>
 * <p>
 *     <b>UpdateManager</b> - this is the class that your application will interact with to do the
 *     actual interrogation of remote update sources and handle downloading and installing extensions.
 *     This is handled transparently by ExtensionManagerDialog! Your application code will likely never
 *     have to deal with UpdateManager directly, other than instantiating an instance of it using
 *     the update sources json file bundled with your application, and then supplying that instance to
 *     ExtensionManagerDialog.
 * </p>
 * <p>
 *     <b>How do I set all this up?</b> - There's a helper application called
 *     <a href="https://github.com/scorbo2/ext-packager">ExtPackager</a> that can walk you through the process
 *     of setting up your UpdateSources json and your VersionManifest, and can also help you with things like
 *     digitally signing your extension jars, providing screenshots for each version, and uploading to your
 *     web host via FTP. You don't have to write this json by hand!
 * </p>
 */
package ca.corbett.updates;