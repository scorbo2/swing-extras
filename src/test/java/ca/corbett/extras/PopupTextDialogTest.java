package ca.corbett.extras;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PopupTextDialogTest {

    @Test
    public void cancelButton_inEditableMode_shouldBeVisible() {
        // GIVEN a dialog in editable mode (default):
        PopupTextDialog dialog = new PopupTextDialog(null, "Test", "Some text", false);

        // THEN the cancel button should be visible:
        assertTrue(dialog.getCancelButton().isVisible());
    }

    @Test
    public void cancelButton_inReadOnlyMode_shouldBeHidden() {
        // GIVEN a dialog in read-only mode:
        PopupTextDialog dialog = new PopupTextDialog(null, "Test", "Some text", false);
        dialog.setReadOnly(true);

        // THEN the cancel button should be hidden:
        assertFalse(dialog.getCancelButton().isVisible());
    }

    @Test
    public void cancelButton_switchingToEditableMode_shouldBeVisible() {
        // GIVEN a dialog that starts in read-only mode:
        PopupTextDialog dialog = new PopupTextDialog(null, "Test", "Some text", false);
        dialog.setReadOnly(true);

        // WHEN we switch back to editable mode:
        dialog.setReadOnly(false);

        // THEN the cancel button should be visible again:
        assertTrue(dialog.getCancelButton().isVisible());
    }

    @Test
    public void textArea_inReadOnlyMode_shouldNotBeEditable() {
        // GIVEN a dialog in read-only mode:
        PopupTextDialog dialog = new PopupTextDialog(null, "Test", "Some text", false);
        dialog.setReadOnly(true);

        // THEN the text area should not be editable:
        assertFalse(dialog.getTextArea().isEditable());
        assertTrue(dialog.isReadOnly());
    }

    @Test
    public void textArea_inEditableMode_shouldBeEditable() {
        // GIVEN a dialog in editable mode:
        PopupTextDialog dialog = new PopupTextDialog(null, "Test", "Some text", false);

        // THEN the text area should be editable:
        assertTrue(dialog.getTextArea().isEditable());
        assertFalse(dialog.isReadOnly());
    }
}
