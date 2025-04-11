package ca.corbett.extras.dirtree;

import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Graphics;

/**
 * Shamelessly stolen from StackOverflow. Draws nice simple little + and - icons next to tree
 * nodes to indicate expand/collapse ability, instead of the horrible icons included
 * in every major look and feel.
 *
 * @author StackOverflow
 * @since 2017-11-09
 */
public class DirTreeNodeIcon implements Icon {

    private static final int SIZE = 9;
    private final char type;

    public DirTreeNodeIcon(char type) {
        this.type = type;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(UIManager.getColor("Tree.background"));
        g.fillRect(x, y, SIZE - 1, SIZE - 1);
        g.setColor(UIManager.getColor("Tree.hash"));
        g.drawRect(x, y, SIZE - 1, SIZE - 1);
        g.setColor(UIManager.getColor("Tree.foreground"));
        g.drawLine(x + 2, y + SIZE / 2, x + SIZE - 3, y + SIZE / 2);
        if (type == '+') {
            g.drawLine(x + SIZE / 2, y + 2, x + SIZE / 2, y + SIZE - 3);
        }
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

}
