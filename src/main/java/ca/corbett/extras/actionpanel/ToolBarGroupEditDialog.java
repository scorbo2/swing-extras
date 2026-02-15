package ca.corbett.extras.actionpanel;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ListField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * A dialog for editing an ActionGroup within an ActionPanel. This is shown when the user clicks
 * the "edit group" button in the ToolBar for an ActionGroup. The dialog allows the user to
 * rename the group, reorder the actions within the group, and remove actions from the group,
 * depending on the permissions set in ToolBarOptions.
 * <p>
 * This class is package-private and is only used internally by ToolBarOptions.
 * Callers can access this functionality by going through the ToolBarOptions class:
 * </p>
 * <pre>
 *     // Enabled by default, but you can turn it off if unneeded:
 *     myActionPanel.getToolBarOptions().setAllowGroupEdit(false);
 *
 *     // To supply your own custom group edit action if you don't like the built-in one:
 *     myActionPanel.getToolBarOptions().addCustomActionSupplier(...);
 * </pre>
 * <p>
 * Alternatively, you can disable the ToolBar altogether:
 * </p>
 * <pre>
 *     myActionPanel.setToolBarEnabled(false); // hides all ToolBar actions
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.8 (copied from ext-iv-ice's QuickTagGroupEditDialog, and modified for swing-extras)
 */
class ToolBarGroupEditDialog extends JDialog {
    private static final Logger log = Logger.getLogger(ToolBarGroupEditDialog.class.getName());

    private final ActionPanel actionPanel;
    private final List<EnhancedAction> modifiedList = new ArrayList<>();
    private boolean listModified = false;
    private final String groupName;
    private final ActionGroup group;
    private final ToolBarOptions options;
    private String modifiedGroupName;
    private boolean wasOkayed = false;
    private FormPanel formPanel;
    private ShortTextField nameField;
    private ListField<EnhancedAction> listField;
    private final KeyStrokeManager keyStrokeManager;

    public ToolBarGroupEditDialog(Window owner, ActionPanel actionPanel, String groupName) {
        super(owner, "Edit group: " + groupName, ModalityType.APPLICATION_MODAL);
        this.actionPanel = actionPanel;
        this.options = actionPanel.getToolBarOptions();
        this.groupName = groupName;
        this.group = actionPanel.getGroup(groupName);
        this.keyStrokeManager = new KeyStrokeManager(this);
        setupKeyboardShortcuts();
        if (this.group == null) {
            // This should never happen - the edit button shouldn't even be visible if the group doesn't exist.
            log.severe("ToolBarGroupEditDialog.<init> - group doesn't exist: \"" + groupName + "\"");
            throw new IllegalArgumentException("Group doesn't exist: \"" + groupName + "\"");
        }

        modifiedGroupName = groupName; // default to the current name, in case the user doesn't change it
        setSize(new Dimension(660, 430));
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
        addWindowListener(new WindowCloseListener(keyStrokeManager));
    }

    public boolean wasOkayed() {
        return wasOkayed;
    }

    public boolean listWasModified() {
        return listModified;
    }

    /**
     * Invoked by our drag and drop code.
     */
    void markListModified() {
        this.listModified = true;
    }

    public List<EnhancedAction> getModifiedList() {
        return modifiedList;
    }

    public boolean groupWasRenamed() {
        return !groupName.equals(modifiedGroupName);
    }

    public String getModifiedGroupName() {
        return modifiedGroupName;
    }

    private JComponent buildFormPanel() {
        formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(12);

        nameField = new ShortTextField("Group name:", 20);
        nameField.setText(groupName);
        nameField.setAllowBlank(false);
        nameField.setEnabled(options.isAllowGroupRename());
        nameField.addFieldValidator(new NameFieldValidator(actionPanel, groupName));
        formPanel.add(nameField);

        boolean allowReorder = options.isAllowItemReorder();
        boolean allowRemoval = options.isAllowItemRemoval();
        formPanel.add(LabelField.createPlainHeaderLabel(buildHeaderLabelText(allowReorder, allowRemoval)));
        listField = new ListField<>("", group.getActions());
        listField.setCellRenderer(new EnhancedActionRenderer());
        listField.setVisibleRowCount(10);
        listField.setShouldExpand(true);
        JList<EnhancedAction> list = listField.getList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDragEnabled(allowReorder);
        if (allowReorder) {
            list.setDropMode(DropMode.INSERT);
            list.setTransferHandler(new ListReorderTransferHandler(this));
        }
        else {
            list.setTransferHandler(null);
        }
        formPanel.add(listField);
        formPanel.add(buildListOptionsPanel());

        return formPanel;
    }

    private FormField buildListOptionsPanel() {
        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        panelField.setShouldExpand(true);
        JPanel wrapper = panelField.getPanel();

        if (options.isAllowItemAdd() && options.getNewActionSupplier() != null) {
            JButton button = new JButton("Add action");
            button.setPreferredSize(new Dimension(125, 23));
            button.addActionListener(actionEvent -> addAction());
            wrapper.add(button);
        }

        if (options.isAllowItemRemoval()) {
            JButton button = new JButton("Remove action");
            button.setPreferredSize(new Dimension(125, 23));
            button.addActionListener(actionEvent -> removeAction());
            wrapper.add(button);
        }

        if (options.isAllowItemReorder()) {

            if (actionPanel.getActionComparator() != null) {
                String warning = "<html><b>Note:</b> The ActionPanel is set to auto-sort actions within each group.<br>" +
                        "Although you can control the action order here, this ordering will be ignored,<br>" +
                        "until the ActionPanel's auto-sort is disabled.</html>";
                panelField.setHelpText(warning);
            }

            JButton button = new JButton("Sort by name");
            button.setPreferredSize(new Dimension(125, 23));
            button.addActionListener(actionEvent -> sortByName());
            wrapper.add(button);

            button = new JButton("Reverse sort");
            button.setPreferredSize(new Dimension(125, 23));
            button.addActionListener(actionEvent -> reverseSort());
            wrapper.add(button);
        }

        panelField.getMargins().setBottom(32);
        return panelField;
    }

    private void addAction() {
        EnhancedAction newAction = options.getNewActionSupplier().get(actionPanel, groupName);
        if (newAction != null) {
            listField.getListModel().addElement(newAction);
            listModified = true;
        }
    }

    private void removeAction() {
        int[] selected = listField.getSelectedIndexes();
        if (selected.length == 0) {
            JOptionPane.showMessageDialog(this, "Nothing selected.", "Remove action", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        listField.getListModel().remove(selected[0]); // single select list
        listModified = true;
    }

    private static String buildHeaderLabelText(boolean allowReorder, boolean allowRemoval) {
        String text = "";
        if (allowReorder) {
            text = "Drag+drop or ctrl+up/ctrl+down to reorder";
        }
        if (allowRemoval) {
            if (!text.isEmpty()) {
                text += ", ";
            }
            text += "DEL to remove";
        }
        return text;
    }

    private void sortByName() {
        sortList(Comparator.comparing(EnhancedAction::getName));
    }

    private void reverseSort() {
        DefaultListModel<EnhancedAction> listModel = listField.getListModel();
        if (listModel.size() <= 1) {
            return; // don't bother
        }
        List<EnhancedAction> copy = new ArrayList<>();
        for (int i = listModel.getSize() - 1; i >= 0; i--) {
            copy.add(listModel.getElementAt(i));
        }
        setListContents(copy);
        listModified = true;
    }

    private void sortList(Comparator<EnhancedAction> comparator) {
        DefaultListModel<EnhancedAction> listModel = listField.getListModel();
        if (listModel.size() <= 1) {
            return; // don't bother
        }
        List<EnhancedAction> copy = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            copy.add(listModel.getElementAt(i));
        }
        copy.sort(comparator);
        setListContents(copy);
        listModified = true;
    }

    private void setListContents(List<EnhancedAction> list) {
        DefaultListModel<EnhancedAction> listModel = listField.getListModel();
        listModel.clear();
        for (EnhancedAction tag : list) {
            listModel.addElement(tag);
        }
        listModified = true;
    }

    private void close(boolean okay) {
        if (okay) {
            if (!formPanel.isFormValid()) {
                return;
            }
            DefaultListModel<EnhancedAction> listModel = listField.getListModel();
            modifiedList.clear();
            for (int i = 0; i < listModel.getSize(); i++) {
                modifiedList.add(listModel.getElementAt(i));
            }
            modifiedGroupName = nameField.getText();
        }

        else { // cancel
            modifiedList.clear();
            modifiedGroupName = groupName; // revert any changes to the group name
        }

        wasOkayed = okay;
        dispose();
    }

    /**
     * Sets up keyboard shortcuts for moving items up and down in the list.
     */
    private void setupKeyboardShortcuts() {
        keyStrokeManager.registerHandler(KeyStrokeManager.parseKeyStroke("ctrl+up"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedItem(-1);
            }
        });
        keyStrokeManager.registerHandler(KeyStrokeManager.parseKeyStroke("ctrl+down"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedItem(1);
            }
        });
        keyStrokeManager.registerHandler(KeyStrokeManager.parseKeyStroke("del"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });
    }

    private JComponent buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createRaisedBevelBorder());

        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(actionEvent -> close(true));
        panel.add(button);

        button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(actionEvent -> close(false));
        panel.add(button);

        return panel;
    }

    /**
     * Moves the selected item in the list by the specified offset.
     *
     * @param offset Direction to move (-1 for up, 1 for down)
     */
    private void moveSelectedItem(int offset) {
        if (!options.isAllowItemReorder()) {
            return; // just ignore if not allowed
        }
        DefaultListModel<EnhancedAction> listModel = listField.getListModel();
        int selectedIndex = listField.getList().getSelectedIndex();

        if (selectedIndex == -1) {
            return; // No selection
        }

        int newIndex = selectedIndex + offset;

        // Check bounds
        if (newIndex < 0 || newIndex >= listModel.getSize()) {
            return; // Can't move beyond bounds
        }

        // Move the item
        EnhancedAction item = listModel.getElementAt(selectedIndex);
        listModel.removeElementAt(selectedIndex);
        listModel.insertElementAt(item, newIndex);

        // Maintain selection on moved item
        listField.getList().setSelectedIndex(newIndex);
        listField.getList().ensureIndexIsVisible(newIndex);
        listModified = true;
    }

    private void removeSelectedItem() {
        if (!options.isAllowItemRemoval()) {
            return; // just ignore if not allowed
        }
        DefaultListModel<EnhancedAction> listModel = listField.getListModel();
        int selectedIndex = listField.getList().getSelectedIndex();
        if (selectedIndex == -1) {
            return;
        }

        listModel.removeElementAt(selectedIndex);
        listModified = true;
    }

    /**
     * Custom TransferHandler that handles dragging and dropping list items
     * to reorder them within the same list.
     */
    private static class ListReorderTransferHandler extends TransferHandler {
        private static final DataFlavor LOCAL_OBJECT_FLAVOR =
                new DataFlavor(Integer.class, "application/x-java-Integer");

        private static final DataFlavor[] SUPPORTED_FLAVORS = {LOCAL_OBJECT_FLAVOR};

        private final ToolBarGroupEditDialog ownerDialog;

        public ListReorderTransferHandler(ToolBarGroupEditDialog dialog) {
            this.ownerDialog = dialog;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JList<?> list = (JList<?>)c;
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex < 0) {
                return null;
            }
            return new IntegerTransferable(selectedIndex);
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int action) {
            // Clean up is handled in importData
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            return support.isDataFlavorSupported(LOCAL_OBJECT_FLAVOR);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            JList.DropLocation dropLocation = (JList.DropLocation)support.getDropLocation();
            int dropIndex = dropLocation.getIndex();

            JList<Object> list = (JList<Object>)support.getComponent();
            DefaultListModel<Object> model = (DefaultListModel<Object>)list.getModel();

            try {
                Integer sourceIndex = (Integer)support.getTransferable()
                                                      .getTransferData(LOCAL_OBJECT_FLAVOR);

                if (sourceIndex == null || sourceIndex == dropIndex) {
                    return false;
                }

                // Remove the item from its original position
                Object item = model.getElementAt(sourceIndex);
                model.removeElementAt(sourceIndex);
                if (ownerDialog != null) {
                    ownerDialog.markListModified();
                }

                // Adjust drop index if we removed an item before the drop position
                if (sourceIndex < dropIndex) {
                    dropIndex--;
                }

                // Insert the item at its new position
                model.insertElementAt(item, dropIndex);

                // Select the moved item
                list.setSelectedIndex(dropIndex);

                return true;

            }
            catch (UnsupportedFlavorException |
                   IOException e) {
                return false;
            }
        }
    }

    /**
     * Simple Transferable implementation for transferring integer indices.
     */
    private static class IntegerTransferable implements Transferable {
        private final Integer value;

        public IntegerTransferable(Integer value) {
            this.value = value;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{ListReorderTransferHandler.LOCAL_OBJECT_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return ListReorderTransferHandler.LOCAL_OBJECT_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return value;
        }
    }

    /**
     * A simple ListCellRenderer that allows us to display EnhancedAction instances
     * nicely, with their icon and name, instead of whatever toString() would give us.
     */
    private class EnhancedActionRenderer implements ListCellRenderer<EnhancedAction> {
        private final JLabel label = new JLabel();

        @Override
        public Component getListCellRendererComponent(JList<? extends EnhancedAction> list, EnhancedAction value, int index, boolean isSelected, boolean cellHasFocus) {
            label.setText(value.getName());
            if (actionPanel.isShowActionIcons()) { // don't show icons if disabled in ActionPanel options
                label.setIcon(value.getIcon());
            }
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            label.setOpaque(true);
            return label;
        }
    }

    /**
     * A simple FieldValidator to check for name conflicts when attempting to rename the group.
     * This ensures that group names remain unique within the ActionPanel, without considering case.
     */
    private static class NameFieldValidator implements FieldValidator<ShortTextField> {

        private final ActionPanel actionPanel;
        private final String originalName;

        public NameFieldValidator(ActionPanel actionPanel, String originalName) {
            this.actionPanel = actionPanel;
            this.originalName = originalName;
        }

        /**
         * Note - this logic copied almost verbatim from ToolBarGroupRenameAction.
         * I don't like having it duplicated in two places like this...
         */
        @Override
        public ValidationResult validate(ShortTextField fieldToValidate) {
            String newName = fieldToValidate.getText();

            // If the new name matches the original name, we'll allow it:
            // (this may seem like an unusual case, but you could actually do this to change
            //  the case of the name - e.g. "my group" -> "My Group" - we generally treat
            //  group names as case-insensitive, but the caller may care about it).
            if (originalName.equalsIgnoreCase(newName)) {
                return ValidationResult.valid();
            }

            // Otherwise, check for uniqueness, without considering case.
            // This means that if "My Group" already exists, you won't be able to rename another
            // group to "my group" (or any other case variation).
            if (actionPanel.hasGroup(newName)) {
                return ValidationResult.invalid("A group with this name already exists. Names must be unique.");
            }

            // If we get here, the new name is valid:
            // (we don't explicitly check for blank strings here because our TextInputDialog won't allow it)
            return ValidationResult.valid();
        }
    }

    /**
     * A simple window listener to ensure we clean up our keyStrokeManager when the dialog is closed,
     * and to simulate a cancel action if the user closes the window manually.
     */
    private static class WindowCloseListener extends WindowAdapter {

        private final KeyStrokeManager keyStrokeManager;

        public WindowCloseListener(KeyStrokeManager keyStrokeManager) {
            this.keyStrokeManager = keyStrokeManager;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (e.getSource() instanceof ToolBarGroupEditDialog dialog) {
                dialog.close(false); // Simulate cancel if user closes window manually.
            }
            if (keyStrokeManager != null) {
                keyStrokeManager.dispose();
            }
        }

        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getSource() instanceof ToolBarGroupEditDialog dialog) {
                if (keyStrokeManager != null) {
                    keyStrokeManager.dispose();
                }
            }
        }
    }
}
