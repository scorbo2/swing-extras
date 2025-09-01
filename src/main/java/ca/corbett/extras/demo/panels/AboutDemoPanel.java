package ca.corbett.extras.demo.panels;

import ca.corbett.extras.Version;
import ca.corbett.extras.about.AboutInfo;
import ca.corbett.extras.about.AboutPanel;
import ca.corbett.forms.Alignment;

import javax.swing.JPanel;

public class AboutDemoPanel extends PanelBuilder {

    public AboutDemoPanel() {
        Version.aboutInfo.applicationName = Version.NAME + " demo app";
        Version.aboutInfo.releaseNotesLocation = "/swing-extras/releaseNotes.txt";
        Version.aboutInfo.logoDisplayMode = AboutInfo.LogoDisplayMode.AS_IS;
    }

    @Override
    public String getTitle() {
        return "About";
    }

    @Override
    public JPanel build() {
        return new AboutPanel(Version.aboutInfo, Alignment.TOP_LEFT, 24);
    }
}
