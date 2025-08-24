package ca.corbett.extras.properties;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.fields.ComboField;
import com.formdev.flatlaf.FlatDarkLaf;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LookAndFeelPropertyTest extends AbstractPropertyBaseTests {

    @Override
    protected AbstractProperty createTestObject(String name, String label) {
        return new LookAndFeelProperty(name, label);
    }

    @Test
    public void saveToProps_withAllDefaultValues_shouldSaveDefault() {
        // GIVEN a fully defaulted prop (with no extra LaFs loaded):
        LookAndFeelProperty prop = new LookAndFeelProperty("prop", "prop");
        String className = prop.getSelectedLafClass();

        // WHEN we save it:
        Properties props = new Properties();
        prop.saveToProps(props);

        // THEN we should see the same className when we load it:
        prop.loadFromProps(props);
        assertEquals(className, prop.getSelectedLafClass());
    }

    @Test
    public void saveToProps_withExtraLafs_shouldRestoreSelected() {
        // GIVEN a setup with extra LaFs loaded:
        LookAndFeelManager.installExtraLafs();
        LookAndFeelProperty prop = new LookAndFeelProperty("prop", "prop", FlatDarkLaf.class.getName());
        String className = prop.getSelectedLafClass();

        // WHEN we save it:
        Properties props = new Properties();
        prop.saveToProps(props);

        // THEN we should see the same className when we load it:
        prop.loadFromProps(props);
        assertEquals(className, prop.getSelectedLafClass());
    }

    @Test
    public void formFieldChangeListener_withFormFieldChanges_shouldFireChangeEvents() {
        // GIVEN a test prop with a mocked property form field change listener on it:
        LookAndFeelProperty testProp = new LookAndFeelProperty("test", "test");
        testProp.setSelectedIndex(0);
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