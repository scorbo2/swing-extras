package ca.corbett.extras.properties;

import ca.corbett.forms.fields.HtmlLabelField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlLabelPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new HtmlLabelProperty(fullyQualifiedName, label, "<html>Label with no links</html>", null);
    }

    @Test
    public void testGetHtmlAndLinkAction() {
        HtmlLabelProperty htmlLabelProperty = new HtmlLabelProperty("Category.Subcategory.Property", "Label", "<html>Test HTML</html>", null);
        assertEquals("<html>Test HTML</html>", htmlLabelProperty.getHtml());
        assertNull(htmlLabelProperty.getLinkAction());

        htmlLabelProperty.setHtml("<html>New HTML</html>", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Do nothing
            }
        });
        assertEquals("<html>New HTML</html>", htmlLabelProperty.getHtml());
        assertNotNull(htmlLabelProperty.getLinkAction());
    }

    @Test
    public void testGenerateFormField_shouldGenerateFormFieldOfCorrectType() {
        HtmlLabelProperty htmlLabelProperty = new HtmlLabelProperty("Category.Subcategory.Property", "Label", "<html>Test HTML</html>", null);
        assertNotNull(htmlLabelProperty.generateFormFieldImpl());
        assertInstanceOf(HtmlLabelField.class, htmlLabelProperty.generateFormField());
    }

    @Test
    public void generateFormField_withTextAndActionSet_shouldGenerateFieldWithTextAndAction() {
        javax.swing.AbstractAction testAction = new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Do nothing
            }
        };
        HtmlLabelProperty htmlLabelProperty = new HtmlLabelProperty("Category.Subcategory.Property", "Label",
                                                                    "<html>Test HTML</html>", testAction);
        HtmlLabelField field = (HtmlLabelField)htmlLabelProperty.generateFormField();

        // JEditorPane modifies the html that we send in, so we can't assert it exactly.
        // But, it should at least contain what we gave it, in pieces:
        assertTrue(field.getText().contains("<html>"));
        assertTrue(field.getText().contains("Test HTML"));
        assertTrue(field.getText().contains("</html>"));

        // Our action should be set correctly:
        assertEquals(testAction, field.getLinkAction());
    }

    @Test
    public void testSaveToProps_and_LoadFromProps_shouldDoNothing() {
        HtmlLabelProperty prop = (HtmlLabelProperty)actual;
        Properties props = new Properties();

        // Just ensure no exceptions are thrown
        prop.saveToProps(props);
        prop.loadFromProps(props);

        // Should have added nothing to props:
        assertEquals(0, props.getPropertyNames().size());
    }

    @Test
    public void testLoadFromFormField_shouldDoNothing() {
        HtmlLabelProperty prop = (HtmlLabelProperty)actual;
        HtmlLabelField field = new HtmlLabelField("<html>Some label</html>", null);

        // Just ensure no exceptions are thrown
        prop.loadFromFormField(field);
    }
}