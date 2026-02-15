package ca.corbett.extras.properties;

import ca.corbett.forms.fields.MarginsField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MarginsPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new MarginsProperty(fullyQualifiedName, label);
    }

    @Test
    public void setHeaderLabel_withNonNullHeaderLabel_shouldSetFieldHeaderLabel() {
        // GIVEN a MarginsProperty with a non-null header label:
        MarginsProperty prop = new MarginsProperty("test", "Test");
        prop.setHeaderLabel("My Header Label");

        // WHEN we generate a MarginsField from this property:
        MarginsField field = (MarginsField)prop.generateFormField();

        // THEN the MarginsField should have the same header label as the property:
        assertEquals("My Header Label", field.getHeaderLabel());
    }

    @Test
    public void setHeaderLabel_withNullHeaderLabel_shouldSetFieldHeaderLabelToNull() {
        // GIVEN a MarginsProperty with a null header label:
        MarginsProperty prop = new MarginsProperty("test", "Test");
        prop.setHeaderLabel(null);

        // WHEN we generate a MarginsField from this property:
        MarginsField field = (MarginsField)prop.generateFormField();

        // THEN the MarginsField should have a null header label:
        assertNull(field.getHeaderLabel());
    }
}
