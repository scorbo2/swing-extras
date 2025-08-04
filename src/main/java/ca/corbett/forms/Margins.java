package ca.corbett.forms;

import java.util.Objects;

/**
 * Represents margins around a FormField.
 */
public class Margins {

    public static final int DEFAULT_MARGIN = 4;

    private int left;
    private int top;
    private int right;
    private int bottom;
    private int internalSpacing;

    public Margins() {
        this(DEFAULT_MARGIN);
    }

    public Margins(int all) {
        this(all, all, all, all, all);
    }

    public Margins(int left, int top, int right, int bottom, int internalSpacing) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.internalSpacing = internalSpacing;
    }

    public Margins(Margins other) {
        this();
        copy(other);
    }

    public void copy(Margins other) {
        if (other != null) {
            this.left = other.left;
            this.top = other.top;
            this.right = other.right;
            this.bottom = other.bottom;
            this.internalSpacing = other.internalSpacing;
        }
    }

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
