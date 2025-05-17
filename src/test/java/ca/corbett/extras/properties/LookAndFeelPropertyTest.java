package ca.corbett.extras.properties;

import ca.corbett.extras.LookAndFeelManager;
import com.formdev.flatlaf.FlatDarkLaf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LookAndFeelPropertyTest {

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
}