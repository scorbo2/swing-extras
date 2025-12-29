package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.PanelField;
import org.junit.jupiter.api.Test;

import java.awt.FlowLayout;
import java.awt.LayoutManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class PanelPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        PanelProperty prop = new PanelProperty(fullyQualifiedName);
        prop.setPropertyLabel(label); // panels have no label, but this is needed for the parent tests
        return prop;
    }

    @Test
    public void testLayoutManager_getterAndSetter_shouldWork() {
        PanelProperty prop = new PanelProperty("propId");
        assertNull(prop.getLayoutManager());

        java.awt.BorderLayout layout = new java.awt.BorderLayout();
        prop.setLayoutManager(layout);
        assertEquals(layout, prop.getLayoutManager());
    }

    @Test
    public void testSaveToProps_and_LoadFromProps_shouldDoNothing() {
        PanelProperty prop = new PanelProperty("propId");
        Properties props = new Properties();

        // Just ensure no exceptions are thrown
        prop.saveToProps(props);
        prop.loadFromProps(props);

        // Should have added nothing to props:
        assertEquals(0, props.getPropertyNames().size());
    }

    @Test
    public void testGenerateFormFieldImpl_shouldCreatePanelField() {
        LayoutManager layoutManager = new FlowLayout(FlowLayout.LEFT);
        PanelProperty prop = new PanelProperty("Category.Subcategory.propId", layoutManager);
        FormField field = prop.generateFormField();

        assertInstanceOf(PanelField.class, field);
        assertEquals("", field.getFieldLabel().getText());
        assertEquals("Category.Subcategory.propId", field.getIdentifier());
        assertEquals(layoutManager, ((PanelField) field).getPanel().getLayout());
    }
}