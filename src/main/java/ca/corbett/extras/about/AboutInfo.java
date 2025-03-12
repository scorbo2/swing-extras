package ca.corbett.extras.about;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Populate an instance of this class and then hand it to AboutDialog or AboutPanel to very quickly
 * and easily create an "about this app" type of display for the user. Most fields here are optional.
 * If present, they will be included in the resulting About view. It's recommended to include a
 * logo image for your application around 480x90 or so and specify it here. If not supplied,
 * the About code will try to generate one automatically based on the applicationName specified here.
 *
 * @author scorbo2
 * @since 2018-02-14
 */
public final class AboutInfo {

    /** Used to determine how to display the application logo image on the AboutPanel. */
    public enum LogoDisplayMode {

        /** Will stretch the logo image to fit its container panel. **/
        STRETCH,

        /** Will display the logo image as-is centered at the top of the panel. **/
        AS_IS,

        /** Same as AS_IS but with a panel border around the container. **/
        AS_IS_WITH_BORDER
    };

    /** The user-friendly name of the application, typically taken from Version.NAME. */
    public String applicationName;

    /** The user-friendly version of the application, typically in "X.Y" format. */
    public String applicationVersion;

    /** The copyright notice for this application, if there is one. */
    public String copyright;

    /**
     * The license name or url for this application, if there is one.
     * If this starts with http:// or https://, the link will be clickable
     * (assuming clickable links are supported and allowed in this JRE).
     */
    public String license;

    /**
     * The project URL for this project, if there is one.
     * If this starts with http:// or https://, the link will be clickable
     * (assuming clickable links are supported and allowed in this JRE).
     */
    public String projectUrl;

    /**
     * The resource url location of the application logo image.
     * Example: "/application_name/images/logo_wide.jpg".
     * If this is null or non-existent, a logo image will automatically
     * be generated using the contents of applicationName.
     */
    public String logoImageLocation;

    /**
     * The resource url location of the release notes text file.
     * Example: "/application_name/ReleaseNotes.txt"
     */
    public String releaseNotesLocation;

    /**
     * You can alternatively specify release notes as a String.
     * If both releaseNotesLocation and releaseNotesText are specified,
     * then releaseNotesText will be used.
     */
    public String releaseNotesText;

    /**
     * An optional user-friendly short description of the application. If null or empty,
     * this field will not be shown at all. Will be truncated at 60 chars for space reasons,
     * so keep it brief.
     */
    public String shortDescription;

    /**
     * How the application logo image should be displayed on the AboutPanel.
     * See LogoDisplayMode for available options. If not specified, the image
     * will be stretched a bit to fit the width of the form, so horizontally
     * rectangular images work best here.
     */
    public LogoDisplayMode logoDisplayMode = LogoDisplayMode.STRETCH;

    /**
     * Indicates whether or not to add a link to open the LogConsole window.
     * Defaults to false. If true, the application logo will be clickable
     * as a bit of an easter egg thing to open the LogConsole.
     */
    public boolean showLogConsole = false;

    /**
     * Custom fields can be specified and will be displayed in a name:value fashion.
     */
    private final Map<String, String> customFieldData;

    /**
     * Internally, this class keeps track of all the AboutPanel instances where we
     * have been displayed, so that if the value of any of our custom fields changes,
     * we can reach out and update them with the new value.
     */
    private final ArrayList<AboutPanel> aboutPanels;

    /**
     * Creates a new, blank AboutInfo instance.
     */
    public AboutInfo() {
        this(false);
    }

    /**
     * Creates a new, blank AboutInfo instance with the given showLogConsole option.
     *
     * @param showLogConsole If true, the logo image on AboutPanel will be clickable.
     */
    public AboutInfo(boolean showLogConsole) {
        customFieldData = new HashMap<>();
        aboutPanels = new ArrayList<>();
        this.showLogConsole = showLogConsole;
    }

    /**
     * Adds a custom field value which will be displayed in the AboutPanel or AboutDialog.
     * Note that you must invoke this with your custom field data BEFORE the AboutPanel or
     * AboutDialog is rendered. Invoking this with a new field after rendering will do nothing.
     * If the value of a custom field has changed since the AboutPanel or AboutDialog was
     * rendered, use updateCustomField instead.
     *
     * @param name Any String which uniquely identifies this field. Will not be shown.
     * @param value The String value to display.
     */
    public void addCustomField(String name, String value) {
        customFieldData.put(name, value);
    }

    /**
     * Modifies the value of a custom field, identified by "name". This can be invoked after this
     * AboutInfo has been rendered, and will update the containing AboutPanel accordingly.
     *
     * @param name The String which identifies the field to update.
     * @param value The new value for the field.
     */
    public void updateCustomField(String name, String value) {
        customFieldData.put(name, value);
        for (AboutPanel panel : aboutPanels) {
            panel.updateCustomFieldValue(name, value);
        }
    }

    /**
     * Returns the current value for the named custom field, if there is one.
     *
     * @param name The String which identifies the field in question.
     * @return The String value of the custom field, or null if no such field.
     */
    public String getCustomFieldValue(String name) {
        return customFieldData.get(name);
    }

    /**
     * Removes all custom fields from this AboutInfo. Note that you must invoke this before
     * the AboutPanel or AboutDialog is rendered, or nothing will happen.
     */
    public void clearCustomFields() {
        customFieldData.clear();
    }

    /**
     * Invoked by an AboutPanel when this AboutInfo is rendered. We track this here so that
     * we can update custom field values if/when necessary. This method has package access
     * so it is not externally available.
     *
     * @param panel The AboutPanel which is rendering this AboutInfo.
     */
    void registerAboutPanel(AboutPanel panel) {
        if (!aboutPanels.contains(panel)) {
            aboutPanels.add(panel);
        }
    }

    /**
     * Returns a list of custom field names.
     *
     * @return A list of custom field names, if any.
     */
    public Set<String> getCustomFieldNames() {
        return customFieldData.keySet();
    }

}
