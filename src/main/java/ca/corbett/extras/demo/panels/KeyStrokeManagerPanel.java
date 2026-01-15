package ca.corbett.extras.demo.panels;

import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.KeyStrokeField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;

/**
 * This demo panel shows off the KeyStrokeManager class,
 * which makes it easy to register and manage keyboard shortcut
 * handlers for any window.
 * <p>
 *     <b>A note about memory management</b> - for this little
 *     demo app, our KeyStrokeManager is created and attached
 *     to the DemoApp window itself, so it effectively lives for the
 *     duration of the application. In a real application, you
 *     should invoke the KeyStrokeManager's {@code dispose()} method
 *     if your KeyStrokeManager is attached to a window that may be
 *     closed and discarded during the application's lifetime.
 *     This will ensure that all references held by the KeyStrokeManager
 *     are released, allowing the window to be garbage collected
 *     properly.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class KeyStrokeManagerPanel extends PanelBuilder {
    private final static Logger log = Logger.getLogger(KeyStrokeManagerPanel.class.getName());
    private final KeyStrokeManager keyManager;
    private FormPanel formPanel;
    private KeyStrokeField customField;
    private MessageUtil messageUtil;
    private ExampleAction userAction;

    // Normally, your application would get these from application settings on startup,
    // or whenever the user changes their shortcut preferences. For this demo, we'll
    // just hard-code them here:
    private final KeyStroke exampleKeyStroke1 = KeyStrokeManager.parseKeyStroke("Ctrl+1");
    private final KeyStroke exampleKeyStroke2 = KeyStrokeManager.parseKeyStroke("Alt+Shift+X");


    public KeyStrokeManagerPanel() {
        keyManager = new KeyStrokeManager(DemoApp.getInstance()); // Safe to call here. DemoApp instance is ready.

        // Create a couple of example actions:
        Action action1 = new ExampleAction("Example1", exampleKeyStroke1);
        Action action2 = new ExampleAction("Example2", exampleKeyStroke2);

        // Most keyboard shortcuts are set up in advance in code, like so:
        keyManager.registerHandler(exampleKeyStroke1, action1);
        keyManager.registerHandler(exampleKeyStroke2, action2);
        // The action that you supply can also be given to a JMenuItem or JButton,
        // so that the same action can be invoked either by clicking a button/menu item,
        // or by pressing the keyboard shortcut. Disabling the action will also
        // automatically disable the menu item/button, and the keyboard shortcut!
        // Reassigning the keyboard shortcut will also automatically update the menu item/button!
        // It's just that easy!
    }

    @Override
    public String getTitle() {
        return "KeyStrokeManager";
    }

    @Override
    public JPanel build() {
        formPanel = buildFormPanel("KeyStrokeManager");

        String sb = "<html>The <b>KeyStrokeManager</b> provides a way to very easily<br>" +
                "add keyboard shortcut handlers to any window.<br>" +
                "Try it out with one of the example shortcuts below,<br>" +
                "or try adding your own on the fly!</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(16);
        formPanel.add(labelField);

        // Let's show our example actions in a couple of LabelFields:
        formPanel.add(new LabelField("Example 1:", KeyStrokeManager.keyStrokeToString(exampleKeyStroke1)));
        formPanel.add(new LabelField("Example 2:", KeyStrokeManager.keyStrokeToString(exampleKeyStroke2)));

        // We can allow custom entry with our KeyStrokeField:
        customField = new KeyStrokeField("Custom:", null);
        customField.setHelpText("<html>Enter a custom keyboard shortcut string here!<br>" +
                                        "Optional modifiers: ctrl, shift, alt, meta, command<br>" +
                                        "Special keys: enter, space, tab, esc, pageup, F1, and so on.<br>" +
                                        "Use the same format as the examples above.</html>");

        // Let's "reserve" our example shortcuts so the user can't accidentally
        // try to register them again. Note that this is optional! It's perfectly
        // valid to have more than one action registered for the same shortcut,
        // if you want to. But we have the option to prevent it, like this:
        customField.setReservedKeyStrokes(List.of(exampleKeyStroke1, exampleKeyStroke2),
                                          "This shortcut is already in use by an example action.");

        formPanel.add(customField);

        ButtonField buttonField = new ButtonField(List.of(
                new CustomAction(),
                new SuspendAction(),
                new ResumeAction()
        ));
        buttonField.setButtonPreferredSize(new Dimension(100, 25));
        formPanel.add(buttonField);

        return formPanel;
    }

    /**
     * Invoked internally to register the user-supplied custom shortcut on the fly.
     * Yes! Keyboard shortcuts don't have to be set up in advance, in code!
     * You can define new ones dynamically at runtime!
     */
    private void executeCustomShortcut() {
        // Validate the form (this will show validation message to user if invalid):
        if (!formPanel.isFormValid()) {
            return;
        }

        // We can get the KeyStroke from the KeyStrokeField now:
        // (we know it's valid because we just validated the form)
        KeyStroke customKeyStroke = customField.getKeyStroke();

        // We COULD check to see if there's already a handler for this shortcut,
        // and disallow it if so - this is an application decision. The KeyStrokeManager
        // allows multiple handlers for the same shortcut, so it's up to your application
        // logic to decide whether that's acceptable or not.
        // Example:
        //    List<Action> existingHandlers = keyManager.getActionsForKeyStroke(customKeyStroke);
        // If existingHandlers is not empty, we could reject the new assignment...

        // If our userAction is already registered, we should unregister it first:
        if (userAction != null) {
            keyManager.unregisterHandler(userAction);
        }

        // Create a new action for this shortcut and register it!
        userAction = new ExampleAction("User-supplied", customKeyStroke);
        keyManager.registerHandler(customKeyStroke, userAction);

        getMessageUtil().info("Shortcut Registered",
                              "The custom shortcut '" +
                                      KeyStrokeManager.keyStrokeToString(customKeyStroke) +
                                      "' has been registered successfully!\n\n"
                                      + "Close this dialog, then try pressing it to see it in action!");
    }

    /**
     * A quick action for our "Register Custom Shortcut" button.
     */
    private class CustomAction extends AbstractAction {

        public CustomAction() {
            super("Register");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            executeCustomShortcut();
        }
    }

    /**
     * You can "suspend" (disable) a KeyStrokeManager at any time.
     * This will disable all registered shortcuts until it is resumed.
     * This does not affect the registered handlers; they remain registered,
     * but they will not be invoked while the KeyStrokeManager is suspended.
     */
    private class SuspendAction extends AbstractAction {

        public SuspendAction() {
            super("Disable");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            keyManager.suspend();
            getMessageUtil().info("Keyboard Shortcuts disabled",
                                  "All keyboard shortcuts are now disabled.");
        }
    }

    /**
     * "Resuming" (enabling) a KeyStrokeManager will re-enable
     * all previously registered shortcuts.
     */
    private class ResumeAction extends AbstractAction {

        public ResumeAction() {
            super("Enable");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            keyManager.resume();
            getMessageUtil().info("Keyboard Shortcuts enabled",
                                  "All keyboard shortcuts are now enabled.");
        }
    }

    /**
     * A silly example action that will just pop up an info dialog
     * when its shortcut is invoked, identifying the keystroke that
     * was used to invoke it.
     */
    private class ExampleAction extends AbstractAction {

        private final String name;
        private final KeyStroke keyStroke;
        private final String keyStrokeString;

        public ExampleAction(String name, KeyStroke keyStroke) {
            super(name);
            this.name = name;
            this.keyStroke = keyStroke;
            this.keyStrokeString = KeyStrokeManager.keyStrokeToString(keyStroke);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getMessageUtil().info("Shortcut Invoked",
                                  "The " + name + " handler for shortcut '" + keyStrokeString + "' was invoked!");
        }
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(DemoApp.getInstance(), log);
        }
        return messageUtil;
    }
}
