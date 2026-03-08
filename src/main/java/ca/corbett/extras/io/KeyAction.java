package ca.corbett.extras.io;

import java.awt.event.ActionEvent;

/**
 * A very simple interface that allows convenient use of lambda expressions for key actions.
 * This is used by KeyStrokeManager to allow you to specify a lambda for the action to perform when a
 * keystroke is triggered, without having to create an actual Action class.
 * <p>
 * Dev note: yes, this is basically just ActionListener, but the overloads for registerHandler()
 * accept Action instances, and the Action class implements ActionListener, which leads to all
 * kinds of confusion with our convenience overloads. So, we use this instead of ActionListener
 * for that purpose, to differentiate them.
 * </p>
 * <p>
 * The vast majority of the time, this will be used in inline lambdas anyway, like this:
 * </p>
 * <pre>
 * // Much more convenient than creating an Action class for something simple like this:
 * keyStrokeManager.registerHandler("Esc", e -> dispose()); // dispose on "Escape"
 * </pre>
 * <p>
 * So the fact that this is KeyAction instead of ActionListener is not a big deal.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
@FunctionalInterface
public interface KeyAction {
    void actionPerformed(ActionEvent e);
}
