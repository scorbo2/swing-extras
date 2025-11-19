package ca.corbett.updates;

import ca.corbett.extras.crypt.SignatureUtil;
import ca.corbett.extras.io.FileSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

class UpdateManagerTest {

    @Test
    public void newUpdateManager_withValidJson_shouldParse() throws Exception {
        // GIVEN a minimal but valid sources json:
        String json = """
                {
                  "applicationName": "Test",
                  "updateSources": [
                    {
                      "name": "Test source",
                      "baseUrl": "http://www.test.example",
                      "versionManifest": "blah.json",
                      "publicKey": "public.key"
                    }
                  ]
                }
                """;
        File sourcesFile = File.createTempFile("UpdateManagerTest", "json");
        sourcesFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(json, sourcesFile);

        // WHEN we instantiate an UpdateManager with it:
        UpdateManager manager = new UpdateManager(sourcesFile);

        // THEN we should see expected results:
        assertNotNull(manager);
        assertEquals("Test", manager.getApplicationName());
        assertEquals(1, manager.getUpdateSources().size());
        assertEquals("Test source", manager.getUpdateSources().get(0).getName());
        assertEquals("http://www.test.example/blah.json",
                     manager.getUpdateSources().get(0).getVersionManifestUrl().toString());
        assertEquals("http://www.test.example/public.key",
                     manager.getUpdateSources().get(0).getPublicKeyUrl().toString());
    }

    @Test
    public void newUpdateManager_withInvalidURL_shouldFailToParse() throws Exception {
        // GIVEN a sources json with a bad url in it:
        String json = """
                {
                  "applicationName": "Test",
                  "updateSources": [
                    {
                      "baseUrl": "This is not a url!",
                      "versionManifest": "This is not a url!",
                      "publicKey": "And neither is this."
                    }
                  ]
                }
                """;
        File sourcesFile = File.createTempFile("UpdateManagerTest", "json");
        sourcesFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(json, sourcesFile);

        // WHEN we try to parse it:
        try {
            new UpdateManager(sourcesFile);
            fail("Expected MalformedURLException but didn't get one!");
        }
        catch (JsonSyntaxException ignored) {
        }
    }

    @Test
    public void addUpdateSource_withSave_shouldUpdateAndSave() throws Exception {
        // GIVEN an initially empty sources json:
        String json = """
                {
                  "applicationName": "Test",
                  "updateSources": [
                  ]
                }
                """;
        File sourcesFile = File.createTempFile("UpdateManagerTest", "json");
        sourcesFile.deleteOnExit();
        FileSystemUtil.writeStringToFile(json, sourcesFile);
        UpdateManager manager = new UpdateManager(sourcesFile);

        // WHEN we add an update source:
        URL baseUrl = new URL("http://www.test.example");
        String versionManifest = "manifest.json";
        String publicKey = "public.key";
        manager.updateSources.addUpdateSource(
                new UpdateSources.UpdateSource("Test source", baseUrl, versionManifest, publicKey));

        // THEN we should see the json was saved correctly:
//        final String expected = """
//                {
//                  "applicationName": "Test",
//                  "updateSources": [
//                    {
//                      "versionManifestUrl": "http://www.test.example/manifest.json",
//                      "publicKeyUrl": "http://www.test.example/public.key"
//                    }
//                  ]
//                }""";
//        assertEquals(expected, FileSystemUtil.readFileToString(sourcesFile));

        // Probably safer to assert on Java objects instead of raw generated json...
        // whitespace, formatting changes, and even the order of keys are all flexible, after all
        UpdateManager manager2 = new UpdateManager(sourcesFile);
        assertEquals("Test", manager2.getApplicationName());
        assertEquals(1, manager.getUpdateSources().size());
        assertEquals("Test source", manager.getUpdateSources().get(0).getName());
        assertEquals("http://www.test.example/manifest.json",
                     manager.getUpdateSources().get(0).getVersionManifestUrl().toString());
        assertEquals("http://www.test.example/public.key",
                     manager.getUpdateSources().get(0).getPublicKeyUrl().toString());
    }

    @Test
    public void resolveUrl_withValidData_shouldResolve() throws Exception {
        // GIVEN valid input:
        URL baseUrl = new URL("http://www.test.example");
        String path = "example.json";

        // WHEN we try to create a proper URL out of it:
        URL actual = UpdateManager.resolveUrl(baseUrl, path);

        // THEN we should see a good URL:
        assertEquals("http://www.test.example/example.json", actual.toString());
    }

    @Test
    public void resolveUrl_withBadData_shouldNotResolve() throws Exception {
        // bad data should crap out:
        assertNull(UpdateManager.resolveUrl(null, null));
        final String url = "http://test.example";
        assertEquals(url, UpdateManager.resolveUrl(new URL(url), null).toString());
    }

    @Test
    public void resolveUrl_withWindowsPath_shouldResolve() throws Exception {
        // GIVEN a path generated on a machine running windows:
        final String url = "http://www.test.example";
        final String path = "a\\b\\c.txt";

        // WHEN we resolve it:
        URL actual = UpdateManager.resolveUrl(new URL(url), path);

        // THEN there should be no surprises:
        assertEquals("http://www.test.example/a/b/c.txt", actual.toString());
    }

    @Test
    public void unresolveUrl_withValidData_shouldUnresolve() throws Exception {
        // GIVEN valid input:
        URL baseUrl = new URL("http://www.test.example/a");
        String path = "b/c.txt";

        // WHEN we resolve and then unresolve it:
        String actual = UpdateManager.unresolveUrl(baseUrl, UpdateManager.resolveUrl(baseUrl, path));

        // THEN we should see it unresolved correctly:
        assertEquals(path, actual);
    }

    @Test
    public void retrieveRemoteFiles_withValidFiles_shouldRetrieve() throws Exception {
        // GIVEN an UpdateManager with a fake listener on it and a remote data source:
        UpdateManagerListener fakeListener = Mockito.mock(UpdateManagerListener.class);
        File remoteDir = buildRemoteDataSource();
        UpdateManager manager = new UpdateManager(remoteDir);
        manager.addUpdateManagerListener(fakeListener);

        // WHEN we use the UpdateSource to retrieve a few files:
        UpdateSources.UpdateSource source = manager.getUpdateSources().get(0);
        manager.retrieveVersionManifest(source);
        manager.retrievePublicKey(source);
        manager.retrieveSignatureFile(
                UpdateManager.resolveUrl(source.getBaseUrl(), "extensions/1.0/extension1.0.sig"));

        // Give it a second to do the retrievals
        Thread.sleep(750);

        // THEN we should see our fake listener got hit with the file results:
        Mockito.verify(fakeListener, Mockito.times(1))
               .signatureFileDownloaded(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(fakeListener, Mockito.times(1)).publicKeyDownloaded(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(fakeListener, Mockito.times(1))
               .versionManifestDownloaded(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(fakeListener, Mockito.never()).downloadFailed(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void executeShutdownHooks_withHooksRegistered_shouldExecute() throws Exception {
        // GIVEN a couple of registered shutdown hooks:
        UpdateManager updateManager = new UpdateManager(buildTestUpdateSources(new File("/"), "Test"));
        ShutdownHook hook1 = Mockito.mock(ShutdownHook.class);
        ShutdownHook hook2 = Mockito.mock(ShutdownHook.class);
        updateManager.registerShutdownHook(hook1);
        updateManager.registerShutdownHook(hook2);

        // WHEN we execute the shutdown hooks via UpdateManager:
        updateManager.executeShutdownHooks();

        // THEN we should see that each go invoked:
        Mockito.verify(hook1, Mockito.times(1)).applicationWillRestart();
        Mockito.verify(hook2, Mockito.times(1)).applicationWillRestart();
    }

    /**
     * Builds up a fake version manifest with a sample extension in the system temp dir
     * and returns the directory where it lives. We can pretend it's a remote update source.
     */
    private File buildRemoteDataSource() throws Exception {
        File remoteDir = Files.createTempDirectory("UpdateManagerTest").toFile();
        VersionManifest.ExtensionVersion extVersion = buildTestExtensionVersion(remoteDir, "1.0");
        VersionManifest.Extension extension = new VersionManifest.Extension();
        extension.addVersion(extVersion);

        VersionManifest.ApplicationVersion appVersion = buildTestApplicationVersion("1.0");
        appVersion.addExtension(extension);

        VersionManifest manifest = buildTestVersionManifest(remoteDir, "Test application");
        manifest.addApplicationVersion(appVersion);

        UpdateSources updateSources = buildTestUpdateSources(remoteDir, "Test application");
        File updateSourcesFile = new File(remoteDir, "updateSources.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileSystemUtil.writeStringToFile(gson.toJson(updateSources), updateSourcesFile);
        return updateSourcesFile;
    }

    /**
     * Builds a simple fake extension with the given version and creates fake jar and signature files for it.
     */
    private VersionManifest.ExtensionVersion buildTestExtensionVersion(File remoteDir, String version)
            throws Exception {
        // Build the directory where it will live, okay if it already exists:
        File extensionDir = new File(remoteDir, "extensions/" + version);
        extensionDir.mkdirs();

        File jarFile = new File(extensionDir, "extension" + version + ".jar");
        FileSystemUtil.writeStringToFile("extension jar file", jarFile);
        File sigFile = new File(extensionDir, "extension" + version + ".sig");
        FileSystemUtil.writeStringToFile("extension signature", sigFile);
        VersionManifest.ExtensionVersion extVersion = new VersionManifest.ExtensionVersion();
        extVersion.setDownloadPath(jarFile.getName());
        extVersion.setSignaturePath(sigFile.getName());
        return extVersion;
    }

    /**
     * Builds a fake application version with the given version string.
     */
    private VersionManifest.ApplicationVersion buildTestApplicationVersion(String version) {
        VersionManifest.ApplicationVersion appVersion = new VersionManifest.ApplicationVersion();
        appVersion.setVersion(version);
        return appVersion;
    }

    /**
     * Builds a fake version manifest with the given application name and some built-in defaults for
     * manifest and public key locations.
     */
    private VersionManifest buildTestVersionManifest(File remoteDir, String applicationName) throws Exception {
        VersionManifest manifest = new VersionManifest();
        manifest.setApplicationName(applicationName);
        manifest.save(new File(remoteDir, "version_manifest.json"));
        File publicKeyFile = new File(remoteDir, "public.key");
        SignatureUtil.savePublicKey(SignatureUtil.generateKeyPair().getPublic(), publicKeyFile);
        return manifest;
    }

    /**
     * Builds a fake UpdateSources with the given application name and built-in defaults for
     * manifest and public key locations.
     */
    private UpdateSources buildTestUpdateSources(File remoteDir, String applicationName) throws Exception {
        UpdateSources.UpdateSource source = new UpdateSources.UpdateSource(applicationName,
                                                                           remoteDir.toURI().toURL(),
                                                                           "version_manifest.json",
                                                                           "public.key");
        UpdateSources updateSources = new UpdateSources(applicationName);
        updateSources.addUpdateSource(source);
        return updateSources;
    }
}