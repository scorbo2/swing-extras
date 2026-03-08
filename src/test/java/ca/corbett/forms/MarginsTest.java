package ca.corbett.forms;

import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    public void testNegativeValues_shouldReject() {
        Margins actual1 = new Margins(-1);
        Margins actual2 = new Margins(-1, -1, -1, -1, -1);
        Margins actual3 = new Margins();
        actual3.setLeft(-1);
        actual3.setRight(-1);
        actual3.setTop(-1);
        actual3.setBottom(-1);
        actual3.setInternalSpacing(-1);

        List<Margins> margins = List.of(actual1, actual2, actual3);
        for (Margins margin : margins) {
            assertEquals(0, margin.getInternalSpacing());
            assertEquals(0, margin.getLeft());
            assertEquals(0, margin.getRight());
            assertEquals(0, margin.getTop());
            assertEquals(0, margin.getBottom());
        }
    }

    @Test
    public void addListener_shouldNotifyOnChange() {
        // GIVEN a Margins instance with a listener:
        Margins margins = new Margins();
        TestListener listener = new TestListener();
        margins.addListener(listener);

        // WHEN we do five updates:
        margins.setLeft(10);
        margins.setTop(20);
        margins.setRight(30);
        margins.setBottom(40);
        margins.setInternalSpacing(50);

        // THEN we should receive five notifications:
        assertEquals(5, listener.getNotificationCount());
    }

    @Test
    public void setAll_withListener_shouldOnlyNotifyOnce() {
        // GIVEN a Margins instance with a listener:
        Margins margins = new Margins();
        TestListener listener = new TestListener();
        margins.addListener(listener);

        // WHEN we set all properties at once:
        margins.setAll(99);

        // THEN we should receive only one notification, not one per property:
        assertEquals(1, listener.getNotificationCount());
    }

    @Test
    public void removeListener_shouldStopNotifications() {
        // GIVEN a Margins instance with a listener that we will remove:
        Margins margins = new Margins();
        TestListener listener = new TestListener();
        margins.addListener(listener);

        // WHEN we remove the listener and then make some changes:
        margins.removeListener(listener);
        margins.setLeft(10);
        margins.setTop(20);
        margins.setRight(30);
        margins.setBottom(40);
        margins.setInternalSpacing(50);

        // THEN we should receive no notifications:
        assertEquals(0, listener.getNotificationCount());
    }

    private static class TestListener implements Margins.Listener {
        private int notificationCount = 0;

        @Override
        public void marginsChanged(Margins margins) {
            notificationCount++;
        }

        public int getNotificationCount() {
            return notificationCount;
        }
    }
}
