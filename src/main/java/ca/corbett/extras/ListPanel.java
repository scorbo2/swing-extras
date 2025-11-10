package ca.corbett.extras;

import ca.corbett.extensions.ui.ExtensionManagerDialog;
import ca.corbett.extras.properties.PropertiesDialog;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Presents a vertical JList of items, with an optional header label above the list,
 * and an optional list of command buttons under the list. This was intended specifically
 * for use on the ExtensionManagerDialog, but is generic enough to use for any
 * type of object. You can listen for list selection events, and your list of custom
 * commands (if supplied) can be any AbstractAction.
 * <p>
 *     The ListPanel defaults to an initial pixel width of 200, but you can
 *     change this with setPreferredWidth(). The command buttons (if specified)
 *     will grow horizontally to fill the width of the ListPanel.
 * </p>
 *
 * @param <T> Any class you like, as long as it has a sensible toString()
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class ListPanel<T> extends JPanel {

    private final DefaultListModel<T> listModel;
    private final JList<T> list;
    private final String headerLabel;
    private final List<AbstractAction> commands;
    private int preferredWidth;

    /**
     * Creates an empty ListPanel with no header label and no custom commands.
     */
    public ListPanel() {
        this(null, null);
    }

    /**
     * Creates an empty ListPanel with the given header label.
     */
    public ListPanel(String title) {
        this(title, null);
    }

    /**
     * Creates an empty ListPanel with no header label and with the given list of custom commands.
     */
    public ListPanel(List<AbstractAction> commands) {
        this(null, commands);
    }

    /**
     * Creates an empty ListPanel with the given header label and the given list of custom commands.
     */
    public ListPanel(String headerLabel, List<AbstractAction> commands) {
        this.commands = commands == null ? new ArrayList<>() : new ArrayList<>(commands);
        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.headerLabel = ExtensionManagerDialog.trimString(headerLabel, 21);
        preferredWidth = 200; // arbitrary default
        initializeLayout();

        // Listen for resizes and track new width:
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                preferredWidth = e.getComponent().getSize().width;
            }
        });
    }

    public void clear() {
        listModel.clear();
        list.revalidate();
        list.repaint();
    }

    public T getItemAt(int index) {
        return listModel.getElementAt(index);
    }

    public void removeItemAt(int index) {
        listModel.removeElementAt(index);
    }

    public void setPreferredWidth(int width) {
        preferredWidth = width;
    }

    @Override
    public void setPreferredSize(Dimension dim) {
        super.setPreferredSize(dim);
        preferredWidth = dim.width;
    }

    public void setListCellRenderer(ListCellRenderer<T> renderer) {
        list.setCellRenderer(renderer);
    }

    /**
     * Overridden here to force correct sizing when this ListPanel is added
     * to a BorderLayout in the EAST or WEST positions. BorderLayout is a bit
     * wonky and returning my own preferredWidth was the only way I could
     * find to force it to render this ListPanel at the desired size,
     * as opposed to just auto-sizing it based on its contents, which is
     * what it will do without this override.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(preferredWidth, super.getPreferredSize().height);
    }

    public boolean isEmpty() {
        return listModel.isEmpty();
    }

    public int getItemCount() {
        return listModel.size();
    }

    public T getSelected() {
        return list.getSelectedValue();
    }

    public void selectItem(int index) {
        list.getSelectionModel().setSelectionInterval(index, index);
    }

    public List<T> getAll() {
        List<T> all = new ArrayList<>(listModel.getSize());
        for (int i = 0; i < listModel.getSize(); i++) {
            all.add(listModel.get(i));
        }
        return all;
    }

    public void setItems(List<T> extensions) {
        clear();
        if (extensions != null && ! extensions.isEmpty()) {
            addItems(extensions);
        }
    }

    public void addItems(List<T> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return;
        }
        for (T extension : extensions) {
            listModel.addElement(extension);
        }
        list.revalidate();
        list.repaint();
    }

    public void addItem(T extension) {
        if (extension == null) {
            return;
        }
        listModel.addElement(extension);
        list.revalidate();
        list.repaint();
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        list.addListSelectionListener(listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener) {
        list.removeListSelectionListener(listener);
    }

    private void initializeLayout() {
        setLayout(new BorderLayout());

        // Add a header for the list panel, if a header was given:
        if (headerLabel != null && ! headerLabel.isBlank()) {
            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            JLabel label = new JLabel(headerLabel);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14));
            headerPanel.add(label);
            add(headerPanel, BorderLayout.NORTH);
        }

        // Add the list itself, wrapped in a scroll pane:
        add(PropertiesDialog.buildScrollPane(list), BorderLayout.CENTER);

        // Add optional command buttons underneath:
        if (commands != null && ! commands.isEmpty()) {
            JPanel buttonPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.insets = new Insets(2,10,2, 10);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            for (AbstractAction action : commands) {
                buttonPanel.add(new JButton(action), gbc);
                gbc.gridy++;
            }

            add(buttonPanel, BorderLayout.SOUTH);
        }

        // Put a bevel border around the entire thing:
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }
}
