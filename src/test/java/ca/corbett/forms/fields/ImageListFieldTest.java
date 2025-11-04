package ca.corbett.forms.fields;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.image.BufferedImage;

class ImageListFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ImageListField("Test:", 1, 75);
    }

    @Test
    public void addValueChangedListener() {
        ImageListField actualField = (ImageListField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actualField.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(actual);
    }

    @Test
    public void removeValueChangedListener() {
        ImageListField actualField = (ImageListField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actual.addValueChangedListener(listener);
        actual.removeValueChangedListener(listener);
        actualField.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        Mockito.verify(listener, Mockito.times(0)).formFieldValueChanged(actual);
    }

    @Test
    public void addValueChangedListener_withBatchRemoval_shouldOnlyNotifyOnce() {
        // GIVEN an ImageListField with a bunch of images and a value change listener:
        ImageListField actualField = (ImageListField)actual;
        ValueChangedListener listener = Mockito.mock(ValueChangedListener.class);
        actualField.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        actualField.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        actualField.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        actualField.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        actual.addValueChangedListener(listener);

        // WHEN we do some batch operation (like remove several images):
        actualField.setMaxImageCount(1); // will remove all images after the first one

        // THEN we should see that only one notification was sent out (not one per each removal):
        Mockito.verify(listener, Mockito.times(1)).formFieldValueChanged(actual);
    }
}