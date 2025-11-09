package ca.corbett.extras;

import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseListener;

/**
 * A custom JTabbedPane that allows toggling tab header visibility at runtime.
 * With the tab pane hidden, this component behaves pretty much like a panel
 * with CardLayout, where you can still programmatically flip between the tabs,
 * but the user can no longer see the tab header and can no longer use the mouse
 * to switch between tabs. This is great for adding hidden tabs and then having
 * a way to show/hide the tab bar at runtime.
 *
 * @author claude.ai wrote most of this one!
 */
public class ToggleableTabbedPane extends JTabbedPane {

    private boolean tabHeaderVisible = true;
    private BasicTabbedPaneUI defaultUI;
    private HiddenTabUI hiddenUI;

    public ToggleableTabbedPane() {
        super();
        initializeUIs();
    }

    public ToggleableTabbedPane(int tabPlacement) {
        super(tabPlacement);
        initializeUIs();
    }

    public ToggleableTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
        initializeUIs();
    }

    private void initializeUIs() {
        // Store the default UI
        defaultUI = new BasicTabbedPaneUI();

        // Create our custom hidden UI
        hiddenUI = new HiddenTabUI();

        // Set initial state
        updateUI();

        // We need to listen for Look and Feel changes, because our custom
        // UI will be overridden by a default UI for the new LaF:
        LookAndFeelManager.addChangeListener(e -> {
            defaultUI = new BasicTabbedPaneUI();
            if (! tabHeaderVisible) {
                setUI(hiddenUI); // re-set it if needed
                revalidate();
                repaint();
            }
        });
    }

    /**
     * Sets whether the tab headers should be visible.
     * @param visible true to show tab headers, false to hide them
     */
    public void setTabHeaderVisible(boolean visible) {
        if (this.tabHeaderVisible != visible) {
            this.tabHeaderVisible = visible;
            updateTabUI();
            revalidate();
            repaint();
        }
    }

    /**
     * Returns whether the tab headers are currently visible.
     * @return true if tab headers are visible, false otherwise
     */
    public boolean isTabHeaderVisible() {
        return tabHeaderVisible;
    }

    /**
     * Toggles the visibility of tab headers.
     */
    public void toggleTabHeaderVisibility() {
        setTabHeaderVisible(!tabHeaderVisible);
    }

    private void updateTabUI() {
        if (tabHeaderVisible) {
            setUI(defaultUI);
        } else {
            setUI(hiddenUI);
        }
    }

    @Override
    public void updateUI() {
        // Initialize UIs if they don't exist yet (during construction)
        if (defaultUI == null || hiddenUI == null) {
            super.updateUI();
            return;
        }

        updateTabUI();
    }

    /**
     * Custom UI that hides the tab headers completely.
     */
    private static class HiddenTabUI extends BasicTabbedPaneUI {

        @Override
        protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
            return 0;
        }

        @Override
        protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount, int maxTabWidth) {
            return 0;
        }

        @Override
        protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                                Rectangle iconRect, Rectangle textRect) {
            // Don't paint individual tabs
        }

        @Override
        protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
            // Don't paint the tab area at all
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
            // Don't paint tab borders
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            // Don't paint tab backgrounds
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                           Rectangle[] rects, int tabIndex,
                                           Rectangle iconRect, Rectangle textRect,
                                           boolean isSelected) {
            // Don't paint focus indicators
        }

        @Override
        protected MouseListener createMouseListener() {
            // Return a no-op mouse listener to prevent tab switching via mouse
            return new MouseListener() {
                public void mouseClicked(java.awt.event.MouseEvent e) {}
                public void mousePressed(java.awt.event.MouseEvent e) {}
                public void mouseReleased(java.awt.event.MouseEvent e) {}
                public void mouseEntered(java.awt.event.MouseEvent e) {}
                public void mouseExited(java.awt.event.MouseEvent e) {}
            };
        }

        @Override
        protected Insets getTabAreaInsets(int tabPlacement) {
            return new Insets(0, 0, 0, 0);
        }
    }
}