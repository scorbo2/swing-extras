package ca.corbett.forms.fields;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.Action;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ButtonFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new ButtonField();
    }

    @Test
    public void getFieldComponent_shouldReturnJPanel() {
        ButtonField buttonField = new ButtonField();
        assertInstanceOf(JPanel.class, buttonField.getFieldComponent());
    }

    @Test
    public void addButton_withNullAction_shouldThrowException() {
        ButtonField buttonField = new ButtonField();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            buttonField.addButton(null);
        });
    }

    @Test
    public void shouldExpand_defaultShouldBeFalse() {
        ButtonField buttonField = new ButtonField();
        Assertions.assertFalse(buttonField.shouldExpand());
    }

    @Test
    public void addRemoveButton_shouldModifyButtonCount() {
        ButtonField buttonField = new ButtonField();
        int initialCount = buttonField.getButtonCount();

        buttonField.addButton(createTestAction("Test Button"));
        assertEquals(initialCount + 1, buttonField.getButtonCount());

        buttonField.removeButton("Test Button");
        assertEquals(initialCount, buttonField.getButtonCount());
    }

    @Test
    public void constructor_withActions_shouldCreateButtons() {
        Action action1 = createTestAction("Button 1");
        Action action2 = createTestAction("Button 2");

        ButtonField buttonField = new ButtonField(java.util.List.of(action1, action2));
        assertEquals(2, buttonField.getButtonCount());
    }

    @Test
    public void layoutDefaults_shouldBeCorrect() {
        ButtonField buttonField = new ButtonField();
        final int expectedHGap = ButtonField.DEFAULT_HGAP;
        final int expectedVGap = ButtonField.DEFAULT_VGAP;

        java.awt.LayoutManager layout = buttonField.getFieldComponent().getLayout();
        assertInstanceOf(FlowLayout.class, layout);
        FlowLayout flowLayout = (FlowLayout) layout;
        assertEquals(expectedHGap, flowLayout.getHgap());
        assertEquals(expectedVGap, flowLayout.getVgap());
        assertEquals(FlowLayout.LEFT, flowLayout.getAlignment());
    }

    @Test
    public void changeLayout_withNewProperties_shouldUpdateLayout() {
        ButtonField buttonField = new ButtonField();
        final int newHGap = 10;
        final int newVGap = 15;
        final int newAlignment = FlowLayout.CENTER;

        buttonField.setAlignment(newAlignment);
        buttonField.setHgap(newHGap);
        buttonField.setVgap(newVGap);

        LayoutManager layout = buttonField.getFieldComponent().getLayout();
        assertInstanceOf(FlowLayout.class, layout);
        FlowLayout flowLayout = (FlowLayout) layout;
        assertEquals(newHGap, flowLayout.getHgap());
        assertEquals(newVGap, flowLayout.getVgap());
        assertEquals(newAlignment, flowLayout.getAlignment());
    }

    private Action createTestAction(String actionName) {
        return new javax.swing.AbstractAction(actionName) {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // No-op for test
            }
        };
    }
}