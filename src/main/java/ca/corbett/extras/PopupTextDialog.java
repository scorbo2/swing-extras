package ca.corbett.extras;

import ca.corbett.forms.fields.LabelField;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

/**
 * A dialog that can be used to view or edit text in a resizable window.
 * Originally used by LongTextField as the "pop-out" editor, but useful
 * enough to be reusable elsewhere.
 * <p>
 *     This dialog allows resize, and will remember the last size that it
 *     was set to, as long as the application is running. This size is
 *     not persisted, so it is lost when the application exits.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class PopupTextDialog extends JDialog {

    protected static final int MIN_WIDTH = 300;
    protected static final int MIN_HEIGHT = 100;

    protected static int lastWidth = 600;  // arbitrary default for initial display
    protected static int lastHeight = 400; // arbitrary default for initial display
    protected JTextArea textArea;
    protected JButton copyButton;
    protected boolean wasOkayed;

    public PopupTextDialog(Window owner, String title, String text, boolean isModal) {
        super(owner, title);
        setModal(isModal);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setSize(Math.max(lastWidth, MIN_WIDTH), Math.max(lastHeight, MIN_HEIGHT));
        setLocationRelativeTo(owner);
        setResizable(true);
        initComponents(text);

        // Add root handler for ESC to cancel the dialog:
        JRootPane rootPane = getRootPane();
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonHandler(false);
            }
        });
    }

    /**
     * Allow direct access to the JTextArea for custom manipulation.
     * For example, you want a certain font size, color, face, or background color.
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * Returns true if the user clicked the OK button, false if the dialog closed any other way.
     */
    public boolean wasOkayed() {
        return wasOkayed;
    }

    public String getText() {
        return textArea.getText();
    }

    /**
     * Replaces the current text contents with the specified contents.
     */
    public void setText(String newText) {
        textArea.setText(newText);
    }

    public boolean isReadOnly() {
        return !textArea.isEditable();
    }

    /**
     * Allows or disallows editing in the text field.
     */
    public void setReadOnly(boolean isReadOnly) {
        textArea.setEditable(!isReadOnly);
    }

    public boolean isClipboardEnabled() {
        return copyButton.isVisible();
    }

    /**
     * Shows or hides the "copy to clipbard" button.
     */
    public void setClipboardEnabled(boolean isEnabled) {
        copyButton.setVisible(isEnabled);
    }

    /**
     * Copies the current text contents to the clipboard.
     */
    public void copyToClipboard() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), null);
    }

    /**
     * This is invoked internally when the window is resized, so the dialog can remember the new size
     * for the next time it's launched. But, it's public, so this is also a sneaky way of setting
     * the size of the dialog before creating a new instance of it.
     * <p>
     * <b>Wait, why not just invoke setSize() after I instantiate the dialog?</b> - a good question.
     * The constructor of this class not only invokes setSize(), but it also invokes setLocationRelativeTo()
     * immediately afterwards. This not only sets the size of the dialog before showing it, but also ensures
     * that the dialog will pop up nicely centered over the owning window. If you instantiate an instance
     * of this dialog and THEN invoke setSize() on it, the dialog will no longer be nicely centered.
     * To fix this, you either have to invoke setLocationRelativeTo() yourself after setSize(), or just
     * invoke this setSavedDimensions() method BEFORE you instantiate. Both solutions work, but this
     * one saves you a line of code.
     * </p>
     */
    public static void setSavedDimensions(int width, int height) {
        lastWidth = Math.max(width, MIN_WIDTH);
        lastHeight = Math.max(height, MIN_HEIGHT);
    }

    protected void buttonHandler(boolean isOk) {
        wasOkayed = isOk;
        dispose();
    }

    protected void initComponents(String text) {
        setLayout(new BorderLayout());
        add(buildTextArea(text), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        // Add ComponentListener to track resize events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Update static variables with current size
                Dimension currentSize = getSize();
                setSavedDimensions(Math.max(currentSize.width, MIN_WIDTH), Math.max(currentSize.height, MIN_HEIGHT));
            }
        });
    }

    /**
     * Creates a plain jane JTextArea with word wrap enabled. You can extend this class and override this
     * method if you want something fancier. Or, if you just want to customize the text area a little,
     * you can use getTextArea in this class and tweak it as you need to (custom font or whatever).
     */
    protected JScrollPane buildTextArea(String text) {
        textArea = new JTextArea(text);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(LabelField.getDefaultFont());
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    /**
     * Invoked internally to create the button panel at the bottom with the
     * "copy to clipboard", cancel, and OK buttons.
     */
    protected JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        copyButton = new JButton("Copy");
        copyButton.setPreferredSize(new Dimension(90, 23));
        copyButton.addActionListener(e -> copyToClipboard());
        leftPanel.add(copyButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(e -> buttonHandler(true));
        rightPanel.add(button);
        button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(e -> buttonHandler(false));
        rightPanel.add(button);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }
}
