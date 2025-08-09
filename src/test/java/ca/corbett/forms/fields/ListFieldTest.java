package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ListField<String>("Label", List.of("One", "Two", "Three"))
                .setSelectedIndexes(new int[] {0,2});
    }

    @Test
    public void testGetFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JList.class, actual.getFieldComponent());
    }

    @Test
    public void testGetFieldLabel() {
        assertTrue(actual.hasFieldLabel());
        assertEquals("Label", actual.getFieldLabel().getText());
    }

    @Test
    public void testIsMultiLine() {
        assertTrue(actual.isMultiLine());
    }

    @Test
    public void testShouldExpand() {
        assertFalse(actual.shouldExpand());
    }

    @Test
    public void testSetSelectionMode() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;
        //noinspection unchecked
        JList<String> actualList = (JList<String>)actualField.getFieldComponent();
        assertEquals(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, actualField.getSelectionMode());
        assertEquals(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, actualList.getSelectionMode());

        int[] selectionModes = new int[] {ListSelectionModel.SINGLE_SELECTION,
                ListSelectionModel.SINGLE_INTERVAL_SELECTION,
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION};
        for (int selectionMode : selectionModes) {
            actualField.setSelectionMode(selectionMode);
            assertEquals(selectionMode, actualField.getSelectionMode());
            assertEquals(selectionMode, actualList.getSelectionMode());
        }
    }

    @Test
    public void testSetSelectionMode_ignoreStupidValues() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;
        //noinspection unchecked
        JList<String> actualList = (JList<String>)actualField.getFieldComponent();
        actualField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        actualField.setSelectionMode(9999);
        assertEquals(ListSelectionModel.SINGLE_SELECTION, actualField.getSelectionMode());
        assertEquals(ListSelectionModel.SINGLE_SELECTION, actualList.getSelectionMode());

        actualField.setSelectionMode(-9999);
        assertEquals(ListSelectionModel.SINGLE_SELECTION, actualField.getSelectionMode());
        assertEquals(ListSelectionModel.SINGLE_SELECTION, actualList.getSelectionMode());
    }

    @Test
    public void testSetLayoutOrientation() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;
        //noinspection unchecked
        JList<String> actualList = (JList<String>)actualField.getFieldComponent();

        int[] orientation = new int[] {JList.VERTICAL,
                JList.VERTICAL_WRAP,
                JList.HORIZONTAL_WRAP};
        for (int value : orientation) {
            actualField.setLayoutOrientation(value);
            assertEquals(value, actualField.getLayoutOrientation());
            assertEquals(value, actualList.getLayoutOrientation());
        }
    }

    @Test
    public void testVisibleRowCount() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;
        //noinspection unchecked
        JList<String> actualList = (JList<String>)actualField.getFieldComponent();

        int[] rowCounts = new int[] {7,11,5};
        for (int rowCount : rowCounts) {
            actualField.setVisibleRowCount(rowCount);
            assertEquals(rowCount, actualField.getVisibleRowCount());
            assertEquals(rowCount, actualList.getVisibleRowCount());
        }
    }

    @Test
    public void testVisibleRowCount_ignoreStupidValues() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;
        //noinspection unchecked
        JList<String> actualList = (JList<String>)actualField.getFieldComponent();
        final int expected = 3;
        actualField.setVisibleRowCount(expected);

        actualField.setVisibleRowCount(-9999);
        assertEquals(expected, actualField.getVisibleRowCount());
        assertEquals(expected, actualList.getVisibleRowCount());

        // This is possibly also a "stupid" value, but it is technically valid:
        actualField.setVisibleRowCount(9999);
        assertEquals(9999, actualField.getVisibleRowCount());
        assertEquals(9999, actualList.getVisibleRowCount());
    }

    @Test
    public void testCellWidth() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;
        //noinspection unchecked
        JList<String> actualList = (JList<String>)actualField.getFieldComponent();

        int[] cellWidths = new int[] {7,11,5,-1};
        for (int cellWidth : cellWidths) {
            actualField.setFixedCellWidth(cellWidth);
            assertEquals(cellWidth, actualField.getFixedCellWidth());
            assertEquals(cellWidth, actualList.getFixedCellWidth());
        }
    }

    @Test
    public void testCellWidth_rejectStupidValues() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;
        //noinspection unchecked
        JList<String> actualList = (JList<String>)actualField.getFieldComponent();

        final int expected = 5;
        actualField.setFixedCellWidth(expected);

        actualField.setFixedCellWidth(-9999);
        assertEquals(expected, actualField.getFixedCellWidth());
        assertEquals(expected, actualList.getFixedCellWidth());
    }

    @Test
    public void testSelectedIndexes() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;

        actualField.setSelectedIndex(0);
        int[] selected = actualField.getSelectedIndexes();
        assertEquals(1, selected.length);
        assertEquals(0, selected[0]);

        int[] expected = new int[]{0,1,2};
        actualField.setSelectedIndexes(expected);
        selected = actualField.getSelectedIndexes();
        assertEquals(3, selected.length);
        assertArrayEquals(expected, actualField.getSelectedIndexes());
    }

    @Test
    public void testSelectedIndexes_ignoreStupidValues() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;

        actualField.setSelectedIndex(0);
        actualField.setSelectedIndex(-999);
        actualField.setSelectedIndex(999);
        int[] selected = actualField.getSelectedIndexes();
        assertEquals(1, selected.length);
        assertEquals(0, selected[0]);
    }

    @Test
    public void validate_invalidScenario() {
        actual.addFieldValidator(new TestValidator());
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        //noinspection unchecked
        ListField<String> actualField = (ListField<String>)actual;
        actual.addFieldValidator(new TestValidator());
        actualField.setSelectedIndex(1);
        assertTrue(actual.isValid());
        assertNull(actual.getValidationLabel().getToolTipText());
    }

    private static class TestValidator implements FieldValidator<ListField<?>> {

        public static final String MSG = "the middle item must be selected";

        @Override
        public ValidationResult validate(ListField<?> fieldToValidate) {
            int[] selected = fieldToValidate.getSelectedIndexes();
            return selected.length == 1 && selected[0] == 1
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MSG);
        }
    }
}