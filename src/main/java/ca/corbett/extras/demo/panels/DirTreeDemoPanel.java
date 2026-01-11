package ca.corbett.extras.demo.panels;

import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.dirtree.DirTree;
import ca.corbett.extras.dirtree.DirTreeListener;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
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

    public DirTreeDemoPanel() {
        loggingDirTreeListener = new LoggingDirTreeListener();
        annoyingDirTreeListener = new AnnoyingPromptListener();
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
        checkBoxField = new CheckBoxField("Listen for events", false);
        checkBoxField.addValueChangedListener(field -> {
            boolean isSelected = ((CheckBoxField)field).isChecked();
            if (isSelected) {
                dirTree.addDirTreeListener(loggingDirTreeListener);
                listenerTextArea.setText("Listening for events..." + System.lineSeparator());
            }
            else {
                dirTree.removeDirTreeListener(loggingDirTreeListener);
                listenerTextArea.setText("(listener disabled)" + System.lineSeparator());
            }
        });
        checkBoxField.getMargins().setBottom(12);
        formPanel.add(checkBoxField);

        listenerTextArea = LongTextField.ofDynamicSizingMultiLine("", 8);
        listenerTextArea.getTextArea().setEditable(false);
        listenerTextArea.getTextArea().setFont(new Font("Monospaced", Font.PLAIN, 10));
        listenerTextArea.setText("(listener disabled)" + System.lineSeparator());
        formPanel.add(listenerTextArea);

        return formPanel;
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
            String msg = "selectionChanged: new dir is " + selectedDir.getAbsolutePath();
            listenerTextArea.setText(listenerTextArea.getText() + msg + "\n");
        }

        @Override
        public void treeLocked(DirTree source, File lockDir) {
            String msg = "treeLocked: lock dir is " + lockDir.getAbsolutePath();
            listenerTextArea.setText(listenerTextArea.getText() + msg + "\n");
        }

        @Override
        public void treeUnlocked(DirTree source) {
            String msg = "treeUnlocked";
            listenerTextArea.setText(listenerTextArea.getText() + msg + "\n");
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
