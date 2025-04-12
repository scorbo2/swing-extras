package ca.corbett.extras.about;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the AboutInfo class.
 *
 * @author scorbo2
 * @since 2025-03-11
 */
class AboutInfoTest {

    @Test
    public void testAddCustomField_withMultipleCustomFields_shouldAdd() {
        AboutInfo info = new AboutInfo();
        assertEquals(0, info.getCustomFieldNames().size());

        info.addCustomField("field1", "value1");
        assertEquals(1, info.getCustomFieldNames().size());
        assertEquals("value1", info.getCustomFieldValue("field1"));

        info.addCustomField("field2", "value2");
        assertEquals(2, info.getCustomFieldNames().size());
        assertEquals("value2", info.getCustomFieldValue("field2"));

        info.clearCustomFields();
        assertEquals(0, info.getCustomFieldNames().size());
        assertNull(info.getCustomFieldValue("field1"));
    }

    @Test
    public void testUpdateCustomFieldValue_withNewValue_shouldUpdate() {
        AboutInfo info = new AboutInfo();
        assertEquals(0, info.getCustomFieldNames().size());
        info.addCustomField("field1", "value1");
        assertEquals("value1", info.getCustomFieldValue("field1"));
        info.updateCustomField("field1", "some new value");
        assertEquals("some new value", info.getCustomFieldValue("field1"));
    }
}