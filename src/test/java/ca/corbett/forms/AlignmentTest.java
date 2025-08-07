package ca.corbett.forms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlignmentTest {

    @Test
    public void testTopAlignment() {
        assertTrue(Alignment.TOP_LEFT.isTopAligned());
        assertTrue(Alignment.TOP_CENTER.isTopAligned());
        assertTrue(Alignment.TOP_RIGHT.isTopAligned());
        assertFalse(Alignment.CENTER_LEFT.isTopAligned());
        assertFalse(Alignment.CENTER.isTopAligned());
        assertFalse(Alignment.CENTER_RIGHT.isTopAligned());
        assertFalse(Alignment.BOTTOM_LEFT.isTopAligned());
        assertFalse(Alignment.BOTTOM_CENTER.isTopAligned());
        assertFalse(Alignment.BOTTOM_RIGHT.isTopAligned());
    }

    @Test
    public void testLeftAlignment() {
        assertTrue(Alignment.TOP_LEFT.isLeftAligned());
        assertFalse(Alignment.TOP_CENTER.isLeftAligned());
        assertFalse(Alignment.TOP_RIGHT.isLeftAligned());
        assertTrue(Alignment.CENTER_LEFT.isLeftAligned());
        assertFalse(Alignment.CENTER.isLeftAligned());
        assertFalse(Alignment.CENTER_RIGHT.isLeftAligned());
        assertTrue(Alignment.BOTTOM_LEFT.isLeftAligned());
        assertFalse(Alignment.BOTTOM_CENTER.isLeftAligned());
        assertFalse(Alignment.BOTTOM_RIGHT.isLeftAligned());
    }

    @Test
    public void testRightAlignment() {
        assertFalse(Alignment.TOP_LEFT.isRightAligned());
        assertFalse(Alignment.TOP_CENTER.isRightAligned());
        assertTrue(Alignment.TOP_RIGHT.isRightAligned());
        assertFalse(Alignment.CENTER_LEFT.isRightAligned());
        assertFalse(Alignment.CENTER.isRightAligned());
        assertTrue(Alignment.CENTER_RIGHT.isRightAligned());
        assertFalse(Alignment.BOTTOM_LEFT.isRightAligned());
        assertFalse(Alignment.BOTTOM_CENTER.isRightAligned());
        assertTrue(Alignment.BOTTOM_RIGHT.isRightAligned());
    }

    @Test
    public void testBottomAlignment() {
        assertFalse(Alignment.TOP_LEFT.isBottomAligned());
        assertFalse(Alignment.TOP_CENTER.isBottomAligned());
        assertFalse(Alignment.TOP_RIGHT.isBottomAligned());
        assertFalse(Alignment.CENTER_LEFT.isBottomAligned());
        assertFalse(Alignment.CENTER.isBottomAligned());
        assertFalse(Alignment.CENTER_RIGHT.isBottomAligned());
        assertTrue(Alignment.BOTTOM_LEFT.isBottomAligned());
        assertTrue(Alignment.BOTTOM_CENTER.isBottomAligned());
        assertTrue(Alignment.BOTTOM_RIGHT.isBottomAligned());
    }

    @Test
    public void testVerticallyCentered() {
        assertFalse(Alignment.TOP_LEFT.isCenteredVertically());
        assertFalse(Alignment.TOP_CENTER.isCenteredVertically());
        assertFalse(Alignment.TOP_RIGHT.isCenteredVertically());
        assertTrue(Alignment.CENTER_LEFT.isCenteredVertically());
        assertTrue(Alignment.CENTER.isCenteredVertically());
        assertTrue(Alignment.CENTER_RIGHT.isCenteredVertically());
        assertFalse(Alignment.BOTTOM_LEFT.isCenteredVertically());
        assertFalse(Alignment.BOTTOM_CENTER.isCenteredVertically());
        assertFalse(Alignment.BOTTOM_RIGHT.isCenteredVertically());
    }

    @Test
    public void testHorizontallyCentered() {
        assertFalse(Alignment.TOP_LEFT.isCenteredHorizontally());
        assertTrue(Alignment.TOP_CENTER.isCenteredHorizontally());
        assertFalse(Alignment.TOP_RIGHT.isCenteredHorizontally());
        assertFalse(Alignment.CENTER_LEFT.isCenteredHorizontally());
        assertTrue(Alignment.CENTER.isCenteredHorizontally());
        assertFalse(Alignment.CENTER_RIGHT.isCenteredHorizontally());
        assertFalse(Alignment.BOTTOM_LEFT.isCenteredHorizontally());
        assertTrue(Alignment.BOTTOM_CENTER.isCenteredHorizontally());
        assertFalse(Alignment.BOTTOM_RIGHT.isCenteredHorizontally());
    }

}