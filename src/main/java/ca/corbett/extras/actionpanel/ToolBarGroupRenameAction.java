package ca.corbett.extras.actionpanel;

import ca.corbett.forms.SwingFormsResources;

import java.awt.event.ActionEvent;

public class ToolBarGroupRenameAction extends ToolBarAction {

    public ToolBarGroupRenameAction(ActionPanel actionPanel, String groupName) {
        super(actionPanel,
              groupName,
              "Rename this group",
              SwingFormsResources.getRenameIcon(SwingFormsResources.NATIVE_SIZE));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO
    }
}
