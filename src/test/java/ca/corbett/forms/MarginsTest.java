package ca.corbett.forms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MarginsTest {

    @Test
    public void testDefaultConstructor() {
        Margins actual = new Margins();
        assertEquals(Margins.DEFAULT_MARGIN, actual.getLeft());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getTop());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getRight());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getBottom());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getInternalSpacing());
    }

    @Test
    public void testOneArgConstructor() {
        final int VALUE = 44;
        Margins actual = new Margins(VALUE);
        assertEquals(VALUE, actual.getLeft());
        assertEquals(VALUE, actual.getTop());
        assertEquals(VALUE, actual.getRight());
        assertEquals(VALUE, actual.getBottom());
        assertEquals(VALUE, actual.getInternalSpacing());
    }

    @Test
    public void testFiveArgConstructor() {
        Margins actual = new Margins(11,22,33,44,55);
        assertEquals(11, actual.getLeft());
        assertEquals(22, actual.getTop());
        assertEquals(33, actual.getRight());
        assertEquals(44, actual.getBottom());
        assertEquals(55, actual.getInternalSpacing());
    }

    @Test
    public void testCopyConstructor() {
        Margins actual1 = new Margins(11,22,33,44,55);
        Margins actual2 = new Margins(actual1);
        assertEquals(11, actual2.getLeft());
        assertEquals(22, actual2.getTop());
        assertEquals(33, actual2.getRight());
        assertEquals(44, actual2.getBottom());
        assertEquals(55, actual2.getInternalSpacing());
    }

    @Test
    public void testSetAll() {
        Margins actual = new Margins().setAll(77);
        assertEquals(77, actual.getLeft());
        assertEquals(77, actual.getTop());
        assertEquals(77, actual.getRight());
        assertEquals(77, actual.getBottom());
        assertEquals(77, actual.getInternalSpacing());
    }

    @Test
    public void testSetLeft() {
        Margins actual = new Margins().setLeft(77);
        assertEquals(77, actual.getLeft());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getTop());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getRight());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getBottom());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getInternalSpacing());
    }

    @Test
    public void testSetRight() {
        Margins actual = new Margins().setRight(77);
        assertEquals(Margins.DEFAULT_MARGIN, actual.getLeft());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getTop());
        assertEquals(77, actual.getRight());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getBottom());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getInternalSpacing());
    }

    @Test
    public void testSetTop() {
        Margins actual = new Margins().setTop(77);
        assertEquals(Margins.DEFAULT_MARGIN, actual.getLeft());
        assertEquals(77, actual.getTop());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getRight());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getBottom());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getInternalSpacing());
    }

    @Test
    public void testSetBottom() {
        Margins actual = new Margins().setBottom(77);
        assertEquals(Margins.DEFAULT_MARGIN, actual.getLeft());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getTop());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getRight());
        assertEquals(77, actual.getBottom());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getInternalSpacing());
    }

    @Test
    public void testSetInternalSpacing() {
        Margins actual = new Margins().setInternalSpacing(77);
        assertEquals(Margins.DEFAULT_MARGIN, actual.getLeft());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getTop());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getRight());
        assertEquals(Margins.DEFAULT_MARGIN, actual.getBottom());
        assertEquals(77, actual.getInternalSpacing());
    }

    @Test
    public void testEquals() {
        Margins actual1 = new Margins();
        Margins actual2 = new Margins(3);

        assertNotEquals(actual1, actual2);

        actual1 = new Margins(actual2);

        assertEquals(actual1, actual2);
    }
}