package ca.corbett.extras.demo.panels;

import ca.corbett.extras.dirtree.DirTree;
import ca.corbett.extras.dirtree.DirTreeListener;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.LongTextField;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
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
    private List<FileSystem> fileSystems;
    private ComboField<String> fileSystemCombo;
    private ComboField<String> rootDirCombo;
    private final DirTreeListener dirTreeListener;
    private LongTextField listenerTextArea;

    public DirTreeDemoPanel() {
        dirTreeListener = new LoggingDirTreeListener();
    }

    @Override
    public String getTitle() {
        return "DirTree";
    }

    @Override
    public JPanel build() {
        fileSystems = findFileSystems();
        File rootNode;

        // Weird scenario: we might start up in an environment with no default file system:
        //   (this seems really unlikely, but because of Java's cross-platform nature,
        //    who knows what kind of device or OS we will be launched on)
        if (fileSystems.isEmpty() || getRootNodes(fileSystems.get(0)).isEmpty()) {
            rootNode = new File("/"); // arbitrary fallback; assume linux or linux-like
        }

        // Otherwise, just default to the first available one:
        else {
            rootNode = getRootNodes(fileSystems.get(0)).get(0);
        }

        dirTree = DirTree.createDirTree(rootNode);
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
                "You can also respond to selection changes as the user<br>" +
                "selects different nodes.</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(sb, 14);
        labelField.getMargins().setTop(12).setBottom(16);
        formPanel.add(labelField);

        // We can build a ComboField to represent all the file systems we enumerated earlier:
        List<String> options = new ArrayList<>();
        for (FileSystem system : fileSystems) {
            options.add(system.getClass().getSimpleName());
        }
        fileSystemCombo = new ComboField<>("File system:", options, 0, false);
        fileSystemCombo.addValueChangedListener(field -> {
            int selectedIndex = fileSystemCombo.getSelectedIndex();
            changeFileSystem(selectedIndex);
        });
        formPanel.add(fileSystemCombo);

        // Some file systems can have more than one root node, so let's build up
        // another ComboField to allow selecting between them:
        options = new ArrayList<>();
        if (!fileSystems.isEmpty()) {
            for (File file : getRootNodes(fileSystems.get(0))) {
                options.add(file.getAbsolutePath());
            }
        }
        rootDirCombo = new ComboField<>("Root node:", options, 0, false);
        rootDirCombo.addValueChangedListener(field -> {
            int selectedIndex = rootDirCombo.getSelectedIndex();
            changeRootNode(selectedIndex);
        });
        formPanel.add(rootDirCombo);

        // We can have a simple checkbox option to allow for locking the tree to a specific directory:
        CheckBoxField checkBoxField = new CheckBoxField("Allow right-click to lock/unlock the tree", true);
        checkBoxField.getMargins().setTop(12);
        checkBoxField.addValueChangedListener(field -> {
            dirTree.setAllowLock(((CheckBoxField)field).isChecked());
        });
        formPanel.add(checkBoxField);

        // And another checkbox option to enable or disable our LoggingDirTreeListener:
        checkBoxField = new CheckBoxField("Listen for events", false);
        checkBoxField.addValueChangedListener(field -> {
            boolean isSelected = ((CheckBoxField)field).isChecked();
            if (isSelected) {
                dirTree.addDirTreeListener(dirTreeListener);
                listenerTextArea.setText("Listening for events..." + System.lineSeparator());
            }
            else {
                dirTree.removeDirTreeListener(dirTreeListener);
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
     * Invoked internally when the user changes the selected file system.
     * We respond by setting appropriate options into the root node chooser.
     */
    private void changeFileSystem(int index) {
        List<File> rootNodes = getRootNodes(fileSystems.get(index));
        List<String> rootNodesStr = new ArrayList<>();
        for (File file : rootNodes) {
            rootNodesStr.add(file.getAbsolutePath());
        }
        rootDirCombo.setOptions(rootNodesStr, 0);
    }

    /**
     * Invoked internally when the user changes the selected root node.
     * We respond by pointing the DirTree at the given root node.
     */
    private void changeRootNode(int index) {
        List<File> rootNodes = getRootNodes(fileSystems.get(fileSystemCombo.getSelectedIndex()));
        dirTree.lock(rootNodes.get(index));
    }

    /**
     * Enumerates all the file systems available on this system.
     */
    private List<FileSystem> findFileSystems() {
        List<FileSystem> list = new ArrayList<>();
        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if ("file".equals(provider.getScheme())) {
                list.add(provider.getFileSystem(URI.create("file:///")));
                break;
            }
        }
        return list;
    }

    /**
     * Enumerates all the root nodes for the given file system.
     */
    private List<File> getRootNodes(FileSystem fileSystem) {
        List<File> list = new ArrayList<>();
        for (Path path : fileSystem.getRootDirectories()) {
            list.add(path.toFile());
        }
        return list;
    }

    /**
     * An implementation of the DirTreeListener that will simply log all events
     * into our little log panel.
     */
    private class LoggingDirTreeListener implements DirTreeListener {
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
}
