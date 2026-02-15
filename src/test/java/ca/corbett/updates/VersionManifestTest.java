package ca.corbett.updates;

import ca.corbett.extensions.AppExtensionInfo;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    public void findLatestApplicationVersion_shouldFindLatestVersion() {
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

    @Test
    public void findHighestExtensionVersion_shouldFindHighestVersion() {
        // GIVEN an extension with multiple versions:
        final String extensionJson = """
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
                              "version": "2.0",
                              "targetAppName": "Test",
                              "targetAppVersion": "1.0"
                            },
                            "downloadPath": "extensions/1.0/Test-2.0.jar",
                            "signaturePath": "extensions/1.0/Test-2.0.sig",
                            "screenshots": []
                          },
                          {
                            "extInfo": {
                              "name": "ExtensionTest",
                              "version": "10.0",
                              "targetAppName": "Test",
                              "targetAppVersion": "1.0"
                            },
                            "downloadPath": "extensions/1.0/Test-10.0.jar",
                            "signaturePath": "extensions/1.0/Test-10.0.sig",
                            "screenshots": []
                          }
                        ]
                      }
                      ]
                    }
                  ]
                }
                """;
        VersionManifest manifest = VersionManifest.fromJson(extensionJson);
        VersionManifest.ApplicationVersion appVersion = manifest.getApplicationVersions().get(0);
        VersionManifest.Extension extension = appVersion.getExtensions().get(0);

        // WHEN we ask for the highest extension version:
        VersionManifest.ExtensionVersion extVersion = extension.getHighestVersion().orElse(null);

        // THEN we should see the highest one:
        assertNotNull(extVersion);
        assertEquals("10.0", extVersion.getExtInfo().getVersion());
    }

    @Test
    public void getApplicationVersionsForMajorVersion_shouldReturnCorrectVersions() {
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
                      "version": "1.5",
                      "extensions": [ ]
                    },
                    {
                      "version": "2.0",
                      "extensions": [ ]
                    }
                  ]
                }
                """;
        VersionManifest manifest = VersionManifest.fromJson(manifestJson);

        // WHEN we ask for versions for major version 1:
        var versions = manifest.getApplicationVersionsForMajorVersion(1);

        // THEN we should see the correct versions:
        assertEquals(2, versions.size());
        assertEquals("1.0", versions.get(0).getVersion());
        assertEquals("1.5", versions.get(1).getVersion());
    }

    @Test
    public void getUniqueExtensionNames_shouldReturnCorrectNames() {
        // GIVEN a manifest with multiple extensions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [
                        { "name": "ExtA", "versions": [] },
                        { "name": "ExtB", "versions": [] }
                      ]
                    },
                    {
                      "version": "2.0",
                      "extensions": [
                        { "name": "ExtB", "versions": [] },
                        { "name": "ExtC", "versions": [] }
                      ]
                    }
                  ]
                }
                """;
        VersionManifest manifest = VersionManifest.fromJson(manifestJson);

        // WHEN we ask for unique extension names:
        var names = manifest.getUniqueExtensionNames();

        // THEN we should see the correct names:
        assertEquals(3, names.size());

        // AND they should be sorted alphabetically:
        assertEquals("ExtA", names.get(0));
        assertEquals("ExtB", names.get(1));
        assertEquals("ExtC", names.get(2));
    }

    @Test
    public void getUniqueExtensionNamesForMajorVersion_shouldReturnCorrectNames() {
        // GIVEN a manifest with multiple extensions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [
                        { "name": "ExtB", "versions": [] },
                        { "name": "ExtA", "versions": [] }
                      ]
                    },
                    {
                      "version": "2.0",
                      "extensions": [
                        { "name": "ExtB", "versions": [] },
                        { "name": "ExtC", "versions": [] }
                      ]
                    }
                  ]
                }
                """;
        VersionManifest manifest = VersionManifest.fromJson(manifestJson);

        // WHEN we ask for unique extension names for major version 1:
        var names = manifest.getUniqueExtensionNamesForMajorVersion(1);

        // THEN we should see the correct names:
        assertEquals(2, names.size());

        // AND they should be sorted alphabetically:
        assertEquals("ExtA", names.get(0));
        assertEquals("ExtB", names.get(1));
    }

    @Test
    public void getHighestVersionForExtension_shouldReturnCorrectVersion() {
        // GIVEN a manifest with multiple extensions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [
                        {
                          "name": "ExtA",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "1.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-1.0.jar",
                              "signaturePath": "extensions/1.0/ExtA-1.0.sig",
                              "screenshots": []
                            },
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "2.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-2.0.jar",
                              "signaturePath": "extensions/1.0/ExtA-2.0.sig",
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

        // WHEN we ask for the highest version for ExtA:
        VersionManifest.ExtensionVersion extVersion = manifest.getHighestVersionForExtension("ExtA")
                                                              .orElse(null);

        // THEN we should see the correct version:
        assertNotNull(extVersion);
        assertEquals("2.0", extVersion.getExtInfo().getVersion());
    }

    @Test
    public void getHighestVersionForExtensionInMajorAppVersion_shouldReturnCorrectVersion() {
        // GIVEN a manifest with multiple extensions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [
                        {
                          "name": "ExtA",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "1.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-1.0.jar",
                              "signaturePath": "extensions/1.0/ExtA-1.0.sig",
                              "screenshots": []
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "version": "2.0",
                      "extensions": [
                        {
                          "name": "ExtA",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "2.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "2.0"
                              },
                              "downloadPath": "extensions/2.0/ExtA-2.0.jar",
                              "signaturePath": "extensions/2.0/ExtA-2.0.sig",
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

        // WHEN we ask for the highest version for ExtA in major app version 2:
        VersionManifest.ExtensionVersion extVersion = manifest
                .getHighestVersionForExtensionInMajorAppVersion("ExtA", 2)
                .orElse(null);

        // THEN we should see the correct version:
        assertNotNull(extVersion);
        assertEquals("2.0", extVersion.getExtInfo().getVersion());
    }

    @Test
    public void getHighestExtensionVersionsForMajorAppVersion_shouldReturnCorrectVersions() {
        // GIVEN a manifest with multiple extensions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [
                        {
                          "name": "ExtA",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "1.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-1.0.jar",
                              "signaturePath": "extensions/1.0/ExtA-1.0.sig",
                              "screenshots": []
                            },
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "1.5",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-1.5.jar",
                              "signaturePath": "extensions/1.0/ExtA-1.5.sig",
                              "screenshots": []
                            }
                          ]
                        },
                        {
                          "name": "ExtB",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtB",
                                "version": "2.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtB-2.0.jar",
                              "signaturePath": "extensions/1.0/ExtB-2.0.sig",
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

        // WHEN we ask for the highest versions for major app version 1:
        var extVersions = manifest.getHighestExtensionVersionsForMajorAppVersion(1);

        // THEN we should see the correct versions:
        assertEquals(2, extVersions.size());
        assertEquals("ExtA", extVersions.get(0).getExtInfo().getName());
        assertEquals("1.5", extVersions.get(0).getExtInfo().getVersion());
        assertEquals("ExtB", extVersions.get(1).getExtInfo().getName());
        assertEquals("2.0", extVersions.get(1).getExtInfo().getVersion());
    }

    @Test
    public void getHighestExtensionVersions_shouldReturnCorrectVersions() {
        // GIVEN a manifest with multiple extensions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [
                        {
                          "name": "ExtA",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "1.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-1.0.jar",
                              "signaturePath": "extensions/1.0/ExtA-1.0.sig",
                              "screenshots": []
                            },
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "1.5",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-1.5.jar",
                              "signaturePath": "extensions/1.0/ExtA-1.5.sig",
                              "screenshots": []
                            }
                          ]
                        },
                        {
                          "name": "ExtB",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtB",
                                "version": "2.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtB-2.0.jar",
                              "signaturePath": "extensions/1.0/ExtB-2.0.sig",
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

        // WHEN we grab an ApplicationVersion and ask for its highest extension versions:
        VersionManifest.ApplicationVersion appVersion = manifest.getApplicationVersions().get(0);
        var extVersions = appVersion.getHighestExtensionVersions();

        // THEN we should see the correct versions:
        assertEquals(2, extVersions.size());
        assertEquals("ExtA", extVersions.get(0).getExtInfo().getName());
        assertEquals("1.5", extVersions.get(0).getExtInfo().getVersion());
        assertEquals("ExtB", extVersions.get(1).getExtInfo().getName());
        assertEquals("2.0", extVersions.get(1).getExtInfo().getVersion());
    }

    @Test
    public void findExtensionForExtensionVersion_shouldReturnCorrectExtension() {
        // GIVEN a manifest with multiple extensions:
        final String manifestJson = """
                {
                  "manifestGenerated": "2025-11-30T05:12:44.276439348Z",
                  "applicationName": "Test",
                  "applicationVersions": [
                    {
                      "version": "1.0",
                      "extensions": [
                        {
                          "name": "ExtA",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "1.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-1.0.jar",
                              "signaturePath": "extensions/1.0/ExtA-1.0.sig",
                              "screenshots": []
                            },
                            {
                              "extInfo": {
                                "name": "ExtA",
                                "version": "1.5",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtA-1.5.jar",
                              "signaturePath": "extensions/1.0/ExtA-1.5.sig",
                              "screenshots": []
                            }
                          ]
                        },
                        {
                          "name": "ExtB",
                          "versions": [
                            {
                              "extInfo": {
                                "name": "ExtB",
                                "version": "2.0",
                                "targetAppName": "Test",
                                "targetAppVersion": "1.0"
                              },
                              "downloadPath": "extensions/1.0/ExtB-2.0.jar",
                              "signaturePath": "extensions/1.0/ExtB-2.0.sig",
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
        VersionManifest.ApplicationVersion appVersion = manifest.getApplicationVersions().get(0); // only one
        VersionManifest.Extension extension = appVersion.getExtensions().get(1); // ExtB
        VersionManifest.ExtensionVersion extVersion = extension.getVersions().get(0); // only one

        // WHEN we ask the manifest to find the Extension for that ExtensionVersion:
        Optional<VersionManifest.Extension> foundExtension = manifest.findExtensionForExtensionVersion(extVersion);

        // THEN we should see the correct extension:
        assertTrue(foundExtension.isPresent());
        assertEquals("ExtB", foundExtension.get().getName());
        assertSame(extension, foundExtension.get()); // it should in fact be the same object
    }
}
