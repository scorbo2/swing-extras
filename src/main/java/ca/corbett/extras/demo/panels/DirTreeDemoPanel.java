package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.dirtree.DirTree;
import ca.corbett.extras.dirtree.DirTreeListener;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

/**
 * A demo panel for the DirTree component, to show off some of its capabilities.
 * DirTree is a pretty handy UI component for allowing users of an application to
 * graphically navigate a file system. You can even (via right-click popup menu)
 * "lock" the DirTree to a specific subdirectory, so that the user can only
 * navigate within that subdirectory.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class DirTreeDemoPanel extends PanelBuilder {

    private DirTree dirTree;
    private final DirTreeListener loggingDirTreeListener;
    private final DirTreeListener annoyingDirTreeListener;
    private LongTextField listenerTextArea;
    private CheckBoxField listenForEventsCheckBox;
    private ComboField<String> colorSchemeField;

    public DirTreeDemoPanel() {
        loggingDirTreeListener = new LoggingDirTreeListener();
        annoyingDirTreeListener = new AnnoyingPromptListener();

        // Listen for Look and Feel changes, so that we can hide our custom color
        // options for Look and Feels that don't work well with our code:
        LookAndFeelManager.addChangeListener(e -> updateColorSchemeVisibility());
    }

    @Override
    public String getTitle() {
        return "DirTree";
    }

    @Override
    public JPanel build() {
        dirTree = new DirTree();
        dirTree.setPreferredSize(new Dimension(200, 1));

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());
        containerPanel.add(dirTree, BorderLayout.WEST);

        containerPanel.add(buildFormPanel(), BorderLayout.CENTER);

        return containerPanel;
    }

    private FormPanel buildFormPanel() {
        FormPanel formPanel = buildFormPanel("DirTree");

        String sb = "<html>The <b>DirTree</b> component gives you a read-only view<br>" +
                "onto a file system, with the ability to do a chroot-style<br>" +
                "lock to a specific directory, so that the DirTree only<br>" +
                "shows the contents of that directory.<br><br>" +
                "On systems with multiple filesystem roots, all roots will be<br>" +
                "shown as top-level nodes. On Linux-based systems, the contents<br>" +
                "of \"/\" are shown as top-level nodes.</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(16);
        formPanel.add(labelField);

        // Create this first so we can "log" messages to it as needed:
        listenerTextArea = LongTextField.ofDynamicSizingMultiLine("", 8);
        listenerTextArea.getTextArea().setEditable(false);
        listenerTextArea.getTextArea().setFont(new Font("Monospaced", Font.PLAIN, 10));
        listenerTextArea.setText("(listener disabled)" + System.lineSeparator());

        // Demonstrate the ability to select and scroll to any arbitrary path, even
        // if that path has not yet been lazy-loaded in the tree:
        ButtonField buttonField = new ButtonField(List.of(new SelectAndScrollToAction()));
        buttonField.getFieldLabel().setText("Select path:");
        buttonField.setButtonPreferredSize(new Dimension(100, 25));
        formPanel.add(buttonField);

        // We can have a simple checkbox option to allow for locking the tree to a specific directory:
        CheckBoxField checkBoxField = new CheckBoxField("Allow right-click to lock/unlock the tree", true);
        checkBoxField.getMargins().setTop(12);
        checkBoxField.addValueChangedListener(field -> {
            dirTree.setAllowLock(((CheckBoxField)field).isChecked());
        });
        formPanel.add(checkBoxField);

        // Starting in swing-extras 2.8, you can customize DirTree cosmetic properties:
        formPanel.add(buildColorCustomizerField());

        // And another checkbox for showing/hiding hidden directories: (new in swing-extras 2.7!)
        checkBoxField = new CheckBoxField("Show hidden directories", true);
        checkBoxField.addValueChangedListener(field -> {
            boolean isSelected = ((CheckBoxField)field).isChecked();
            dirTree.setShowHiddenDirs(isSelected);
        });
        formPanel.add(checkBoxField);

        // And another checkbox to add a confirmation prompt on every selection change: (new in swing-extras 2.7!)
        checkBoxField = new CheckBoxField("Prompt to confirm selection changes", false);
        checkBoxField.addValueChangedListener(field -> {
            boolean isSelected = ((CheckBoxField)field).isChecked();
            if (isSelected) {
                dirTree.addDirTreeListener(annoyingDirTreeListener);
            }
            else {
                dirTree.removeDirTreeListener(annoyingDirTreeListener);
            }
        });
        formPanel.add(checkBoxField);

        // And another checkbox option to enable or disable our LoggingDirTreeListener:
        listenForEventsCheckBox = new CheckBoxField("Listen for events", false);
        listenForEventsCheckBox.addValueChangedListener(field -> updateListenerTextArea());
        listenForEventsCheckBox.getMargins().setBottom(12);
        formPanel.add(listenForEventsCheckBox);

        // Now we can add the text area we created earlier:
        formPanel.add(listenerTextArea);

        return formPanel;
    }

    /**
     * Sets initial text in the listener log area, and also serves to update it
     * when the user toggles the "Listen for events" checkbox.
     */
    private void updateListenerTextArea() {
        if (listenForEventsCheckBox.isChecked()) {
            dirTree.addDirTreeListener(loggingDirTreeListener);
            listenerTextArea.setText("Listening for events..." + System.lineSeparator());
        }
        else {
            dirTree.removeDirTreeListener(loggingDirTreeListener);
            listenerTextArea.setText("(listener disabled)" + System.lineSeparator());
        }
    }

    /**
     * Handy utility method to append a message to the listener log area, with a newline.
     */
    private void appendToListenerTextArea(String msg) {
        listenerTextArea.setText(listenerTextArea.getText() + msg + System.lineSeparator());
    }

    /**
     * Builds and returns a color scheme chooser, just for fun.
     * Best practice is to let the Look and Feel decide on colors, so that the DirTree
     * can blend in with the rest of the application. But if your application is doing
     * something highly custom, here's an example of how you can customize it!
     */
    private FormField buildColorCustomizerField() {
        List<String> options = List.of(
                "Use Look and Feel defaults",
                "Matrix - green on black",
                "Got the blues",
                "Hot dog stand!"
        );
        colorSchemeField = new ComboField("Color scheme:", options, 0);
        colorSchemeField.setVisible(dirTree.getTreeCellRenderer() instanceof DefaultTreeCellRenderer);
        colorSchemeField.addValueChangedListener(new ValueChangedListener() {
            @Override
            public void formFieldValueChanged(FormField field) {
                switch (colorSchemeField.getSelectedIndex()) {
                    case 0 -> restoreDefaultColors();
                    case 1 -> setMatrixColors();
                    case 2 -> setBluesColors();
                    case 3 -> setHotDogStandColors();
                }
            }
        });
        return colorSchemeField;
    }

    /**
     * Our color scheme options only work when the DirTree is working with a DefaultTreeCellRenderer.
     * Most of the time, that is the case. However, some Look and Feels might install a renderer
     * of some other type, which will break our code. So, hide the option if we're using
     * such a Look and Feel.
     */
    private void updateColorSchemeVisibility() {
        boolean isOkay = dirTree.getTreeCellRenderer() instanceof DefaultTreeCellRenderer;
        colorSchemeField.setVisible(isOkay);
        if (!isOkay) {
            // Try to revert it as best we can:
            dirTree.setBackground(LookAndFeelManager.getLafColor("Tree.background", Color.WHITE));

            // Let the user know about this sad state of affairs:
            appendToListenerTextArea("Custom color schemes not supported on this Look and Feel :(");
        }
        else {
            // clear previous sad messages to avoid confusion:
            updateListenerTextArea();
        }
    }

    /**
     * Restore our DirTree to using Look and Feel defaults.
     * This is easy to do with the LookAndFeelManager utility class.
     */
    private void restoreDefaultColors() {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)dirTree.getTreeCellRenderer();
        renderer.setBackground(LookAndFeelManager.getLafColor("Tree.background", Color.WHITE));
        renderer.setBackgroundSelectionColor(LookAndFeelManager.getLafColor("Tree.selectionBackground", Color.BLUE));
        renderer.setBackgroundNonSelectionColor(LookAndFeelManager.getLafColor("Tree.textBackground", Color.WHITE));
        renderer.setTextSelectionColor(LookAndFeelManager.getLafColor("Tree.selectionForeground", Color.WHITE));
        renderer.setTextNonSelectionColor(LookAndFeelManager.getLafColor("Tree.textForeground", Color.BLACK));
        dirTree.setBackground(LookAndFeelManager.getLafColor("Tree.background", Color.WHITE));
        dirTree.repaint();
    }

    /**
     * Sets a green-on-black "Matrix" color scheme, just for fun.
     */
    private void setMatrixColors() {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)dirTree.getTreeCellRenderer();
        renderer.setBackground(Color.BLACK);
        renderer.setBackgroundSelectionColor(Color.GREEN.darker());
        renderer.setBackgroundNonSelectionColor(Color.BLACK);
        renderer.setTextSelectionColor(Color.GREEN.brighter());
        renderer.setTextNonSelectionColor(Color.GREEN);
        dirTree.setBackground(Color.BLACK);
        dirTree.repaint();
    }

    /**
     * Sets a blue color scheme, just for fun.
     */
    private void setBluesColors() {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)dirTree.getTreeCellRenderer();
        renderer.setBackground(new Color(0xE0F7FA));
        renderer.setBackgroundSelectionColor(new Color(0x0288D1));
        renderer.setBackgroundNonSelectionColor(new Color(0xE0F7FA));
        renderer.setTextSelectionColor(Color.WHITE);
        renderer.setTextNonSelectionColor(new Color(0x01579B));
        dirTree.setBackground(new Color(0xE0F7FA));
        dirTree.repaint();
    }

    /**
     * Sets a hot dog stand color scheme, just for fun. Because who doesn't like a good hot dog stand?
     */
    private void setHotDogStandColors() {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)dirTree.getTreeCellRenderer();
        renderer.setBackground(new Color(0xFFF3E0));
        renderer.setBackgroundSelectionColor(new Color(0xFF5722));
        renderer.setBackgroundNonSelectionColor(new Color(0xFFF3E0));
        renderer.setTextSelectionColor(Color.WHITE);
        renderer.setTextNonSelectionColor(new Color(0xBF360C));
        dirTree.setBackground(new Color(0xFFF3E0));
        dirTree.repaint();
    }

    /**
     * An annoying implementation of DirTreeListener that demonstrates the ability
     * to listen for (and veto!) selection changes within the DirTree.
     *
     * @since 2.7
     */
    private class AnnoyingPromptListener implements DirTreeListener {
        @Override
        public boolean selectionWillChange(DirTree source, File newSelectedDir) {
            return JOptionPane.showConfirmDialog(DemoApp.getInstance(),
                                                 "Are you sure you wish to change directories?",
                                                 "Confirm selection change",
                                                 JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        @Override
        public void selectionChanged(DirTree source, File selectedDir) {
        }

        @Override
        public void showHiddenFilesChanged(DirTree source, boolean showHiddenFiles) {

        }

        @Override
        public void treeLocked(DirTree source, File lockDir) {
        }

        @Override
        public void treeUnlocked(DirTree source) {
        }
    }

    /**
     * An implementation of the DirTreeListener that will simply log all events
     * into our little log panel.
     */
    private class LoggingDirTreeListener implements DirTreeListener {
        @Override
        public boolean selectionWillChange(DirTree source, File newSelectedDir) {
            return true;
        }

        @Override
        public void selectionChanged(DirTree source, File selectedDir) {
            appendToListenerTextArea("selectionChanged: new dir is " + selectedDir.getAbsolutePath());
        }

        @Override
        public void showHiddenFilesChanged(DirTree source, boolean showHiddenFiles) {
            appendToListenerTextArea("showHiddenFilesChanged: now "
                                             + (showHiddenFiles ? "showing" : "hiding")
                                             + " hidden files");
        }

        @Override
        public void treeLocked(DirTree source, File lockDir) {
            appendToListenerTextArea("treeLocked: lock dir is " + lockDir.getAbsolutePath());
        }

        @Override
        public void treeUnlocked(DirTree source) {
            appendToListenerTextArea("treeUnlocked");
        }
    }

    /**
     * A quick action to demo the ability to select and scroll to an arbitrary path
     * within the DirTree, even if that path has not yet been lazy-loaded.
     */
    private class SelectAndScrollToAction extends AbstractAction {

        public SelectAndScrollToAction() {
            super("Choose");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String path = JOptionPane.showInputDialog(DemoApp.getInstance(),
                                                      "Enter any path to select and scroll to:",
                                                      "Select path",
                                                      JOptionPane.QUESTION_MESSAGE);
            if (path != null && !path.isBlank()) {
                File pathFile = new File(path);
                if (!dirTree.selectAndScrollTo(pathFile)) {
                    JOptionPane.showMessageDialog(DemoApp.getInstance(),
                                                  "The path \"" + path + "\" could not be found in the DirTree.",
                                                  "Path not found",
                                                  JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
}
