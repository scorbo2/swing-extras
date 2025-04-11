package ca.corbett.extras.demo.panels;

import ca.corbett.extras.dirtree.DirTree;
import ca.corbett.extras.dirtree.DirTreeListener;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.TextField;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;

public class DirTreeDemoPanel extends PanelBuilder {

    private DirTree dirTree;
    private List<FileSystem> fileSystems;
    private ComboField fileSystemCombo;
    private ComboField rootDirCombo;
    private final DirTreeListener dirTreeListener;
    private TextField listenerTextArea;

    public DirTreeDemoPanel() {
        dirTreeListener = new DirTreeListener() {
            @Override
            public void selectionChanged(DirTree source, File selectedDir) {
                String msg = "selectionChanged: new dir is "+selectedDir.getAbsolutePath();
                listenerTextArea.setText(listenerTextArea.getText() + msg + System.lineSeparator());
            }

            @Override
            public void treeLocked(DirTree source, File lockDir) {
                String msg = "treeLocked: lock dir is "+lockDir.getAbsolutePath();
                listenerTextArea.setText(listenerTextArea.getText() + msg + System.lineSeparator());
            }

            @Override
            public void treeUnlocked(DirTree source) {
                String msg = "treeUnlocked";
                listenerTextArea.setText(listenerTextArea.getText() + msg + System.lineSeparator());
            }
        };
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
        if (fileSystems.isEmpty() || getRootNodes(fileSystems.get(0)).isEmpty()) {
            rootNode = new File("/"); // arbitrary fallback; assume linux or linux-like
        }

        // Otherwise, just default to the first available one:
        else {
            rootNode = getRootNodes(fileSystems.get(0)).get(0);
        }

        dirTree = DirTree.createDirTree(rootNode);
        dirTree.setPreferredSize(new Dimension(200,1));

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());
        containerPanel.add(dirTree, BorderLayout.WEST);

        containerPanel.add(buildFormPanel(), BorderLayout.CENTER);

        return containerPanel;
    }

    private FormPanel buildFormPanel() {
        FormPanel formPanel = new FormPanel();

        StringBuilder sb = new StringBuilder();
        sb.append("<html>The <b>DirTree</b> component gives you a read-only view<br>");
        sb.append("onto a file system, with the ability to do a chroot-style<br>");
        sb.append("lock to a specific directory, so that the DirTree only<br>");
        sb.append("shows the contents of that directory.<br><br>");
        sb.append("You can also respond to selection changes as the user<br>");
        sb.append("selects different nodes.</html>");
        LabelField labelField = createSimpleLabelField(sb.toString());
        labelField.setTopMargin(12);
        labelField.setBottomMargin(16);
        formPanel.addFormField(labelField);

        List<String> options = new ArrayList<>();
        for (FileSystem system : fileSystems) {
            options.add(system.getClass().getSimpleName());
        }
        ComboField comboField = new ComboField("File system:", options, 0, false);
        comboField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = ((ComboField)e.getSource()).getSelectedIndex();
                changeFileSystem(selectedIndex);
            }
        });
        formPanel.addFormField(comboField);

        options = new ArrayList<>();
        if (! fileSystems.isEmpty()) {
            for (File file : getRootNodes(fileSystems.get(0))) {
                options.add(file.getAbsolutePath());
            }
        }
        comboField = new ComboField("Root node:", options, 0, false);
        comboField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = ((ComboField)e.getSource()).getSelectedIndex();
                changeRootNode(selectedIndex);
            }
        });
        formPanel.addFormField(comboField);

        CheckBoxField checkBoxField = new CheckBoxField("Allow right-click to lock/unlock the tree", true);
        checkBoxField.setTopMargin(12);
        checkBoxField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dirTree.setAllowLock(((CheckBoxField)e.getSource()).isChecked());
            }
        });
        formPanel.addFormField(checkBoxField);

        checkBoxField = new CheckBoxField("Listen for events", false);
        checkBoxField.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSelected = ((CheckBoxField)e.getSource()).isChecked();
                if (isSelected) {
                    dirTree.addDirTreeListener(dirTreeListener);
                    listenerTextArea.setText("Listening for events..."+System.lineSeparator());
                }
                else {
                    dirTree.removeDirTreeListener(dirTreeListener);
                    listenerTextArea.setText("(listener disabled)"+System.lineSeparator());
                }
            }
        });
        checkBoxField.setBottomMargin(12);
        formPanel.addFormField(checkBoxField);

        listenerTextArea = new TextField("", 65, 8, true);
        ((JTextComponent)listenerTextArea.getFieldComponent()).setEditable(false);
        ((JTextComponent)listenerTextArea.getFieldComponent()).setFont(new Font("Monospaced", Font.PLAIN, 10));
        listenerTextArea.setText("(listener disabled)" + System.lineSeparator());
        formPanel.addFormField(listenerTextArea);

        formPanel.render();
        return formPanel;
    }

    private void changeFileSystem(int index) {
        List<File> rootNodes = getRootNodes(fileSystems.get(index));
        List<String> rootNodesStr = new ArrayList<>();
        for (File file : rootNodes) {
            rootNodesStr.add(file.getAbsolutePath());
        }
        rootDirCombo.setOptions(rootNodesStr, 0);
    }

    private void changeRootNode(int index) {
        List<File> rootNodes = getRootNodes(fileSystems.get(fileSystemCombo.getSelectedIndex()));
        dirTree.lock(rootNodes.get(index));
    }

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

    private List<File> getRootNodes(FileSystem fileSystem) {
        List<File> list = new ArrayList<>();
        for (Path path : fileSystem.getRootDirectories()) {
            list.add(path.toFile());
        }
        return list;
    }
}
