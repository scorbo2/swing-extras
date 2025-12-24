package ca.corbett.extras.properties;

import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.FormField;
import org.junit.jupiter.api.Test;

import java.awt.FlowLayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ButtonPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new ButtonProperty(fullyQualifiedName, label);
    }

    @Test
    public void testButtonPropertyCreation() {
        ButtonProperty buttonProperty = new ButtonProperty("Category.Subcategory.ButtonProp", "Click Me");
        assertEquals("Category", buttonProperty.getCategoryName());
        assertEquals("Subcategory", buttonProperty.getSubCategoryName());
        assertEquals("ButtonProp", buttonProperty.getPropertyName());
        assertEquals("Click Me", buttonProperty.getPropertyLabel());
    }

    @Test
    public void setLayoutProperties_shouldUpdateLayout() {
        ButtonProperty buttonProperty = (ButtonProperty)actual;
        buttonProperty.setAlignment(FlowLayout.RIGHT);
        buttonProperty.setHgap(8);
        buttonProperty.setVgap(12);
        FormField formField = buttonProperty.generateFormFieldImpl();
        assertInstanceOf(ButtonField.class, formField);
        ButtonField buttonField = (ButtonField) formField;
        FlowLayout layout = (FlowLayout) buttonField.getFieldComponent().getLayout();
        assertEquals(FlowLayout.RIGHT, layout.getAlignment());
        assertEquals(8, layout.getHgap());
        assertEquals(12, layout.getVgap());
    }
}