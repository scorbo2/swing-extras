package ca.corbett.forms;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Can be used to specify margins and padding around and inside a FormField,
 * or any other component that supports this Margins class.
 * <p>
 * The left, right, top, and bottom properties apply margins outside the
 * target component. That is, the space between the component and the
 * edge of its container. For example, in the case of a FormField:
 * <ul>
 *     <li><b>left</b> - the "indent" between the left edge of the FormPanel
 *         and the left edge of the FormField.</li>
 *     <li><b>right</b> - the space between the right edge of the FormField
 *         and the right edge of the FormPanel.</li>
 *     <li><b>top</b> - the space between the top edge of the FormField and
 *         either the previous FormField on the same FormPanel, or the top
 *         edge of the FormPanel if this is the first FormField.</li>
 *     <li><b>bottom</b> - the space between the bottom edge of the
 *         FormField and either the next FormField on the same FormPanel,
 *         or the bottom edge of the FormPanel if this is the last FormField.</li>
 * </ul>
 * <p>
 * The <b>internalSpacing</b> property applies extra space within the component.
 * For example, in the case of a FormField, this is the extra space between the various
 * subcomponents of the FormField. That is, the space between the field label and the field component,
 * between the field component and the help label, and between the help label and the validation label.
 * </p>
 * <p>
 *     Any component that supports this Margins class can apply the properties
 *     in a way that makes sense for that component. The examples above
 *     are specific to FormField, but this Margins class can be used for other components as well.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.4
 */
public class Margins {

    private static final Logger log = Logger.getLogger(Margins.class.getName());

    /**
     * A very simple interface that can be used to listen for changes to a Margins instance.
     */
    @FunctionalInterface
    public interface Listener {
        void marginsChanged(Margins margins);
    }

    public static final int DEFAULT_MARGIN = 4;

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private int left;
    private int top;
    private int right;
    private int bottom;
    private int internalSpacing;

    /**
     * Creates a new Margins instance with all properties set to a hard-coded default of 4 pixels.
     */
    public Margins() {
        this(DEFAULT_MARGIN);
    }

    /**
     * Creates a new Margins instance with all properties set to the given pixel value.
     */
    public Margins(int all) {
        this(all, all, all, all, all);
    }

    /**
     * Creates a new Margins instance with the given properties (all values in pixels).
     */
    public Margins(int left, int top, int right, int bottom, int internalSpacing) {
        this.left = validateInput(left);
        this.top = validateInput(top);
        this.right = validateInput(right);
        this.bottom = validateInput(bottom);
        this.internalSpacing = validateInput(internalSpacing);
    }

    /**
     * Creates a new Margins instance by copying values from the given other Margins instance.
     * If the given instance is null, this is equivalent to new Margins().
     */
    public Margins(Margins other) {
        this();
        copy(other);
    }

    /**
     * Copies all values from the given other Margins instance. If the given instance is null,
     * this call does nothing.
     */
    public Margins copy(Margins other) {
        if (other != null) {
            this.left = validateInput(other.left);
            this.top = validateInput(other.top);
            this.right = validateInput(other.right);
            this.bottom = validateInput(other.bottom);
            this.internalSpacing = validateInput(other.internalSpacing);
            fireChangeEvent(); // Just fire one event for this
        }
        return this;
    }

    /**
     * Set all values to the given pixel value.
     */
    public Margins setAll(int value) {
        left = validateInput(value);
        top = validateInput(value);
        right = validateInput(value);
        bottom = validateInput(value);
        internalSpacing = validateInput(value);
        fireChangeEvent(); // Just fire one event for this
        return this;
    }

    public int getLeft() {
        return left;
    }

    public Margins setLeft(int left) {
        this.left = validateInput(left);
        fireChangeEvent();
        return this;
    }

    public int getTop() {
        return top;
    }

    public Margins setTop(int top) {
        this.top = validateInput(top);
        fireChangeEvent();
        return this;
    }

    public int getRight() {
        return right;
    }

    public Margins setRight(int right) {
        this.right = validateInput(right);
        fireChangeEvent();
        return this;
    }

    public int getBottom() {
        return bottom;
    }

    public Margins setBottom(int bottom) {
        this.bottom = validateInput(bottom);
        fireChangeEvent();
        return this;
    }

    public int getInternalSpacing() {
        return internalSpacing;
    }

    public Margins setInternalSpacing(int internalSpacing) {
        this.internalSpacing = validateInput(internalSpacing);
        fireChangeEvent();
        return this;
    }

    private int validateInput(int input) {
        if (input < 0) {
            log.warning("Margins: ignoring negative margin: " + input);
            return 0; // legacy behavior expected by callers
        }
        return input;
    }

    /**
     * You can listen for changes to this Margins instance by adding a Listener.
     * Whenever any property of this Margins instance is changed,
     * all registered listeners will be notified via their marginsChanged() method.
     */
    public Margins addListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        listeners.add(listener);
        return this;
    }

    /**
     * You can stop listening to this Margins instance by removing a Listener
     * that was previously added via addListener().
     */
    public Margins removeListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        listeners.remove(listener);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Margins margins)) { return false; }
        return left == margins.left
                && top == margins.top
                && right == margins.right
                && bottom == margins.bottom
                && internalSpacing == margins.internalSpacing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, top, right, bottom, internalSpacing);
    }

    private void fireChangeEvent() {
        for (Listener listener : listeners) {
            listener.marginsChanged(this);
        }
    }
}
