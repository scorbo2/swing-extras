package ca.corbett.extras.properties;

import ca.corbett.forms.fields.HtmlLabelField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    public void testGenerateFormFieldImpl() {
        HtmlLabelProperty htmlLabelProperty = new HtmlLabelProperty("Category.Subcategory.Property", "Label", "<html>Test HTML</html>", null);
        assertNotNull(htmlLabelProperty.generateFormFieldImpl());
        assertInstanceOf(HtmlLabelField.class, htmlLabelProperty.generateFormField());
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

}