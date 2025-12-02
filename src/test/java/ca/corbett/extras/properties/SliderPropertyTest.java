package ca.corbett.extras.properties;

import ca.corbett.forms.fields.SliderField;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SliderPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new SliderProperty(fullyQualifiedName, label, 0, 100, 50);
    }

    @Test
    public void saveToProps_shouldSaveAndLoad() {
        // GIVEN a prop with test values:
        SliderProperty expectedProp = (SliderProperty)actual;
        expectedProp.setColorStops(List.of(Color.BLUE, Color.GREEN, Color.YELLOW));
        expectedProp.setLabels(List.of("one", "two", "three", "four", "five"), true);

        // WHEN we save it:
        Properties props = new Properties();
        expectedProp.saveToProps(props);
        SliderProperty actualProp = new SliderProperty(expectedProp.fullyQualifiedName, expectedProp.getPropertyLabel(), 0,0,0);
        actualProp.loadFromProps(props);

        // THEN the test values should have been restored:
        assertEquals(expectedProp.getMinValue(), actualProp.getMinValue());
        assertEquals(expectedProp.getMaxValue(), actualProp.getMaxValue());
        assertEquals(expectedProp.getValue(), actualProp.getValue());
        assertEquals(expectedProp.isAllowNumericValueInLabel(), actualProp.isAllowNumericValueInLabel());
        assertEquals(expectedProp.getColorStops().size(), actualProp.getColorStops().size());
        assertEquals(expectedProp.getLabels().size(), actualProp.getLabels().size());
        for (int i = 0; i < expectedProp.getColorStops().size(); i++) {
            assertEquals(expectedProp.getColorStops().get(i), actualProp.getColorStops().get(i));
        }
        for (int i = 0; i < expectedProp.getLabels().size(); i++) {
            assertEquals(expectedProp.getLabels().get(i), actualProp.getLabels().get(i));
        }
    }

    @Test
    public void generateFormField_shouldGenerateAndLoadChanges() {
        // GIVEN a prop with test values:
        SliderProperty testProp = (SliderProperty)actual;

        // WHEN we generate a form field from it and change it:
        SliderField field = (SliderField)testProp.generateFormField();
        field.setValue(77);
        testProp.loadFromFormField(field);

        // THEN we should see the value was loaded from the form field:
        assertEquals(77, testProp.getValue());
    }
}