package ca.corbett.extras.properties;

import ca.corbett.forms.fields.CollapsiblePanelField;
import ca.corbett.forms.fields.FormField;
import org.junit.jupiter.api.Test;

import java.awt.FlowLayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollapsiblePanelPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new CollapsiblePanelProperty(fullyQualifiedName, label);
    }

    @Test
    public void setLayoutManager_withNoExplicitValue_shouldReturnDefault() {
        CollapsiblePanelProperty prop = new CollapsiblePanelProperty("propId", "Panel Label");

        // The property wrapper should have a null value since we didn't specify one:
        assertNull(prop.getLayoutManager());

        // But when we generate a form field, the CollapsiblePanelField supplies a FlowLayout if not overridden:
        FormField panelField = prop.generateFormField();
        assertInstanceOf(CollapsiblePanelField.class, panelField);
        CollapsiblePanelField collapsiblePanelField = (CollapsiblePanelField) panelField;
        assertInstanceOf(FlowLayout.class, collapsiblePanelField.getPanel().getLayout());
    }

    @Test
    public void testSaveToProps_and_LoadFromProps_shouldDoNothing() {
        CollapsiblePanelProperty prop = (CollapsiblePanelProperty)actual;
        Properties props = new Properties();

        // Just ensure no exceptions are thrown
        prop.saveToProps(props);
        prop.loadFromProps(props);

        // Should have added nothing to props:
        assertEquals(0, props.getPropertyNames().size());
    }

    @Test
    public void testGenerateFormFieldImpl_shouldCreateCollapsiblePanelField() {
        CollapsiblePanelProperty prop = new CollapsiblePanelProperty("Category.Subcategory.propId", "Panel Label");
        FormField field = prop.generateFormField();

        assertInstanceOf(CollapsiblePanelField.class, field);
        assertEquals("Panel Label", field.getFieldLabel().getText());
        assertEquals("Category.Subcategory.propId", field.getIdentifier());

        // Initial expanded state defaults to true unless set otherwise:
        CollapsiblePanelField collapsiblePanelField = (CollapsiblePanelField) field;
        assertTrue(collapsiblePanelField.isExpanded());
    }

    @Test
    public void testInitiallyExpanded_getterAndSetter_shouldWork() {
        CollapsiblePanelProperty prop = new CollapsiblePanelProperty("propId", "Panel Label");

        // Default is true
        assertTrue(prop.isInitiallyExpanded());

        // Set to false
        prop.setInitiallyExpanded(false);
        assertFalse(prop.isInitiallyExpanded());

        // Set back to true
        prop.setInitiallyExpanded(true);
        assertTrue(prop.isInitiallyExpanded());
    }

    @Test
    public void testGenerateFormFieldImpl_withInitiallyExpandedFalse_shouldCreateCollapsedPanel() {
        CollapsiblePanelProperty prop = new CollapsiblePanelProperty("propId", "Panel Label");
        prop.setInitiallyExpanded(false);

        FormField field = prop.generateFormField();
        assertInstanceOf(CollapsiblePanelField.class, field);

        CollapsiblePanelField collapsiblePanelField = (CollapsiblePanelField) field;
        assertFalse(collapsiblePanelField.isExpanded());
    }

    @Test
    public void testLayoutManager_getterAndSetter_shouldWork() {
        CollapsiblePanelProperty prop = new CollapsiblePanelProperty("propId", "Panel Label");
        assertNull(prop.getLayoutManager());

        java.awt.BorderLayout layout = new java.awt.BorderLayout();
        prop.setLayoutManager(layout);
        assertEquals(layout, prop.getLayoutManager());
    }
}