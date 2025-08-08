package ca.corbett.extras;

import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * The way JTextComponent handles change events is infuriating - this class
 * aims to fix some of the wonkiness and make it more natural to work with.
 * <P>
 * <b>The Problem:</b> the only way to listen for changes in a JTextComponent
 *     is to add a DocumentListener, which gives you a notification when
 *     text is inserted, and a separate notification when text is removed.
 *     Pretty simple, right? The problem is that programmatically setting
 *     text via the setText() method will result in two notifications in a row
 *     for the same change. This is exposing internals as to how the setText()
 *     method works. First, it blanks any text currently in the text field
 *     (thereby triggering a remove notification), and then it adds the
 *     new text (triggering an insert notification). This is very frustrating
 *     if you just want to be notified each time the field changes.
 * </P>
 * <p>
 * <b>The solution:</b> this class will listen for insert/remove notifications
 *     on a JTextComponent and then start a cheesy countdown timer of a very
 *     short interval. If no further changes are received within that interval,
 *     a single change notification is sent out. If a change is received during
 *     that interval, the timer is restarted. This small delay should be
 *     imperceptible to users but will also allow us to coalesce the multiple
 *     behind-the-scenes change notifications into one single notification.
 *     In effect, we can treat setText() as a single operation, as it should be.
 * </p>
 *
 * @author scorbo2 (with help from claude.ai!)
 * @since swing-extras 2.4
 */
public class CoalescingDocumentListener implements DocumentListener {

    public static final int DELAY_MS = 25;

    private final Timer timer;
    private final ChangeListener changeListener;
    private final JTextComponent textComponent;
    private String lastNotifiedText = "";

    /**
     * Create a new instance by supplying the JTextComponent to be monitored along with
     * a ChangeListener that will be invoked when the text field is modified.
     */
    public CoalescingDocumentListener(JTextComponent textField, ChangeListener listener) {
        this.textComponent = textField;
        this.changeListener = listener;
        this.lastNotifiedText = textComponent.getText();

        // Timer with short delay to coalesce rapid changes
        this.timer = new Timer(DELAY_MS, e -> {
            String currentText = textComponent.getText();
            if (!currentText.equals(lastNotifiedText)) {
                lastNotifiedText = currentText;
                changeListener.stateChanged(new ChangeEvent(textComponent));
            }
        });
        timer.setRepeats(false);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        timer.restart();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        timer.restart();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        timer.restart();
    }
}
