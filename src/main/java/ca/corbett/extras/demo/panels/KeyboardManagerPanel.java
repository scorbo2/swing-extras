package ca.corbett.extras.demo.panels;

import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.io.KeyboardManager;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;

/**
 * This demo panel shows off the KeyboardManager class,
 * which makes it easy to register and manage keyboard shortcut
 * handlers for any window.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class KeyboardManagerPanel extends PanelBuilder {
    private final static Logger log = Logger.getLogger(KeyboardManagerPanel.class.getName());
    private final KeyboardManager keyManager;
    private ShortTextField customField;
    private MessageUtil messageUtil;
    private ExampleAction userAction;

    public KeyboardManagerPanel() {
        keyManager = new KeyboardManager(DemoApp.getInstance()); // Safe to call here. DemoApp instance is ready.

        // Most keyboard shortcuts are set up in advance in code, like so:
        keyManager.registerHandler("Ctrl+1", new ExampleAction("Example1", "Ctrl+1"));
        keyManager.registerHandler("Alt+Shift+X", new ExampleAction("Example2", "Alt+Shift+X"));
        // The action that you supply can also be given to a JMenuItem or JButton,
        // so that the same action can be invoked either by clicking a button/menu item,
        // or by pressing the keyboard shortcut. Disabling the action will also
        // automatically disable the menu item/button, and the keyboard shortcut!
        // Reassigning the keyboard shortcut will also automatically update the menu item/button!
        // It's just that easy!
    }

    @Override
    public String getTitle() {
        return "KeyboardManager";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("KeyboardManager");

        String sb = "<html>The <b>KeyboardManager</b> provides a way to very easily<br>" +
                "add keyboard shortcut handlers to any window.<br>" +
                "Try it out with one of the example shortcuts below,<br>" +
                "or try adding your own on the fly!</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(16);
        formPanel.add(labelField);

        formPanel.add(new LabelField("Example 1:", "Ctrl+1"));
        formPanel.add(new LabelField("Example 2:", "Alt+Shift+X"));

        customField = new ShortTextField("Custom:", 15);
        customField.setHelpText("<html>Enter a custom keyboard shortcut string here!<br>" +
                                        "Optional modifiers: ctrl, shift, alt, meta, command<br>" +
                                        "Special keys: enter, space, tab, esc, pageup, F1, and so on.<br>" +
                                        "Use the same format as the examples above.</html>");
        formPanel.add(customField);

        ButtonField buttonField = new ButtonField(List.of(new CustomAction()));
        buttonField.setButtonPreferredSize(new Dimension(210, 25));
        formPanel.add(buttonField);

        return formPanel;
    }

    /**
     * Invoked internally to register the user-supplied custom shortcut on the fly.
     * Yes! Keyboard shortcuts don't have to be set up in advance, in code!
     * You can define new ones dynamically at runtime!
     */
    private void executeCustomShortcut() {
        // Get the custom shortcut string from the text field:
        String shortcutString = customField.getText();

        // Make sure it's valid!
        if (!keyManager.isKeyStrokeValid(shortcutString)) {
            getMessageUtil().error("Invalid Shortcut",
                                   "The shortcut string you entered is not valid:\n\n" + shortcutString);
            return;
        }

        // Check to see if there's already a handler for this shortcut.
        // If so, we can let the user decide whether to continue or not.
        // Note that the KeyboardManager class actually allows multiple actions
        // to be registered for the same shortcut. So, if your application has
        // multiple extensions that want to use the same shortcut, they can all
        // register for it, and all their handlers will be invoked when the shortcut
        // is pressed.
        // TODO mention KeyboardManagerField when that class is ready.
        //      The idea is that the user can remap conflicting shortcuts via application settings.
        List<Action> existingHandlers = keyManager.getHandlers(shortcutString);
        if (!existingHandlers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("The shortcut '").append(shortcutString).append("' is already registered for:\n\n");
            for (Action a : existingHandlers) {
                sb.append(" - ").append(a.getValue(Action.NAME)).append("\n");
            }
            sb.append("\nRegistering your new shortcut will unregister the existing handler(s).\n\n" +
                              "Do you want to continue?");
            if (JOptionPane.showConfirmDialog(DemoApp.getInstance(),
                                              sb.toString(),
                                              "Shortcut Already Registered",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                // User chose not to continue.
                return;
            }

            // Unregister existing handlers:
            for (Action a : existingHandlers) {
                keyManager.unregisterHandler(a);
            }
        }

        // If our userAction was already registered, unregister it first:
        if (userAction != null) {
            keyManager.unregisterHandler(userAction);
        }

        // Create a new action for this shortcut:
        userAction = new ExampleAction("User-supplied", shortcutString);

        // Register it!
        keyManager.registerHandler(shortcutString, userAction);

        // Verify it was registered:
        List<Action> handlers = keyManager.getHandlers(shortcutString);
        if (handlers.contains(userAction)) {
            getMessageUtil().info("Shortcut Registered",
                                  "The shortcut '" + shortcutString + "' was successfully registered.\n\n" +
                                          "Close this confirmation dialog, then try pressing it to see it in action!");
        }
        else {
            // This generally shouldn't happen, especially because we validated the keystroke earlier.
            // But, to be diligent, your code shouldn't assume that registerHandler() always works.
            getMessageUtil().error("Registration Failed",
                                   "The shortcut '" + shortcutString +
                                           "' could not be registered for some reason :(");
        }
    }

    /**
     * A quick action for our "Register Custom Shortcut" button.
     */
    private class CustomAction extends AbstractAction {

        public CustomAction() {
            super("Register Custom Shortcut");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            executeCustomShortcut();
        }
    }

    /**
     * A silly example action that will just pop up an info dialog
     * when its shortcut is invoked, identifying the keystroke that
     * was used to invoke it.
     */
    private class ExampleAction extends AbstractAction {

        private final String name;
        private final String shortcutString;

        public ExampleAction(String name, String shortcutString) {
            super(name);
            this.name = name;
            this.shortcutString = shortcutString;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getMessageUtil().info("Shortcut Invoked",
                                  "The " + name + " handler for shortcut '" + shortcutString + "' was invoked!");
        }
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(DemoApp.getInstance(), log);
        }
        return messageUtil;
    }
}
