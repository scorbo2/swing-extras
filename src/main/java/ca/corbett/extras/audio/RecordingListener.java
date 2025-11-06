package ca.corbett.extras.audio;

/**
 * Provides an interface for listening for recording events.
 * <P>
 *     <B>IMPORTANT:</B> The callbacks in this listener will be invoked from the worker thread!
 *     If you need to update a Swing UI component as a result of one of these callbacks, you need
 *     to marshal that call to the Swing Event Dispatching Thread, like this:
 * </P>
 * <pre>
 * &#64;Override
 * public void complete() {
 *     // Most operations are fine to do here on the worker thread.
 *     // For example, computing a user-friendly string describing something
 *     String text = buildProgressTextLabel();
 *
 *     // BUT! Now we need to display it in a Swing UI component:
 *     SwingUtilities.invokeLater(() -> { // marshal to EDT
 *        myStatusLabel.setText(text);
 *     });
 * }
 * </pre>
 * <p>
 *     Failure to do this may result in deadlocks or other threading issues, as Swing
 *     itself is not thread-safe.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2018-01-20
 */
public interface RecordingListener {

    void complete();

}
