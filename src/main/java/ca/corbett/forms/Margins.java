package ca.corbett.forms;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Can be used to specify margins around an inside of a FormField.
 * <p>
 * The left, right, top, and bottom properties apply margins outside the
 * FormField - that is, either between the FormField and the outside border
 * of the FormPanel, or between the FormField and the previous or next
 * FormField on the same FormPanel.
 * <p>
 * The internalSpacing property applies extra space within the FormField -
 * that is, between the field label and the field component, between the
 * field component and the help label, and between the help label and
 * the validation label.
 */
public class Margins {

    private static final Logger log = Logger.getLogger(Margins.class.getName());

    public static final int DEFAULT_MARGIN = 4;

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
    public void copy(Margins other) {
        if (other != null) {
            this.left = validateInput(other.left);
            this.top = validateInput(other.top);
            this.right = validateInput(other.right);
            this.bottom = validateInput(other.bottom);
            this.internalSpacing = validateInput(other.internalSpacing);
        }
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
        return this;
    }

    public int getLeft() {
        return left;
    }

    public Margins setLeft(int left) {
        this.left = validateInput(left);
        return this;
    }

    public int getTop() {
        return top;
    }

    public Margins setTop(int top) {
        this.top = validateInput(top);
        return this;
    }

    public int getRight() {
        return right;
    }

    public Margins setRight(int right) {
        this.right = validateInput(right);
        return this;
    }

    public int getBottom() {
        return bottom;
    }

    public Margins setBottom(int bottom) {
        this.bottom = validateInput(bottom);
        return this;
    }

    public int getInternalSpacing() {
        return internalSpacing;
    }

    public Margins setInternalSpacing(int internalSpacing) {
        this.internalSpacing = validateInput(internalSpacing);
        return this;
    }

    private int validateInput(int input) {
        if (input < 0) {
            log.warning("Margins: ignoring negative margin: " + input);
            return 0;
        }
        return input;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Margins)) { return false; }
        Margins margins = (Margins)o;
        return left == margins.left && top == margins.top && right == margins.right && bottom == margins.bottom && internalSpacing == margins.internalSpacing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, top, right, bottom, internalSpacing);
    }
}
