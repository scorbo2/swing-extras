package ca.corbett.extras.about;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Provides a reusable and consistent AboutDialog that can be used in any application.
 * By specifying an AboutInfo instance customized for your application, a standard
 * AboutDialog can be shown without having to write any code for it in your application.
 * See also AboutPanel if you'd rather embed a JPanel somewhere instead of popping
 * up a dialog.
 *
 * @author scorbo2
 * @since 2018-02-14
 */
public final class AboutDialog extends JDialog implements KeyEventDispatcher {

    private final AboutPanel aboutPanel;

    /**
     * Creates a new AboutDialog instance with the given parent window and application info,
     *
     * @param parent The JComponent that owns this dialog.
     * @param info   An AboutInfo instance containing the description of this application.
     */
    public AboutDialog(Frame parent, AboutInfo info) {
        super(parent, "About " + info.applicationName, JDialog.ModalityType.APPLICATION_MODAL);
        aboutPanel = new AboutPanel(info);
        initComponents();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Updates the memory stats label on the wrapped AboutPanel.
     * This method simply defers to AboutPanel.refreshMemoryStats()
     */
    public void refreshMemoryStats() {
        aboutPanel.refreshMemoryStats();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        }
        super.setVisible(visible);
    }

    /**
     * Overridden to handle memory cleanup. This was originally done in setVisible(false) but
     * it turns out that never gets invoked when the dialog is disposed.
     */
    @Override
    public void dispose() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
        super.dispose();
    }

    /**
     * Invoked internally to set the layout and build the dialog's components.
     */
    private void initComponents() {
        setSize(540, 600);
        setResizable(false);
        setLayout(new BorderLayout());

        add(aboutPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90, 28));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });
        buttonPanel.add(button);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Press ESC or Enter to close and dispose the dialog.
     *
     * @param e the KeyEvent to dispatch
     * @return Whether we handled the key event or not.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (!isActive()) {
            return false; // don't capture keystrokes if this dialog isn't showing.
        }

        if (e.getID() == KeyEvent.KEY_RELEASED) {
            switch (e.getKeyCode()) {

                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_ESCAPE:
                    dispose();
                    break;
            }
        }

        return true;
    }
}
