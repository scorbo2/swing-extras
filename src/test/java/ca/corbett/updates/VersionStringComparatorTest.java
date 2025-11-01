package ca.corbett.updates;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}