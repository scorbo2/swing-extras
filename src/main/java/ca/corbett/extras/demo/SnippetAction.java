package ca.corbett.extras.demo;

import ca.corbett.extras.PopupTextDialog;

import javax.swing.AbstractAction;
import java.awt.Font;
import java.awt.event.ActionEvent;

/**
 * Can be extended to show a read-only code snippet in a popup window.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public abstract class SnippetAction extends AbstractAction {

    protected abstract String getSnippet();

    @Override
    public void actionPerformed(ActionEvent e) {
        PopupTextDialog dialog = new PopupTextDialog(DemoApp.getInstance(), "Code snippet", getSnippet(), false);
        dialog.getTextArea().setLineWrap(false);
        dialog.getTextArea().setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        dialog.setReadOnly(true);
        dialog.setVisible(true);
    }
}
