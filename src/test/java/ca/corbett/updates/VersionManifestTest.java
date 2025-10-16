package ca.corbett.updates;

import ca.corbett.extensions.AppExtensionInfo;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionManifestTest {

    @Test
    public void newVersionManifest_withValidData_shouldGenerateJson() throws Exception {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("Test extension")
                .setVersion("1.0.0")
                .setAuthor("me")
                .setShortDescription("Test")
                .setLongDescription("This is just a test")
                .setTargetAppName("Test")
                .setTargetAppVersion("1.0")
                .build();
        VersionManifest.ExtensionVersion extVersion = new VersionManifest.ExtensionVersion();
        extVersion.setDownloadUrl(new URL("http://www.test.example/someJar.jar"));
        extVersion.setExtInfo(extInfo);

        VersionManifest.Extension extension = new VersionManifest.Extension();
        extension.setName("Test extension");
        extension.addVersion(extVersion);

        VersionManifest.ApplicationVersion appVersion = new VersionManifest.ApplicationVersion();
        appVersion.setVersion("1.0");
        appVersion.addExtension(extension);

        VersionManifest app = new VersionManifest();
        app.setManifestGenerated(Instant.now());
        app.setApplicationName("Test");
        app.addApplicationVersion(appVersion);

        File f = File.createTempFile("ApplicationTest", ".json");
        f.deleteOnExit();
        app.save(f);
        assertTrue(f.exists());
        assertTrue(f.length() > 0);

        // For visual verification:
        //System.out.println(FileSystemUtil.readFileToString(f));

        // Now load it back:
        VersionManifest loaded = VersionManifest.fromJson(f);
        assertNotNull(loaded);
        assertEquals(app.getApplicationName(), loaded.getApplicationName());
        assertEquals(app.getManifestGenerated(), loaded.getManifestGenerated());
    }

}