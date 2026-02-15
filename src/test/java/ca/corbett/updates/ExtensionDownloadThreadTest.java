package ca.corbett.updates;

import ca.corbett.extras.io.DownloadManager;
import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.progress.SimpleProgressAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExtensionDownloadThreadTest {

    private static DownloadManager downloadManager;
    private static DownloadedExtension testExtensionFiles;
    private static VersionManifest.ExtensionVersion extensionVersion;
    private static UpdateSources.UpdateSource updateSource;
    private DownloadedExtension testResult;

    @BeforeAll
    public static void setup() throws Exception {
        File tempDirectory = Files.createTempDirectory("ExtensionDownloadThreadTest").toFile();
        tempDirectory.deleteOnExit();
        downloadManager = new DownloadManager();
        extensionVersion = createSampleExtensionVersion();
        createVersionManifest(tempDirectory, extensionVersion);
        testExtensionFiles = createSampleExtensionFiles(tempDirectory);
        updateSource = createSampleUpdateSource(tempDirectory);
    }

    @Test
    public void downloadAll_shouldDownloadAllFiles() throws Exception {
        // GIVEN an ExtensionDownloadThread with default settings:
        testResult = null;
        ExtensionDownloadThread worker = new ExtensionDownloadThread(downloadManager, updateSource, extensionVersion);
        worker.addProgressListener(new DownloadProgressListener(worker));

        // WHEN we execute it:
        Thread thread = new Thread(worker);
        thread.start();
        thread.join();

        // THEN it should have downloaded all files:
        assertNotNull(testResult);
        assertNotNull(testResult.getJarFile());
        assertNotNull(testResult.getSignatureFile());
        assertNotNull(testResult.getScreenshots());
        assertEquals(testExtensionFiles.getScreenshots().size(), testResult.getScreenshots().size());
        assertEquals(testExtensionFiles.getJarFile().getName(), testResult.getJarFile().getName());
        assertEquals(testExtensionFiles.getSignatureFile().getName(), testResult.getSignatureFile().getName());
    }

    @Test
    public void downloadJarOnly_shouldIgnoreOtherFiles() throws Exception {
        // GIVEN an ExtensionDownloadThread set to download only the extension jar:
        testResult = null;
        ExtensionDownloadThread worker = new ExtensionDownloadThread(downloadManager, updateSource, extensionVersion);
        worker.setDownloadOptions(ExtensionDownloadThread.Options.JarOnly);
        worker.addProgressListener(new DownloadProgressListener(worker));

        // WHEN we execute it:
        Thread thread = new Thread(worker);
        thread.start();
        thread.join();

        // THEN it should have downloaded only the jar file:
        assertNotNull(testResult);
        assertNotNull(testResult.getJarFile());
        assertEquals(testExtensionFiles.getJarFile().getName(), testResult.getJarFile().getName());
        assertNull(testResult.getSignatureFile());
        assertEquals(0, testResult.getScreenshots().size());
    }

    @Test
    public void downloadScreenshotsOnly_shouldIgnoreOtherFiles() throws Exception {
        // GIVEN an ExtensionDownloadThread set to download only the screenshots:
        testResult = null;
        ExtensionDownloadThread worker = new ExtensionDownloadThread(downloadManager, updateSource, extensionVersion);
        worker.setDownloadOptions(ExtensionDownloadThread.Options.ScreenshotsOnly);
        worker.addProgressListener(new DownloadProgressListener(worker));

        // WHEN we execute it:
        Thread thread = new Thread(worker);
        thread.start();
        thread.join();

        // THEN it should have downloaded only the screenshots:
        assertNotNull(testResult);
        assertNull(testResult.getJarFile());
        assertNull(testResult.getSignatureFile());
        assertEquals(testExtensionFiles.getScreenshots().size(), testResult.getScreenshots().size());
    }

    private static DownloadedExtension createSampleExtensionFiles(File tempDir) throws Exception {
        File jarFile = new File(tempDir, "extension.jar");
        File sigFile = new File(tempDir, "extension.sig");
        File screen1 = new File(tempDir, "extension_1.jpg");
        File screen2 = new File(tempDir, "extension_2.jpg");
        FileSystemUtil.writeStringToFile("hello", jarFile);
        FileSystemUtil.writeStringToFile("hello", sigFile);
        FileSystemUtil.writeStringToFile("hello", screen1);
        FileSystemUtil.writeStringToFile("hello", screen2);
        jarFile.deleteOnExit();
        sigFile.deleteOnExit();
        screen1.deleteOnExit();
        screen2.deleteOnExit();
        DownloadedExtension ext = new DownloadedExtension();
        ext.setJarFile(jarFile);
        ext.setSignatureFile(sigFile);
        ext.addScreenshot(screen1);
        ext.addScreenshot(screen2);
        return ext;
    }

    private static VersionManifest.ExtensionVersion createSampleExtensionVersion() {
        VersionManifest.ExtensionVersion extVersion = new VersionManifest.ExtensionVersion();
        extVersion.setDownloadPath("extension.jar");
        extVersion.setSignaturePath("extension.sig");
        extVersion.addScreenshot("extension_1.jpg");
        extVersion.addScreenshot("extension_2.jpg");
        return extVersion;
    }

    private static void createVersionManifest(File tempDir, VersionManifest.ExtensionVersion extVersion)
            throws Exception {
        VersionManifest manifest = new VersionManifest();
        VersionManifest.ApplicationVersion appVersion = new VersionManifest.ApplicationVersion();
        VersionManifest.Extension extension = new VersionManifest.Extension();
        extension.addVersion(extVersion);
        appVersion.addExtension(extension);
        manifest.addApplicationVersion(appVersion);
        File manifestFile = new File(tempDir, "manifest.json");
        manifest.save(manifestFile);
        manifestFile.deleteOnExit();
    }

    private static UpdateSources.UpdateSource createSampleUpdateSource(File tempDir) throws Exception {
        URL baseUrl = new URL("file:" + tempDir.getAbsolutePath());
        return new UpdateSources.UpdateSource("test", baseUrl, "manifest.json");
    }

    private class DownloadProgressListener extends SimpleProgressAdapter {

        private final ExtensionDownloadThread worker;

        public DownloadProgressListener(ExtensionDownloadThread worker) {
            this.worker = worker;
        }

        @Override
        public void progressComplete() {
            testResult = worker.getDownloadedExtension();
        }
    }
}
