package ca.corbett.forms;

import java.util.Objects;

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
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.internalSpacing = internalSpacing;
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
            this.left = other.left;
            this.top = other.top;
            this.right = other.right;
            this.bottom = other.bottom;
            this.internalSpacing = other.internalSpacing;
        }
    }

    /**
     * Set all values to the given pixel value.
     */
    public Margins setAll(int value) {
        left = value;
        top = value;
        right = value;
        bottom = value;
        internalSpacing = value;
        return this;
    }

    public int getLeft() {
        return left;
    }

    public Margins setLeft(int left) {
        this.left = left;
        return this;
    }

    public int getTop() {
        return top;
    }

    public Margins setTop(int top) {
        this.top = top;
        return this;
    }

    public int getRight() {
        return right;
    }

    public Margins setRight(int right) {
        this.right = right;
        return this;
    }

    public int getBottom() {
        return bottom;
    }

    public Margins setBottom(int bottom) {
        this.bottom = bottom;
        return this;
    }

    public int getInternalSpacing() {
        return internalSpacing;
    }

    public Margins setInternalSpacing(int internalSpacing) {
        this.internalSpacing = internalSpacing;
        return this;
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
