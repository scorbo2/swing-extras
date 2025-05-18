package ca.corbett.extras;

import ca.corbett.extras.about.AboutInfo;

/**
 * Contains project-related constants that can be referenced from
 * anywhere in the code as needed.
 */
public final class Version {

    public static final String VERSION = "2.1.1";

    public static final String NAME = "swing-extras";

    public static final String FULL_NAME = NAME + " " + VERSION;

    public static final String PROJECT_URL = "https://github.com/scorbo2/swing-extras";

    public static final String COPYRIGHT = "Copyright © 2012 Steve Corbett";

    public static final String LICENSE = "https://opensource.org/license/mit";

    public static final AboutInfo aboutInfo;

    static {
        aboutInfo = new AboutInfo();
        aboutInfo.license = LICENSE;
        aboutInfo.copyright = COPYRIGHT;
        aboutInfo.projectUrl = PROJECT_URL;
        aboutInfo.logoImageLocation = "/swing-extras/images/swing-extras-logo.jpg";
        aboutInfo.applicationName = Version.NAME;
        aboutInfo.applicationVersion = Version.VERSION;
    }
}
