package ca.corbett.extensions.ui;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.LookAndFeelManager;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Provides a standardized UI for viewing and enabling extensions.
 * If you want a convenient dialog wrapper around this panel,
 * use ExtensionDialog instead.
 *
 * @author scorbo2
 * @since 2023-11-11
 */
public class ExtensionManagerPanel extends JPanel {

    protected final ExtensionManager<AppExtension> extManager;
    protected JList extList;
    protected DefaultListModel<AppExtensionPlaceholder> extListModel;
    protected final JPanel contentPanel = new JPanel();
    protected final List<AppExtensionPlaceholder> extensions;
    protected final Map<String, ExtensionDetailsPanel> detailsPanelMap;
    protected ExtensionDetailsPanel emptyPanel;

    public ExtensionManagerPanel(ExtensionManager manager) {
        extManager = manager;
        extensions = new ArrayList<>();
        detailsPanelMap = new HashMap<>();
        for (AppExtension extension : extManager.getAllLoadedExtensions()) {
            extensions.add(new AppExtensionPlaceholder(extension,
                                                       extension.getInfo().getName(),
                                                       extManager.isExtensionEnabled(extension.getClass().getName())));
            extensions.sort(new Comparator<AppExtensionPlaceholder>() {
                @Override
                public int compare(AppExtensionPlaceholder o1, AppExtensionPlaceholder o2) {
                    return o1.extension.getInfo().getName().compareTo(o2.extension.getInfo().getName());
                }

            });
        }
        initComponents();
    }

    /**
     * Reports whether an extension has been enabled or disabled by the user on this panel.
     *
     * @param className The fully qualified class name of the extension to query.
     * @return True if that extension is enabled on this panel, false otherwise or if not found.
     */
    public boolean isExtensionEnabled(String className) {
        for (AppExtensionPlaceholder placeholder : extensions) {
            if (placeholder.extension.getClass().getName().equals(className)) {
                return placeholder.isEnabled;
            }
        }
        return false;
    }

    protected void setAllEnabled(boolean enabled) {
        for (AppExtensionPlaceholder placeholder : extensions) {
            placeholder.isEnabled = enabled;
            ExtensionDetailsPanel detailsPanel = detailsPanelMap.get(placeholder.extension.getClass().getName());
            if (detailsPanel != null) {
                detailsPanel.setExtensionEnabled(enabled);
            }
        }
        rejigger(extList);
    }

    protected void listSelectionChanged() {
        contentPanel.removeAll();
        final AppExtensionPlaceholder placeholder = (AppExtensionPlaceholder)extList.getSelectedValue();
        if (placeholder == null) {
            contentPanel.add(emptyPanel);
            return; // no selection
        }
        ExtensionDetailsPanel detailsPanel = detailsPanelMap.get(placeholder.extension.getClass().getName());
        if (detailsPanel == null) {
            detailsPanel = new ExtensionDetailsPanel(extManager, placeholder.extension, placeholder.isEnabled);
            detailsPanel.addExtensionDetailsPanelListener(new ExtensionDetailsPanelListener() {
                @Override
                public void extensionEnabled(ExtensionDetailsPanel source, String className) {
                    placeholder.isEnabled = true;
                    rejigger(extList);
                }

                @Override
                public void extensionDisabled(ExtensionDetailsPanel source, String className) {
                    placeholder.isEnabled = false;
                    rejigger(extList);
                }

            });
            detailsPanelMap.put(placeholder.extension.getClass().getName(), detailsPanel);
        }
        contentPanel.add(detailsPanel, BorderLayout.CENTER);
        rejigger(contentPanel);
    }

    /**
     * Swing nonsense - when stuff is added or removed from a container, you have to
     * do some silliness to get the change to become visible.
     *
     * @param component The thing to rejigger.
     */
    protected void rejigger(final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                component.invalidate();
                component.revalidate();
                component.repaint();
            }

        });
    }

    protected void initComponents() {
        setLayout(new BorderLayout());

        // The empty panel will be shown by default, or whenever the list selection is cleared.
        emptyPanel = new ExtensionDetailsPanel(extManager, null, false);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        // Add a header for the left panel:
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel label = new JLabel("Available extensions");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14));
        headerPanel.add(label);
        leftPanel.add(headerPanel, BorderLayout.NORTH);

        // The extension list fills most of the left panel:
        extListModel = new DefaultListModel<>();
        for (AppExtensionPlaceholder placeholder : extensions) {
            extListModel.addElement(placeholder);
        }
        extList = new JList(extListModel);
        extList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        extList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                listSelectionChanged();
            }

        });
        extList.setCellRenderer(new ExtensionListRenderer());
        JScrollPane listScrollPane = new JScrollPane(extList);
        listScrollPane.setPreferredSize(new Dimension(200, 200));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Put some buttons underneath the list for batch operations:
        leftPanel.add(buildBatchButtonPanel(), BorderLayout.SOUTH);
        leftPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        // And we have our layout:
        add(leftPanel, BorderLayout.WEST);
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // If we have at least one extension, select it:
        if (!extListModel.isEmpty()) {
            SwingUtilities.invokeLater(() -> extList.getSelectionModel().setSelectionInterval(0, 0));
        }

        // Otherwise, show the empty panel:
        else {
            contentPanel.add(emptyPanel);
        }
    }

    protected JPanel buildBatchButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1));

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btn = new JButton("Enable all");
        btn.setPreferredSize(new Dimension(125, 23));
        containerPanel.add(btn);
        btn.addActionListener(e -> setAllEnabled(true));
        buttonPanel.add(containerPanel);

        containerPanel = new JPanel();
        containerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("Disable all");
        btn.setPreferredSize(new Dimension(125, 23));
        containerPanel.add(btn);
        btn.addActionListener(e -> setAllEnabled(false));
        buttonPanel.add(containerPanel);

        return buttonPanel;
    }

    protected static class AppExtensionPlaceholder {

        protected final AppExtension extension;
        protected String name;
        protected boolean isEnabled;

        public AppExtensionPlaceholder(AppExtension extension, String name, boolean isEnabled) {
            this.extension = extension;
            this.name = name;
            this.isEnabled = isEnabled;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AppExtensionPlaceholder other = (AppExtensionPlaceholder)obj;
            return Objects.equals(this.name, other.name);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            if (!isEnabled) {
                sb.append(" (disabled)");
            }
            return sb.toString();
        }

    }

    /**
     * TODO I don't remember why this is needed...
     *      I think I just wanted to make disabled extensions visually more obvious?
     */
    protected static class ExtensionListRenderer extends JLabel implements ListCellRenderer<AppExtensionPlaceholder> {

        @Override
        public Component getListCellRendererComponent(JList<? extends AppExtensionPlaceholder> list, AppExtensionPlaceholder value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
            setOpaque(true);
            Color selectedFg = LookAndFeelManager.getLafColor("List.selectionForeground", Color.WHITE);
            Color selectedBg = LookAndFeelManager.getLafColor("List.selectionBackground", Color.BLUE);
            Color normalFg = LookAndFeelManager.getLafColor("List.foreground", Color.BLACK);
            Color normalBg = LookAndFeelManager.getLafColor("List.background", Color.WHITE);
            setForeground(isSelected ? selectedFg : normalFg);
            setBackground(isSelected ? selectedBg : normalBg);
            if (value.isEnabled) {
                setFont(list.getFont().deriveFont(Font.PLAIN));
            }
            else {
                setFont(list.getFont().deriveFont(Font.ITALIC));
            }
            return this;
        }
    }
}
