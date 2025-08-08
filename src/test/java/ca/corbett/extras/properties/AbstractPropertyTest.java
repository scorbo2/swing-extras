package ca.corbett.extras.properties;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AbstractProperty is an abstract class, so in order to test out some of its functionality,
 * we have to instantiate one of its implementing classes.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class AbstractPropertyTest {

    @Test
    public void testPropertyFieldNaming_withVariousInputs_shouldNameCorrectly() {
        BooleanProperty testField = new BooleanProperty("A.B.C", "");
        assertEquals("A", testField.getCategoryName());
        assertEquals("B", testField.getSubCategoryName());
        assertEquals("C", testField.getPropertyName());
        assertEquals("A.B.C", testField.getFullyQualifiedName());

        testField = new BooleanProperty("A.B", "");
        assertEquals("A", testField.getCategoryName());
        assertEquals(AbstractProperty.DEFAULT_CATEGORY, testField.getSubCategoryName());
        assertEquals("B", testField.getPropertyName());
        assertEquals("A.General.B", testField.getFullyQualifiedName());

        testField = new BooleanProperty("A", "");
        assertEquals(AbstractProperty.DEFAULT_CATEGORY, testField.getCategoryName());
        assertEquals(AbstractProperty.DEFAULT_CATEGORY, testField.getSubCategoryName());
        assertEquals("A", testField.getPropertyName());
        assertEquals("General.General.A", testField.getFullyQualifiedName());

        testField = new BooleanProperty("A.B.C.D", "");
        assertEquals("A", testField.getCategoryName());
        assertEquals("B", testField.getSubCategoryName());
        assertEquals("C.D", testField.getPropertyName());
        assertEquals("A.B.C.D", testField.getFullyQualifiedName());
    }

    @Test
    public void testPropertySorting_withVariousInputs_shouldSortCorrectly() {
        BooleanProperty testField1 = new BooleanProperty("A.B.C2", "");
        BooleanProperty testField2 = new BooleanProperty("A.B.C1", "");
        BooleanProperty testField3 = new BooleanProperty("D.E.F1", "");
        BooleanProperty testField4 = new BooleanProperty("G.A.F2", "");
        BooleanProperty testField5 = new BooleanProperty("0.0.0", "");
        BooleanProperty testField6 = new BooleanProperty("0", "");
        BooleanProperty testField7 = new BooleanProperty("A.0", "");
        BooleanProperty testField8 = new BooleanProperty("zzzz.last", "");
        BooleanProperty testField9 = new BooleanProperty("zzzz.first", "");
        List<AbstractProperty> list = new ArrayList<>();
        list.add(testField1);
        list.add(testField2);
        list.add(testField3);
        list.add(testField4);
        list.add(testField5);
        list.add(testField6);
        list.add(testField7);
        list.add(testField8);
        list.add(testField9);
        list.sort(new PropertyComparator());
        assertEquals(testField5, list.get(0));
        assertEquals(testField8, list.get(8));
    }

}
