package ca.corbett.updates;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionStringComparatorTest {


    @Test
    public void versionStringSorting() {
        // GIVEN version strings that will sort incorrectly out of the box:
        List<String> versions = new ArrayList<>();
        versions.add("11.0");
        versions.add("2.1");

        // WHEN we try to sort it using default string comparison:
        versions.sort(null);

        // THEN we should see that the order is wrong, because string sorting of numeric values is wonky:
        assertEquals("11.0", versions.get(0));

        // BUT, WHEN we use our version string comparator:
        versions.sort(new VersionStringComparator());

        // THEN we should see better results:
        assertEquals("2.1", versions.get(0));
    }

    @Test
    public void convertVersionToSafeCompareString() {
        // Given version strings of various lengths, should normalize to three segments with three digits per segment:
        assertEquals("001000000", VersionStringComparator.convertVersionToSafeCompareString("1"));
        assertEquals("001000000", VersionStringComparator.convertVersionToSafeCompareString("1.0"));
        assertEquals("001000000", VersionStringComparator.convertVersionToSafeCompareString("1.0.0"));
        assertEquals("001000000", VersionStringComparator.convertVersionToSafeCompareString("1.0.0.0"));

        // Given version strings with non-numeric stuff in them, should get stripped out:
        assertEquals("001000000", VersionStringComparator.convertVersionToSafeCompareString("v1-SNAPSHOT"));
        assertEquals("000000000", VersionStringComparator.convertVersionToSafeCompareString("hello"));
        assertEquals("001002003", VersionStringComparator.convertVersionToSafeCompareString("v1.?2.!3.asdf"));
    }

    @Test
    public void isNewerThan() {
        // Happy path
        assertTrue(VersionStringComparator.isNewerThan("1.1", "1.0"));
        assertTrue(VersionStringComparator.isNewerThan("1.1", "1"));
        assertTrue(VersionStringComparator.isNewerThan("1.1", "1.0.1"));
        assertTrue(VersionStringComparator.isNewerThan("1", "0"));
        assertTrue(VersionStringComparator.isNewerThan("1", "0.9"));
        assertTrue(VersionStringComparator.isNewerThan("11", "1.111111"));

        // Unhappy path
        assertFalse(VersionStringComparator.isNewerThan("1.0", "1.0"));
        assertFalse(VersionStringComparator.isNewerThan("1.0", "1.1"));
    }

    @Test
    public void isOlderThan() {
        // Happy path
        assertTrue(VersionStringComparator.isOlderThan("1.0", "2.0"));
        assertTrue(VersionStringComparator.isOlderThan("1.0", "1.1"));
        assertTrue(VersionStringComparator.isOlderThan("1.0", "11"));
        assertTrue(VersionStringComparator.isOlderThan("1.0", "1.0.1"));
        assertTrue(VersionStringComparator.isOlderThan("1.0", "2"));
        assertTrue(VersionStringComparator.isOlderThan("1.0", "2.0.0.0.0"));
        assertTrue(VersionStringComparator.isOlderThan("1.0", "10"));

        // Unhappy path
        assertFalse(VersionStringComparator.isOlderThan("1.0", "1.0"));
        assertFalse(VersionStringComparator.isOlderThan("2.0", "1.0"));
    }

    @Test
    public void isAtLeast() {
        // Happy path
        assertTrue(VersionStringComparator.isAtLeast("1.1", "1.0"));
        assertTrue(VersionStringComparator.isAtLeast("1.0", "1.0"));
        assertTrue(VersionStringComparator.isAtLeast("1.0", "0.9"));
        assertTrue(VersionStringComparator.isAtLeast("2.0", "1.9.9"));

        // Unhappy path
        assertFalse(VersionStringComparator.isAtLeast("1.0", "1.1"));
        assertFalse(VersionStringComparator.isAtLeast("1.0", "10.10"));
    }

    @Test
    public void isAtMost() {
        // Happy path
        assertTrue(VersionStringComparator.isAtMost("1.0", "1.1"));
        assertTrue(VersionStringComparator.isAtMost("1.0", "1.0"));
        assertTrue(VersionStringComparator.isAtMost("0.9", "1.0"));
        assertTrue(VersionStringComparator.isAtMost("1.9.9", "2.0"));

        // Unhappy path
        assertFalse(VersionStringComparator.isAtMost("1.1", "1.0"));
        assertFalse(VersionStringComparator.isAtMost("10.10", "1.0"));
    }

    @Test
    public void isExactly() {
        // Happy path
        assertTrue(VersionStringComparator.isExactly("1.0", "1.0"));
        assertTrue(VersionStringComparator.isExactly("1.0.0.0.0.0", "1.0"));

        // Unhappy path
        assertFalse(VersionStringComparator.isExactly("1.0", "1.1"));
        assertFalse(VersionStringComparator.isExactly("1.0", "0.9"));
        assertFalse(VersionStringComparator.isExactly("2.0", "1.9.9"));
    }
}