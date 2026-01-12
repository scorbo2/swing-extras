package ca.corbett.extras.properties;

import ca.corbett.forms.Margins;
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

    @Test
    public void addFormFieldGenerationListener_withValidListener_shouldModify() {
        final int customMargin = 99;

        // GIVEN a custom FormFieldGeneration listener that sets a custom margin:
        actual.addFormFieldGenerationListener((p, f) -> f.getMargins().setLeft(customMargin));

        // WHEN we generate a FormField from this property:
        FormField formField = actual.generateFormField();

        // THEN we should see our custom margin:
        assertEquals(customMargin, formField.getMargins().getLeft());
    }

    @Test
    public void addFormFieldGenerationListener_withMultipleListeners_lastOneShouldWin() {
        // GIVEN a pair of FormFieldGenerationListeners that have conflicting instructions:
        actual.addFormFieldGenerationListener((p, f) -> f.getMargins().setLeft(88));
        actual.addFormFieldGenerationListener((p, f) -> f.getMargins().setLeft(99));

        // WHEN we generate a FormField from this property:
        FormField formField = actual.generateFormField();

        // THEN we should see that the last one added wins:
        assertEquals(99, formField.getMargins().getLeft());
    }

    @Test
    public void addFormFieldGenerationListener_withIdentifierChange_shouldIgnore() {
        // GIVEN a FormFieldGenerationListener that tries to change the FormField's identifier (not allowed!):
        actual.addFormFieldGenerationListener((p, f) -> f.setIdentifier("invalid!"));

        // WHEN we generate a FormField from this property:
        FormField formField = actual.generateFormField();

        // THEN we should see that our change was utterly ignored:
        assertEquals(actual.fullyQualifiedName, formField.getIdentifier());
    }

    @Test
    public void addPadding_withNoValues_shouldUseDefaults() {
        // WHEN we DON'T add padding to our property:
        int expectedTop = Margins.DEFAULT_MARGIN;
        int expectedBottom = Margins.DEFAULT_MARGIN;

        // We have to special-case LabelProperty, because it can add its own padding values as well:
        if (actual instanceof LabelProperty) {
            LabelProperty labelProp = (LabelProperty)actual;
            expectedTop += labelProp.getExtraTopMargin();
            expectedBottom += labelProp.getExtraBottomMargin();
        }

        // THEN we should see the default values in the generated FormField:
        FormField formField = actual.generateFormField();
        assertEquals(expectedTop, formField.getMargins().getTop());
        assertEquals(Margins.DEFAULT_MARGIN, formField.getMargins().getLeft());
        assertEquals(expectedBottom, formField.getMargins().getBottom());
        assertEquals(Margins.DEFAULT_MARGIN, formField.getMargins().getRight());
        assertEquals(Margins.DEFAULT_MARGIN, formField.getMargins().getInternalSpacing());
    }

    @Test
    public void addPadding_withPaddingValues_shouldAddCorrectly() {
        // GIVEN padding values:
        int top = 1;
        int left = 2;
        int bottom = 3;
        int right = 4;
        int inner = 5;
        actual.addPadding(left, top, right, bottom, inner);

        // WHEN we generate a form field from our property:
        FormField formField = actual.generateFormField();

        // THEN we should see those values in the generated FormField:
        //      They should be ADDED to the default as padding, they shouldn't replace the defaults!
        int expectedLeft = Margins.DEFAULT_MARGIN + left;
        int expectedTop = Margins.DEFAULT_MARGIN + top;
        int expectedBottom = Margins.DEFAULT_MARGIN + bottom;
        int expectedRight = Margins.DEFAULT_MARGIN + right;
        int expectedInner = Margins.DEFAULT_MARGIN + inner;

        // We have to special-case LabelProperty, because it can add its own padding values as well:
        if (actual instanceof LabelProperty) {
            LabelProperty labelProp = (LabelProperty)actual;
            expectedTop += labelProp.getExtraTopMargin();
            expectedBottom += labelProp.getExtraBottomMargin();
        }

        assertEquals(expectedTop, formField.getMargins().getTop());
        assertEquals(expectedLeft, formField.getMargins().getLeft());
        assertEquals(expectedBottom, formField.getMargins().getBottom());
        assertEquals(expectedRight, formField.getMargins().getRight());
        assertEquals(expectedInner, formField.getMargins().getInternalSpacing());
    }

    @Test
    public void addPadding_withFormFieldGenerationOverride_shouldBeOverridden() {
        // GIVEN initial padding values:
        int top = 1;
        int left = 2;
        int bottom = 3;
        int right = 4;
        int inner = 5;
        actual.addPadding(left, top, right, bottom, inner);

        // And GIVEN a FormFieldGenerationListener that overrides those values:
        actual.addFormFieldGenerationListener((p, f) -> {
            f.getMargins().setTop(10);
            f.getMargins().setLeft(20);
            f.getMargins().setBottom(30);
            f.getMargins().setRight(40);
            f.getMargins().setInternalSpacing(50);
        });

        // WHEN we generate a FormField from this property:
        FormField formField = actual.generateFormField();

        // THEN we should see that our input values should have been overridden:
        assertEquals(10, formField.getMargins().getTop());
        assertEquals(20, formField.getMargins().getLeft());
        assertEquals(30, formField.getMargins().getBottom());
        assertEquals(40, formField.getMargins().getRight());
        assertEquals(50, formField.getMargins().getInternalSpacing());
    }
}
