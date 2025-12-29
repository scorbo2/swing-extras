package ca.corbett.forms.fields;

import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlLabelFieldTest extends FormFieldBaseTests {

    @Override
    protected FormField createTestObject() {
        return new HtmlLabelField("<html>Test label with no links.</html>", null);
    }

    @Test
    public void getFieldComponent() {
        assertNotNull(actual.getFieldComponent());
        assertInstanceOf(JEditorPane.class, actual.fieldComponent);
    }

    @Test
    public void addFieldValidator_withValidData_shouldValidate() {
        HtmlLabelField htmlLabelField = new HtmlLabelField(TestHtmlLabelFieldValidator.requiredContent, null);
        htmlLabelField.addFieldValidator(new TestHtmlLabelFieldValidator());
        assertTrue(htmlLabelField.isValid());
        assertNull(htmlLabelField.getValidationLabel().getToolTipText());
    }

    @Test
    public void addFieldValidator_withInvalidData_shouldInvalidate() {
        HtmlLabelField htmlLabelField = new HtmlLabelField("The magic text ain't here", null);
        htmlLabelField.addFieldValidator(new TestHtmlLabelFieldValidator());
        assertFalse(htmlLabelField.isValid());
        assertEquals(TestHtmlLabelFieldValidator.MESSAGE, htmlLabelField.getValidationLabel().getToolTipText());
    }

    @Test
    public void isValid_withNoValidator_shouldAlwaysBeValid() {
        HtmlLabelField htmlLabelField = new HtmlLabelField("Any text", null);
        assertTrue(htmlLabelField.isValid());
    }

    @Test
    public void testMultipleHyperlinks() {
        // Track which action commands were received
        List<String> receivedCommands = new ArrayList<>();

        Action testAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receivedCommands.add(e.getActionCommand());
            }
        };

        String html = "<html>Would you like to <a href='proceedAction'>proceed</a> or <a href='cancelAction'>cancel</a>?</html>";
        HtmlLabelField field = new HtmlLabelField(html, testAction);

        // Get the hyperlink listeners
        JEditorPane pane = (JEditorPane) field.getFieldComponent();
        HyperlinkListener[] listeners = pane.getHyperlinkListeners();
        assertEquals(1, listeners.length);

        HyperlinkListener listener = listeners[0];

        // Simulate clicking the "proceed" link
        HyperlinkEvent proceedEvent = createHyperlinkEvent(pane, "proceedAction");
        listener.hyperlinkUpdate(proceedEvent);

        // Simulate clicking the "cancel" link
        HyperlinkEvent cancelEvent = createHyperlinkEvent(pane, "cancelAction");
        listener.hyperlinkUpdate(cancelEvent);

        // Verify both actions were invoked with correct commands
        assertEquals(2, receivedCommands.size());
        assertEquals("proceedAction", receivedCommands.get(0));
        assertEquals("cancelAction", receivedCommands.get(1));
    }

    @Test
    public void testNonActivatedEventsIgnored() {
        List<String> receivedCommands = new ArrayList<>();

        Action testAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receivedCommands.add(e.getActionCommand());
            }
        };

        String html = "<html><a href='testAction'>test</a></html>";
        HtmlLabelField field = new HtmlLabelField(html, testAction);
        JEditorPane pane = (JEditorPane) field.getFieldComponent();

        HyperlinkListener listener = pane.getHyperlinkListeners()[0];

        // Simulate ENTERED event (hovering over link) - should not trigger action
        HyperlinkEvent enteredEvent = new HyperlinkEvent(
                pane,
                HyperlinkEvent.EventType.ENTERED,
                null,
                "testAction"
        );
        listener.hyperlinkUpdate(enteredEvent);

        // Verify action was NOT invoked
        assertEquals(0, receivedCommands.size());
    }

    @Test
    public void setText_shouldReplaceExistingTextAndAction() throws Exception {
        List<String> receivedCommands = new ArrayList<>();

        Action firstAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receivedCommands.add("first:" + e.getActionCommand());
            }
        };

        String firstHtml = "<html><a href='firstLink'>First Link</a></html>";
        HtmlLabelField field = new HtmlLabelField(firstHtml, firstAction);
        JEditorPane pane = (JEditorPane)field.getFieldComponent();
        HyperlinkListener firstListener = pane.getHyperlinkListeners()[0];

        // Simulate clicking the first link
        HyperlinkEvent firstEvent = createHyperlinkEvent(pane, "firstLink");
        firstListener.hyperlinkUpdate(firstEvent);

        // Now set new text and action
        Action secondAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receivedCommands.add("second:" + e.getActionCommand());
            }
        };

        String secondHtml = "<html><a href='secondLink'>Second Link</a></html>";
        field.setText(secondHtml, secondAction);

        // Get the new listener
        HyperlinkListener secondListener = pane.getHyperlinkListeners()[0];

        // Simulate clicking the second link
        HyperlinkEvent secondEvent = createHyperlinkEvent(pane, "secondLink");
        secondListener.hyperlinkUpdate(secondEvent);

        // Verify both actions were invoked correctly
        assertEquals(2, receivedCommands.size());
        assertEquals("first:firstLink", receivedCommands.get(0));
        assertEquals("second:secondLink", receivedCommands.get(1));
    }

    @Test
    public void setText_withNullAction_shouldDoNothingWhenLinksClicked() throws Exception {
        HtmlLabelField field = new HtmlLabelField("<html>Initial Text</html>", null);

        // When the supplied action is null, there should be no hyperlink listeners registered:
        JEditorPane pane = (JEditorPane)field.getFieldComponent();
        HyperlinkListener[] listeners = pane.getHyperlinkListeners();
        assertEquals(0, listeners.length);
    }

    /**
     * Helper method to create a HyperlinkEvent for testing
     */
    private HyperlinkEvent createHyperlinkEvent(JEditorPane pane, String href) {
        return new HyperlinkEvent(
                pane,
                HyperlinkEvent.EventType.ACTIVATED,
                null,  // URL (can be null for our purposes)
                href   // description (this is what getDescription() returns)
        );
    }

    private static class TestHtmlLabelFieldValidator implements FieldValidator<HtmlLabelField> {
        public static final String requiredContent = "floobydooby";
        public static final String MESSAGE = "Text must contain: " + requiredContent;

        @Override
        public ValidationResult validate(HtmlLabelField field) {
            return field.getText().contains(requiredContent)
                    ? ValidationResult.valid()
                    : ValidationResult.invalid(MESSAGE);
        }
    }
}