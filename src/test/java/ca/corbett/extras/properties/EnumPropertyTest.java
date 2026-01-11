package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EnumPropertyTest extends AbstractPropertyBaseTests {

    enum TestEnum1 {
        VALUE1, VALUE2, VALUE3;
    }

    enum TestEnumWithLabels {
        VALUE1("This is value 1"),
        VALUE2("This is value 2"),
        VALUE3("This is value 3");

        private final String label;

        TestEnumWithLabels(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new EnumProperty<TestEnumWithLabels>(name, label, TestEnumWithLabels.VALUE1);
    }

    @Test
    public void getSelectedItem_withValidEnum_shouldSucceed() {
        // GIVEN an EnumProperty with a valid enum:
        EnumProperty<TestEnum1> testProp = new EnumProperty<>("test1", "test1", TestEnum1.VALUE2);

        // WHEN we try to get the selected item:
        TestEnum1 actual = testProp.getSelectedItem();

        // THEN it should be a valid output:
        assertEquals(TestEnum1.VALUE2, actual);
    }

    @Test
    public void setSelectedItem_withValidEnum_shouldSucceed() {
        // GIVEN an EnumProperty with a valid selection:
        EnumProperty<TestEnum1> testProp = new EnumProperty<>("test1", "test1", TestEnum1.VALUE2);

        // WHEN we try to set the selected item using a valid enum value:
        testProp.setSelectedItem(TestEnum1.VALUE1);

        // THEN it should change the selection to that value:
        assertEquals(TestEnum1.VALUE1, testProp.getSelectedItem());
    }

    @Test
    public void generateFormField_withValidEnum_shouldSucceed() {
        // GIVEN an EnumProperty with a valid selection:
        EnumProperty<TestEnum1> testProp = new EnumProperty<>("test1", "test1", TestEnum1.VALUE1);

        // WHEN we generate a form field from it:
        FormField formField = testProp.generateFormField();

        // THEN we should see a valid form field with expected options:
        assertInstanceOf(ComboField.class, formField);
        ComboField comboField = (ComboField)formField;
        assertEquals(0, comboField.getSelectedIndex());
        assertEquals(TestEnum1.VALUE1.name(), comboField.getSelectedItem());
    }

    @Test
    public void generateFormField_withValidEnumWithLabels_shouldSucceed() {
        // GIVEN an EnumProperty that has labels as well as names:
        EnumProperty<TestEnumWithLabels> testProp = new EnumProperty<>("test1", "test1", TestEnumWithLabels.VALUE1);

        // WHEN we generate a form field from it:
        FormField formField = testProp.generateFormField();

        // THEN we should see a valid form field with expected options:
        assertInstanceOf(ComboField.class, formField);
        ComboField comboField = (ComboField)formField;
        assertEquals(0, comboField.getSelectedIndex());
        assertEquals(TestEnumWithLabels.VALUE1.toString(), comboField.getSelectedItem());
    }

    @Test
    public void loadFromFormField_givenValidFormField_shouldSucceed() {
        // GIVEN a FormField of the correct type with the correct options:
        ComboField<TestEnum1> comboField = new ComboField<>("Test", List.of(TestEnum1.values()), 2, false);
        comboField.setIdentifier("my.field.test1");

        // WHEN we try to load an EnumProperty from that field:
        EnumProperty<TestEnum1> enumProperty = new EnumProperty<>("my.field.test1", "test1", TestEnum1.VALUE1);
        enumProperty.loadFromFormField(comboField);

        // THEN the selection should have been updated appropriately:
        assertEquals(TestEnum1.VALUE3, enumProperty.getSelectedItem());
    }

    @Test
    public void saveToProps_withName_shouldSucceed() {
        // GIVEN an enum with valid values:
        EnumProperty<TestEnum1> enumProperty = new EnumProperty<>("my.test.field", "hello", TestEnum1.VALUE2);

        // WHEN we save it to a properties object:
        Properties props = new Properties();
        enumProperty.saveToProps(props);

        // THEN it should have saved as expected:
        assertEquals(1, props.getPropertyNames().size());
        assertEquals("my.test.field", props.getPropertyNames().get(0));
        assertEquals(TestEnum1.VALUE2.name(), props.getString("my.test.field", "defaultShouldBeIgnored"));
    }

    @Test
    public void saveToProps_withLabel_shouldIgnoreLabelAndUseName() {
        // GIVEN an enum that has labels:
        EnumProperty<TestEnumWithLabels> enumProperty = new EnumProperty<>("my.test.field", "hello",
                                                                           TestEnumWithLabels.VALUE3);

        // WHEN we save it to a properties object:
        Properties props = new Properties();
        enumProperty.saveToProps(props);

        // THEN it should have saved as expected:
        assertEquals(1, props.getPropertyNames().size());
        assertEquals("my.test.field", props.getPropertyNames().get(0));
        assertEquals(TestEnum1.VALUE3.name(), props.getString("my.test.field", "defaultShouldBeIgnored"));
    }

    @Test
    public void loadFromProps_withValidProps_shouldSucceed() {
        // GIVEN a properties instance that has a valid value:
        Properties props = new Properties();
        props.setString("my.test.field", TestEnum1.VALUE3.name());

        // WHEN we try to load from props:
        EnumProperty<TestEnum1> enumProperty = new EnumProperty<>("my.test.field", "blah", TestEnum1.VALUE1);
        enumProperty.loadFromProps(props);

        // THEN the field should have updated correctly:
        assertEquals(TestEnum1.VALUE3, enumProperty.getSelectedItem());
    }

    @Test
    public void loadFromProps_withInvalidProps_shouldFail() {
        // GIVEN a properties instance that has an valid value:
        Properties props = new Properties();
        props.setString("my.test.field", "This value is just wrong, plain wrong.");

        // WHEN we try to load from props:
        EnumProperty<TestEnum1> enumProperty = new EnumProperty<>("my.test.field", "blah", TestEnum1.VALUE1);
        enumProperty.loadFromProps(props);

        // THEN the field should have ignored the load request and stuck with the default value:
        assertEquals(TestEnum1.VALUE1, enumProperty.getSelectedItem());
    }

    @Test
    public void formFieldChangeListener_withFormFieldChanges_shouldFireChangeEvents() {
        // GIVEN a test prop with a mocked property form field change listener on it:
        EnumProperty<TestEnum1> testProp = new EnumProperty<>("test", "test", TestEnum1.VALUE1);
        PropertyFormFieldChangeListener listener = Mockito.mock(PropertyFormFieldChangeListener.class);
        testProp.addFormFieldChangeListener(listener);

        // WHEN we generate a form field and mess with it a bit:
        //noinspection unchecked
        ComboField<String> formField = (ComboField<String>)testProp.generateFormField();
        formField.setSelectedIndex(1);
        formField.setSelectedIndex(2);

        // THEN we should see our change listener got invoked:
        Mockito.verify(listener, Mockito.times(2)).valueChanged(Mockito.any());
    }
}