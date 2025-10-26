package ca.corbett.forms.fields;

class ImageListFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ImageListField("Test:", 1, 75);
    }
}