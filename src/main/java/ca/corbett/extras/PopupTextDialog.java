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

    public boolean wasOkayed() {
        return wasOkayed;
    }

    public String getText() {
        return textArea.getText();
    }

    public boolean isReadOnly() {
        return !textArea.isEditable();
    }

    public void setReadOnly(boolean isReadOnly) {
        textArea.setEditable(!isReadOnly);
    }

    public boolean isClipboardEnabled() {
        return copyButton.isVisible();
    }

    public void setClipboardEnabled(boolean isEnabled) {
        copyButton.setVisible(isEnabled);
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
                lastWidth = Math.max(currentSize.width, MIN_WIDTH);
                lastHeight = Math.max(currentSize.height, MIN_HEIGHT);
            }
        });
    }

    protected JScrollPane buildTextArea(String text) {
        textArea = new JTextArea(text);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(LabelField.getDefaultFont());
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    protected JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        copyButton = new JButton("Copy");
        copyButton.setPreferredSize(new Dimension(90, 23));
        copyButton.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()), null);
        });
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
