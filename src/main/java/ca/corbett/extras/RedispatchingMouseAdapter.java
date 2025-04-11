package ca.corbett.extras;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Sometimes you want mouse events that arrive on a child component to bubble up to the
 * containing component to be processed there. For example, you have a JLabel sitting
 * inside a JPanel, but when a mouse event happens on the JLabel, you want the JPanel
 * to receive it instead. So, we need a way to propagate the mouse events up to
 * the parent. I found this solution on StackOverflow and it seems to work really well.
 * All credit goes to user Wojciech Wirzbicki and his
 * <A HREF="https://stackoverflow.com/questions/3818246/passing-events-to-parent/32204965#32204965">answer from 2015</A>.
 *
 * @since 2017-11-12
 */
public class RedispatchingMouseAdapter
        implements MouseListener, MouseWheelListener, MouseMotionListener {

    @Override
    public void mouseClicked(MouseEvent e) {
        redispatchToParent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        redispatchToParent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        redispatchToParent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        redispatchToParent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        redispatchToParent(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        redispatchToParent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        redispatchToParent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        redispatchToParent(e);
    }

    private void redispatchToParent(MouseEvent e) {
        Component source = (Component)e.getSource();
        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
        source.getParent().dispatchEvent(parentEvent);
    }
}
