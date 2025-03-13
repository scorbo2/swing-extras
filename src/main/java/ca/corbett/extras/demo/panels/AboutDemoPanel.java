package ca.corbett.extras.demo.panels;

import ca.corbett.extras.Version;
import ca.corbett.extras.about.AboutPanel;

import javax.swing.JPanel;

public class AboutDemoPanel extends PanelBuilder {

    public AboutDemoPanel() {
        Version.aboutInfo.applicationName = Version.NAME + " demo app";
        Version.aboutInfo.releaseNotesText = "This AboutPanel is created by populating a simple AboutInfo object. "
                + "You can then very easily show a consistent AboutPanel for your application with clickable "
                + "links for the project URL and license URL (if the current JRE supports browsing). "
                + "Zero UI code required!\n\n"
                + "This can also be shown in a popup AboutDialog instead of in a panel.\n\n"
                + "AboutInfo info = new AboutInfo()\n"
                + "info.applicationName = \"My Application\";\n"
                + "info.applicationVersion = \"1.0.0\";\n"
                + "info.projectUrl = \"http://www.example.com/myproject\";\n"
                + "// and so on... most properties are optional\n"
                + "// you can also set custom properties\n\n"
                + "new AboutDialog(myMainWindow, info).setVisible(true);";
    }

    @Override
    public String getTitle() {
        return "About";
    }

    @Override
    public JPanel build() {
        return new AboutPanel(Version.aboutInfo);
    }
}
