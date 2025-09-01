package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractPropertyBaseTests {

    protected AbstractProperty actual;

    @BeforeEach
    public void setup() {
        actual = createTestObject("TestCategory.TestSubcategory.TestPropertyName", "TestLabel");
    }

    protected abstract AbstractProperty createTestObject(String fullyQualifiedName, String label);

    @Test
    public void testGetCategoryName() {
        assertEquals("TestCategory", actual.getCategoryName());
    }

    @Test
    public void testGetSubcategoryName() {
        assertEquals("TestSubcategory", actual.getSubCategoryName());
    }

    @Test
    public void testGetPropertyName() {
        assertEquals("TestPropertyName", actual.getPropertyName());
    }

    @Test
    public void testPropertyLabel() {
        assertEquals("TestLabel", actual.getPropertyLabel());

        actual.setPropertyLabel("hello");
        assertEquals("hello", actual.getPropertyLabel());

        actual.setPropertyLabel("TestLabel");
        assertEquals("TestLabel", actual.getPropertyLabel());
    }

    @Test
    public void testPropertyFieldNaming_withVariousInputs_shouldNameCorrectly() {
        AbstractProperty testProperty = createTestObject("A.B.C", "");
        assertEquals("A", testProperty.getCategoryName());
        assertEquals("B", testProperty.getSubCategoryName());
        assertEquals("C", testProperty.getPropertyName());
        assertEquals("A.B.C", testProperty.getFullyQualifiedName());

        testProperty = createTestObject("A.B", "");
        assertEquals("A", testProperty.getCategoryName());
        assertEquals(AbstractProperty.DEFAULT_CATEGORY, testProperty.getSubCategoryName());
        assertEquals("B", testProperty.getPropertyName());
        assertEquals("A.General.B", testProperty.getFullyQualifiedName());

        testProperty = createTestObject("A", "");
        assertEquals(AbstractProperty.DEFAULT_CATEGORY, testProperty.getCategoryName());
        assertEquals(AbstractProperty.DEFAULT_CATEGORY, testProperty.getSubCategoryName());
        assertEquals("A", testProperty.getPropertyName());
        assertEquals("General.General.A", testProperty.getFullyQualifiedName());

        testProperty = createTestObject("A.B.C.D", "");
        assertEquals("A", testProperty.getCategoryName());
        assertEquals("B", testProperty.getSubCategoryName());
        assertEquals("C.D", testProperty.getPropertyName());
        assertEquals("A.B.C.D", testProperty.getFullyQualifiedName());
    }

    @Test
    public void testPropertySorting_withVariousInputs_shouldSortCorrectly() {
        AbstractProperty testField1 = createTestObject("A.B.C2", "");
        AbstractProperty testField2 = createTestObject("A.B.C1", "");
        AbstractProperty testField3 = createTestObject("D.E.F1", "");
        AbstractProperty testField4 = createTestObject("G.A.F2", "");
        AbstractProperty testField5 = createTestObject("0.0.0", "");
        AbstractProperty testField6 = createTestObject("0", "");
        AbstractProperty testField7 = createTestObject("A.0", "");
        AbstractProperty testField8 = createTestObject("zzzz.last", "");
        AbstractProperty testField9 = createTestObject("zzzz.first", "");
        List<AbstractProperty> list = new ArrayList<>();
        list.add(testField1);
        list.add(testField2);
        list.add(testField3);
        list.add(testField4);
        list.add(testField5);
        list.add(testField6);
        list.add(testField7);
        list.add(testField8);
        list.add(testField9);
        list.sort(new PropertyComparator());
        assertEquals(testField5, list.get(0));
        assertEquals(testField8, list.get(8));
    }

    @Test
    public void testIsExposed() {
        assertTrue(actual.isExposed());

        actual.setExposed(false);
        assertFalse(actual.isExposed());

        actual.setExposed(true);
        assertTrue(actual.isExposed());
    }

    @Test
    public void testIsEnabled() {
        assertTrue(actual.isEnabled());

        actual.setEnabled(false);
        assertFalse(actual.isEnabled());

        actual.setEnabled(true);
        assertTrue(actual.isEnabled());
    }

    @Test
    public void testIsInitiallyEditable() {
        actual.setInitiallyEditable(false);
        assertFalse(actual.isInitiallyEditable());

        try {
            FormField formField = actual.generateFormField();
            assertFalse(formField.getFieldLabel().isEnabled());
            assertFalse(formField.getFieldComponent().isEnabled());
            assertFalse(formField.getHelpLabel().isEnabled());
            assertFalse(formField.getValidationLabel().isEnabled());
        }
        catch (UnsupportedOperationException ignored) {
            // This is fine... not all properties support form field generation.
        }

        actual.setInitiallyEditable(true);
        assertTrue(actual.isInitiallyEditable());

        try {
            FormField formField = actual.generateFormField();
            assertTrue(formField.getFieldLabel().isEnabled());
            assertTrue(formField.getFieldComponent().isEnabled());
            assertTrue(formField.getHelpLabel().isEnabled());
            assertTrue(formField.getValidationLabel().isEnabled());
        }
        catch (UnsupportedOperationException ignored) {
            // This is fine... not all properties support form field generation.
        }
    }

    @Test
    public void testIsInitiallyVisible() {
        actual.setInitiallyVisible(false);
        assertFalse(actual.isInitiallyVisible());

        try {
            FormField formField = actual.generateFormField();
            assertFalse(formField.getFieldLabel().isVisible());
            assertFalse(formField.getFieldComponent().isVisible());
            assertFalse(formField.getHelpLabel().isVisible());
            assertFalse(formField.getValidationLabel().isVisible());
        }
        catch (UnsupportedOperationException ignored) {
            // This is fine... not all properties support form field generation.
        }

        actual.setInitiallyVisible(true);
        assertTrue(actual.isInitiallyVisible());

        try {
            FormField formField = actual.generateFormField();
            assertTrue(formField.getFieldLabel().isVisible());
            assertTrue(formField.getFieldComponent().isVisible());
            assertTrue(formField.getHelpLabel().isVisible());
            assertTrue(formField.getValidationLabel().isVisible());
        }
        catch (UnsupportedOperationException ignored) {
            // This is fine... not all properties support form field generation.
        }
    }

    @Test
    public void testHelpText() {
        final String HELP = "this is help text";
        actual.setHelpText(HELP);
        assertEquals(HELP, actual.getHelpText());

        try {
            FormField formField = actual.generateFormField();
            assertEquals(HELP, formField.getHelpLabel().getToolTipText());
        }
        catch (UnsupportedOperationException ignored) {
            // this is fine... not all properties support form field generation.
        }

        actual.setHelpText("");
        assertTrue(actual.getHelpText().isBlank());
    }

    @Test
    public void testExtraAttributes() {
        final String ATTR_NAME = "TestAttribute";
        final String ATTR_VALUE = "TestAttributeValue";
        assertNull(actual.getExtraAttribute(ATTR_NAME));

        actual.setExtraAttribute(ATTR_NAME, ATTR_VALUE);
        assertEquals(ATTR_VALUE, actual.getExtraAttribute(ATTR_NAME));

        actual.clearExtraAttribute(ATTR_NAME);
        assertNull(actual.getExtraAttribute(ATTR_NAME));
    }
}
