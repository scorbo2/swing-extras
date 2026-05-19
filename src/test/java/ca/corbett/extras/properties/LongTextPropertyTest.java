package ca.corbett.extras.properties;

import ca.corbett.forms.fields.LongTextField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongTextPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new LongTextProperty(name, label, LongTextField.TextFieldType.MULTI_LINE_FIXED_ROWS_COLS, 20, 3);
    }

    @Test
    public void formFieldChangeListener_withFormFieldChanges_shouldFireChangeEvents() {
        // GIVEN a test prop with a mocked property form field change listener on it:
        LongTextProperty testProp = new LongTextProperty("test",
                                                         "test",
                                                         LongTextField.TextFieldType.MULTI_LINE_FIXED_ROWS_COLS,
                                                         20,
                                                         5);
        testProp.setValue("Test text");
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it a bit:
        LongTextField formField = (LongTextField)testProp.generateFormField();
        formField.setText("Modified text");

        // THEN we should see our change listener got invoked:
        // (note: we only made one change, but we get 2 events, because DocumentListener is broken)
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }

    @Test
    public void ofDynamicSizingMultiLine_defaultOverload_shouldUseZeroRows() {
        LongTextProperty property = LongTextProperty.ofDynamicSizingMultiLine("name", "label");

        LongTextField field = (LongTextField)property.generateFormField();

        assertEquals(0, field.getTextArea().getRows());
    }

    @Test
    public void ofDynamicSizingMultiLine_withRows_shouldUseSpecifiedRows() {
        LongTextProperty property = LongTextProperty.ofDynamicSizingMultiLine("name", "label", 5);

        LongTextField field = (LongTextField)property.generateFormField();

        assertEquals(5, field.getTextArea().getRows());
    }
}
