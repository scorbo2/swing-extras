package ca.corbett.forms.fields;

import ca.corbett.extras.image.ImagePanel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void setBackground_withNewColor_shouldPropagateToImageListPanel() {
        // GIVEN an ImageListField with an ImagePanel:
        ImageListField actualField = (ImageListField)actual;
        BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        actualField.addImage(dummyImage);
        Color newColor = Color.PINK;

        // WHEN we set a new background color:
        actualField.setBackground(newColor);

        // THEN the ImageListPanel inside it should also have the new background color:
        Color imageListPanelBg = actualField.getImageListPanel().getBackground();
        assertEquals(newColor, imageListPanelBg);
        assertEquals(newColor, actualField.getFieldComponent().getBackground()); // and the scroll pane too!

        // AND any child ImagePanel instances should also have the new background color:
        List<Color> foundColors = new ArrayList<>();
        for (int i = 0; i < actualField.getImageListPanel().getComponentCount(); i++) {
            if (actualField.getImageListPanel().getComponent(i) instanceof ImagePanel) {
                foundColors.add(actualField.getImageListPanel().getComponent(i).getBackground());
            }
        }
        assertEquals(1, foundColors.size(), "There should be exactly 1 ImagePanel.");
        assertEquals(newColor, foundColors.get(0), "The ImagePanel should have the new background color.");

        // WHEN we then add another image:
        actualField.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        // THEN the new ImagePanel should also have the expected color:
        foundColors = new ArrayList<>();
        for (int i = 0; i < actualField.getImageListPanel().getComponentCount(); i++) {
            if (actualField.getImageListPanel().getComponent(i) instanceof ImagePanel) {
                foundColors.add(actualField.getImageListPanel().getComponent(i).getBackground());
            }
        }
        assertEquals(2, foundColors.size(), "There should be exactly 2 ImagePanels.");
        for (Color color : foundColors) {
            assertEquals(newColor, color, "Each ImagePanel should have the new background color.");
        }
    }
}
