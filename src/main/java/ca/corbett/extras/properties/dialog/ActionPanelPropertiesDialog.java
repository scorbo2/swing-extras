package ca.corbett.extras.properties.dialog;

import ca.corbett.extras.actionpanel.ActionPanel;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.forms.FormPanel;

import java.awt.Window;
import java.util.List;

public class ActionPanelPropertiesDialog extends PropertiesDialog {

    private final ActionPanel actionPanel;

    public ActionPanelPropertiesDialog(Window owner, String title, List<AbstractProperty> properties) {
        super(owner, title, properties);
        actionPanel = new ActionPanel();
    }

    /**
     * There are a crazy number of options for ActionPanel, so rather than write wrapper methods
     * for each of them, we just expose the ActionPanel directly. Refer to the ActionPanel Javadocs
     * for details on what you can do with it, or refer to the built-in demo application to see
     * examples of it in action! You can make cosmetic/styling changes to the ActionPanel before or
     * after showing the dialog. Adding or removing actions or action groups, however, is not
     * recommended - better to leave that to this class.
     *
     * @return The ActionPanel used in this dialog, which you can customize as needed.
     */
    public ActionPanel getActionPanel() {
        return actionPanel;
    }

    @Override
    protected void populateFormPanels() {

    }

    @Override
    protected void initLayout() {

    }

    @Override
    protected void makeFormPanelVisible(FormPanel formPanel) {

    }
}
