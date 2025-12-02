package ca.corbett.updates;

import ca.corbett.extensions.AppExtensionInfo;
import org.junit.jupiter.api.Test;

import java.io.File;
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
        extVersion.setDownloadPath("someJar.jar");
        extVersion.setSignaturePath("someJar.sig");
        extVersion.addScreenshot("someJar-screenshot1.jpg");
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
        VersionManifest loaded = VersionManifest.fromFile(f);
        assertNotNull(loaded);
        assertEquals(app.getApplicationName(), loaded.getApplicationName());
        assertEquals(app.getManifestGenerated(), loaded.getManifestGenerated());
        assertEquals(app.getApplicationVersions().get(0).getExtensions().get(0).getVersions().get(0).getDownloadPath(),
                     loaded.getApplicationVersions().get(0).getExtensions().get(0).getVersions().get(0)
                           .getDownloadPath());
        assertEquals(app.getApplicationVersions().get(0).getExtensions().get(0).getVersions().get(0).getSignaturePath(),
                     loaded.getApplicationVersions().get(0).getExtensions().get(0).getVersions().get(0)
                           .getSignaturePath());
        assertEquals(
                app.getApplicationVersions().get(0).getExtensions().get(0).getVersions().get(0).getScreenshots().get(0),
                loaded.getApplicationVersions().get(0).getExtensions().get(0).getVersions().get(0)
                      .getScreenshots().get(0));
    }

    @Test
    public void findLatestExtVersion_shouldFindHighestVersion() throws Exception {
        // GIVEN a manifest with an extension with two versions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [
                        {
                          "name": "ExtensionTest",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtensionTest",
                                "version": "1.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/Test-1.0.jar",
                              "signaturePath": "extensions/1.0/Test-1.0.sig",
                              "screenshots": []
                            },
                            {
                              "extInfo": {
                                "name": "ExtensionTest",
                                "version": "1.6",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/Test-1.6.jar",
                              "signaturePath": "extensions/1.0/Test-1.6.sig",
                              "screenshots": []
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """;
        VersionManifest manifest = VersionManifest.fromJson(manifestJson);

        // WHEN we ask it for the latest version:
        VersionManifest.Extension extension = manifest.getApplicationVersions().get(0).getExtensions().get(0);
        VersionManifest.ExtensionVersion extVersion = VersionManifest.findLatestExtVersion(extension);

        // THEN we should see the highest version:
        assertNotNull(extVersion);
        assertEquals("1.6", extVersion.getExtInfo().getVersion());
    }

    @Test
    public void findLatestApplicationVersion_shouldFindLatestVersion() throws Exception {
        // GIVEN a manifest with multiple application versions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [ ]
                    },
                    {
                      "version": "9.9",
                      "extensions": [ ]
                    },
                    {
                      "version": "0.1alpha",
                      "extensions": [ ]
                    }
                  ]
                }
                """;
        VersionManifest manifest = VersionManifest.fromJson(manifestJson);

        // WHEN we ask for the highest app version:
        VersionManifest.ApplicationVersion appVersion = manifest.findLatestApplicationVersion();

        // THEN we should see the highest one:
        assertNotNull(appVersion);
        assertEquals("9.9", appVersion.getVersion());
    }
}