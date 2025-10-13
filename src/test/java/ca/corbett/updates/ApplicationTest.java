package ca.corbett.updates;

import ca.corbett.extensions.AppExtensionInfo;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTest {

    @Test
    public void newApplication_withValidData_shouldGenerateJson() throws Exception {
        AppExtensionInfo extInfo = new AppExtensionInfo.Builder("Test extension")
                .setVersion("1.0.0")
                .setAuthor("me")
                .setShortDescription("Test")
                .setLongDescription("This is just a test")
                .setTargetAppName("Test")
                .setTargetAppVersion("1.0")
                .build();
        ExtensionVersion extVersion = new ExtensionVersion();
        extVersion.setDownloadURL(new URL("http://www.test.example/someJar.jar"));
        extVersion.setExtInfo(extInfo);

        Extension extension = new Extension();
        extension.setName("Test extension");
        extension.addVersion(extVersion);

        ApplicationVersion appVersion = new ApplicationVersion();
        appVersion.setVersion("1.0");
        appVersion.addExtension(extension);

        Application app = new Application();
        app.setApplicationName("Test");
        app.addApplicationVersion(appVersion);

        File f = File.createTempFile("ApplicationTest", ".json");
        f.deleteOnExit();
        app.save(f);
        assertTrue(f.exists());
        assertTrue(f.length() > 0);

        // For visual verification:
        //System.out.println(FileSystemUtil.readFileToString(f));
    }

}