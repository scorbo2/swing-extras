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
    protected final Window ownerWindow;
    protected JTextArea textArea;
    protected JButton copyButton;
    protected JButton cancelButton;
    protected boolean wasOkayed;

    public PopupTextDialog(Window owner, String title, String text, boolean isModal) {
        super(owner, title);
        setModal(isModal);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setSize(Math.max(lastWidth, MIN_WIDTH), Math.max(lastHeight, MIN_HEIGHT));
        ownerWindow = owner;
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
     * In read-only mode, the Cancel button is hidden as it serves no purpose.
     */
    public void setReadOnly(boolean isReadOnly) {
        textArea.setEditable(!isReadOnly);
        updateCancelButtonVisibility();
    }

    public boolean isClipboardEnabled() {
        return copyButton.isVisible();
    }

    /**
     * Returns the Cancel button component for testing purposes.
     * @return the Cancel button
     */
    JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Shows or hides the "copy to clipboard" button.
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
     * Overridden here so that when the dialog size is programmatically changed, we re-center
     * ourselves over our owner window.
     */
    @Override
    public void setSize(Dimension dim) {
        super.setSize(dim);
        setLocationRelativeTo(ownerWindow);
    }

    protected void buttonHandler(boolean isOk) {
        wasOkayed = isOk;
        dispose();
    }

    /**
     * Updates the visibility of the Cancel button based on whether the dialog is in read-only mode.
     * In read-only mode, the Cancel button is hidden since editing is not possible.
     */
    protected void updateCancelButtonVisibility() {
        if (cancelButton != null) {
            cancelButton.setVisible(!isReadOnly());
        }
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
                lastWidth = Math.max(currentSize.width, MIN_WIDTH);
                lastHeight = Math.max(currentSize.height, MIN_HEIGHT);
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
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(90, 23));
        cancelButton.addActionListener(e -> buttonHandler(false));
        rightPanel.add(cancelButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }
}
