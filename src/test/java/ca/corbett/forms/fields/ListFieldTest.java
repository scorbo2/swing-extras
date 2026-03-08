package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
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
        assertInstanceOf(JPanel.class, actual.getFieldComponent());
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

        ((ListField)actual).setShouldExpand(true);
        assertTrue(actual.shouldExpand());
    }

    @Test
    public void testSetSelectionMode() {
        ListField<?> actualField = (ListField<?>)actual;
        JList<?> actualList = actualField.getList();
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
        ListField<?> actualField = (ListField<?>)actual;
        JList<?> actualList = actualField.getList();
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
        ListField<?> actualField = (ListField<?>)actual;
        JList<?> actualList = actualField.getList();

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
        ListField<?> actualField = (ListField<?>)actual;
        JList<?> actualList = actualField.getList();

        int[] rowCounts = new int[] {7,11,5};
        for (int rowCount : rowCounts) {
            actualField.setVisibleRowCount(rowCount);
            assertEquals(rowCount, actualField.getVisibleRowCount());
            assertEquals(rowCount, actualList.getVisibleRowCount());
        }
    }

    @Test
    public void testVisibleRowCount_ignoreStupidValues() {
        ListField<?> actualField = (ListField<?>)actual;
        JList<?> actualList = actualField.getList();
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
        ListField<?> actualField = (ListField<?>)actual;
        JList<?> actualList = actualField.getList();

        int[] cellWidths = new int[] {7,11,5,-1};
        for (int cellWidth : cellWidths) {
            actualField.setFixedCellWidth(cellWidth);
            assertEquals(cellWidth, actualField.getFixedCellWidth());
            assertEquals(cellWidth, actualList.getFixedCellWidth());
        }
    }

    @Test
    public void testCellWidth_rejectStupidValues() {
        ListField<?> actualField = (ListField<?>)actual;
        JList<?> actualList = actualField.getList();

        final int expected = 5;
        actualField.setFixedCellWidth(expected);

        actualField.setFixedCellWidth(-9999);
        assertEquals(expected, actualField.getFixedCellWidth());
        assertEquals(expected, actualList.getFixedCellWidth());
    }

    @Test
    public void testSelectedIndexes() {
        ListField<?> actualField = (ListField<?>)actual;

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
        ListField<?> actualField = (ListField<?>)actual;

        actualField.setSelectedIndex(0);
        actualField.setSelectedIndex(-999);
        actualField.setSelectedIndex(999);
        int[] selected = actualField.getSelectedIndexes();
        assertEquals(1, selected.length);
        assertEquals(0, selected[0]);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void selectionChange_withValueChangedListener_shouldNotify() {
        // Given a ListField with a ValueChangedListener:
        ListField<String> actualField = (ListField<String>)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actualField.addValueChangedListener(listener);

        // WHEN we make two changes to the selection:
        actualField.setSelectedIndex(1);
        actualField.setSelectedIndexes(new int[]{0, 1});

        // THEN our listener should be notified twice:
        Mockito.verify(listener, Mockito.times(2)).formFieldValueChanged(actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void selectionChange_withListDataListener_shouldNotNotify() {
        // GIVEN a ListField with a ListDataListener:
        ListField<String> actualField = (ListField<String>)actual;
        ListDataListener listener = Mockito.mock(ListDataListener.class);
        actualField.addListDataListener(listener);

        // WHEN we make two changes to the selection:
        actualField.setSelectedIndex(1);
        actualField.setSelectedIndexes(new int[]{0, 1});

        // THEN our listener should NOT be notified:
        Mockito.verify(listener, Mockito.times(0)).intervalAdded(Mockito.any());
        Mockito.verify(listener, Mockito.times(0)).intervalRemoved(Mockito.any());
        Mockito.verify(listener, Mockito.times(0)).contentsChanged(Mockito.any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listDataChange_withValueChangedListener_shouldNotNotify() {
        // GIVEN a ListField with a ValueChangedListener:
        ListField<String> actualField = (ListField<String>)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actualField.addValueChangedListener(listener);

        // WHEN we add or remove items directly in the underlying ListModel:
        actualField.getListModel().addElement("hello");

        // THEN our listener should NOT be notified, because this is not a "value" change:
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listDataChange_withListDataListener_shouldNotify() {
        // GIVEN a ListField with a ListDataListener:
        ListField<String> actualField = (ListField<String>)actual;
        ListDataListener listener = Mockito.mock(ListDataListener.class);
        actualField.addListDataListener(listener);

        // WHEN we add or remove items directly in the underlying ListModel:
        actualField.getListModel().addElement("hello");

        // THEN our listener should be notified:
        Mockito.verify(listener, Mockito.times(1)).intervalAdded(Mockito.any());
    }

    @Test
    public void testValueChangedOnSelectionModeChange_shouldNotifyOnce() {
        ListField<?> actualField = (ListField<?>)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(actual);
    }

    @Test
    public void testRemoveValueChangedListener() {
        ListField<?> actualField = (ListField<?>)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.setSelectedIndex(1);
        actualField.setSelectedIndexes(new int[]{0, 1});
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void validate_invalidScenario() {
        actual.addFieldValidator(new TestValidator());
        assertFalse(actual.isValid());
        assertEquals(TestValidator.MSG, actual.getValidationLabel().getToolTipText());
    }

    @Test
    public void validate_validScenario() {
        ListField<?> actualField = (ListField<?>)actual;
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
