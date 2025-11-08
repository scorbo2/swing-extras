package ca.corbett.extras.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordPropertyTest extends AbstractPropertyBaseTests {

    private static final String fullyQualifiedName = "TestCategory.TestSubcategory.TestPropertyName";

    @Override
    protected AbstractProperty createTestObject(String fullyQualifiedName, String label) {
        return new PasswordProperty(fullyQualifiedName, label);
    }

    @Test
    public void passwordSave_whenDisabled_shouldNotSave() {
        // GIVEN a PasswordProperty configured to not save password to props:
        ((PasswordProperty)actual).setPasswordSavedToProps(false);
        ((PasswordProperty)actual).setPassword("hello");

        // WHEN we save it:
        Properties props = new Properties();
        actual.saveToProps(props);

        // THEN we should see that it did NOT write out the property value:
        assertEquals("", props.getString(fullyQualifiedName + ".value", "hello"));
    }

    @Test
    public void passwordSave_whenEnabled_shouldSave() {
        // GIVEN a PasswordProperty configured to save its password to props:
        ((PasswordProperty)actual).setPasswordSavedToProps(true);
        ((PasswordProperty)actual).setPassword("hello");

        // WHEN we save it:
        Properties props = new Properties();
        actual.saveToProps(props);

        // THEN we should see that it wrote out the password:
        assertEquals("hello", props.getString(fullyQualifiedName + ".value", ""));
    }
}