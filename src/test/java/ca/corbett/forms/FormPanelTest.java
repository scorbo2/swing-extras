package ca.corbett.forms;

import ca.corbett.forms.fields.AlwaysFalseValidator;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class FormPanelTest {

    @Test
    public void testGetFormField_failureScenarios() {
        FormPanel formPanel = new FormPanel();
        assertNull(formPanel.getFormField("hello"));

        ShortTextField textField = new ShortTextField("Text:", 12);
        textField.setIdentifier("textField1");
        formPanel.add(textField);

        assertNull(formPanel.getFormField("TEXTFIELD1"));
        assertNull(formPanel.getFormField("textField11"));
        assertNull(formPanel.getFormField("textField"));
    }

    @Test
    public void testGetFormField_successScenarios() {
        NumberField numberField = new NumberField("Number:", 1, 0, 2, 1);
        numberField.setIdentifier("numberField1");
        FormPanel formPanel = new FormPanel();
        formPanel.add(List.of(numberField));
        assertNotNull(formPanel.getFormField("numberField1"));

        ShortTextField textField = new ShortTextField("Text:", 12);
        textField.setIdentifier("textField1");
        formPanel.add(textField);
        assertNotNull(formPanel.getFormField("textField1"));

        formPanel.removeAllFormFields();
        assertNull(formPanel.getFormField("numberField1"));
        assertNull(formPanel.getFormField("textField1"));
    }

    @Test
    public void testGetFieldCount() {
        FormPanel formPanel = new FormPanel();
        assertEquals(0, formPanel.getFieldCount());
        formPanel.add(new PanelField());
        assertEquals(1, formPanel.getFieldCount());
        formPanel.add(new PanelField());
        assertEquals(2, formPanel.getFieldCount());
        formPanel.removeAllFormFields();
        assertEquals(0, formPanel.getFieldCount());
    }

    @Test
    public void testClearValidationResults() {
        FormPanel formPanel = new FormPanel();
        PanelField field1 = new PanelField();
        field1.addFieldValidator(new AlwaysFalseValidator());
        PanelField field2 = new PanelField();
        field2.addFieldValidator(new AlwaysFalseValidator());
        formPanel.add(field1);
        formPanel.add(field2);
        formPanel.add(new PanelField().addFieldValidator(new AlwaysFalseValidator()));
        // With help labels always rendered, validation labels are now at different indices
        assertInstanceOf(JLabel.class, formPanel.getComponent(4));
        assertInstanceOf(JLabel.class, formPanel.getComponent(9));
        JLabel validationLabel1 = (JLabel)formPanel.getComponent(4);
        JLabel validationLabel2 = (JLabel)formPanel.getComponent(9);
        assertSame(field1.getValidationLabel(), validationLabel1);
        assertSame(field2.getValidationLabel(), validationLabel2);

        formPanel.validateForm();
        assertNotNull(validationLabel1.getIcon());
        assertNotNull(validationLabel2.getIcon());
        assertEquals(AlwaysFalseValidator.MESSAGE, validationLabel1.getToolTipText());
        assertEquals(AlwaysFalseValidator.MESSAGE, validationLabel2.getToolTipText());
        assertFalse(field1.isValid());
        assertFalse(field2.isValid());

        formPanel.clearValidationResults();
        assertNull(validationLabel1.getIcon());
        assertNull(validationLabel2.getIcon());
        assertNull(validationLabel1.getToolTipText());
        assertNull(validationLabel2.getToolTipText());
    }

    @Test
    public void testSetBorderMargin_leftMargin() {
        final int MARGIN = 11;
        for (Alignment alignment : List.of(Alignment.CENTER_LEFT, Alignment.TOP_LEFT, Alignment.BOTTOM_LEFT)) {
            FormPanel formPanel = new FormPanel(alignment);
            formPanel.setBorderMargin(MARGIN);
            assertEquals(MARGIN, formPanel.getBorderMargin().getLeft());
            PanelField field = new PanelField();
            field.setMargins(new Margins(MARGIN));
            formPanel.add(field);
            assertExpectedComponentCount_borderMarginTest(formPanel);
            GridBagConstraints gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getFieldComponent());
            assertEquals(MARGIN * 2, gbc.insets.left); // form's border margin + field's left margin
        }
    }

    @Test
    public void testSetBorderMargin_rightMargin() {
        final int MARGIN = 11;
        for (Alignment alignment : List.of(Alignment.CENTER_RIGHT, Alignment.TOP_RIGHT, Alignment.BOTTOM_RIGHT)) {
            FormPanel formPanel = new FormPanel(alignment);
            formPanel.setBorderMargin(MARGIN);
            assertEquals(MARGIN, formPanel.getBorderMargin().getRight());
            PanelField field = new PanelField();
            field.setMargins(new Margins(MARGIN));
            formPanel.add(field);
            assertExpectedComponentCount_borderMarginTest(formPanel);
            GridBagConstraints gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getValidationLabel());
            assertEquals(MARGIN * 2, gbc.insets.right); // form's border margin + field's right margin
        }
    }

    @Test
    public void testSetBorderMargin_topMargin() {
        final int MARGIN = 11;
        for (Alignment alignment : List.of(Alignment.TOP_CENTER, Alignment.TOP_RIGHT, Alignment.TOP_LEFT)) {
            FormPanel formPanel = new FormPanel(alignment);
            formPanel.setBorderMargin(MARGIN);
            assertEquals(MARGIN, formPanel.getBorderMargin().getTop());
            PanelField field = new PanelField();
            field.setMargins(new Margins(MARGIN));
            formPanel.add(field);
            assertExpectedComponentCount_borderMarginTest(formPanel);
            GridBagConstraints gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getFieldComponent());
            assertEquals(MARGIN * 2, gbc.insets.top); // form's border margin + field's top margin
            gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getValidationLabel());
            assertEquals(MARGIN * 2, gbc.insets.top); // form's border margin + field's top margin
        }
    }

    @Test
    public void testSetBorderMargin_bottomMargin() {
        final int MARGIN = 11;
        for (Alignment alignment : List.of(Alignment.BOTTOM_LEFT, Alignment.BOTTOM_RIGHT, Alignment.BOTTOM_CENTER)) {
            FormPanel formPanel = new FormPanel(alignment);
            formPanel.setBorderMargin(MARGIN);
            assertEquals(MARGIN, formPanel.getBorderMargin().getBottom());
            PanelField field = new PanelField();
            field.setMargins(new Margins(MARGIN));
            formPanel.add(field);
            assertExpectedComponentCount_borderMarginTest(formPanel);
            GridBagConstraints gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getFieldComponent());
            assertEquals(MARGIN * 2, gbc.insets.bottom); // form's border margin + field's bottom margin
            gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getValidationLabel());
            assertEquals(MARGIN * 2, gbc.insets.bottom); // form's border margin + field's bottom margin
        }
    }

    @Test
    public void testSetBorderMargin_center_shouldStillSetMargins() {
        final int MARGIN = 11;
        FormPanel formPanel = new FormPanel(Alignment.CENTER);
        formPanel.setBorderMargin(MARGIN); // should be ignored for CENTER alignment
        assertEquals(MARGIN, formPanel.getBorderMargin().getLeft());
        assertEquals(MARGIN, formPanel.getBorderMargin().getTop());
        PanelField field = new PanelField();
        field.setMargins(new Margins(MARGIN));
        formPanel.add(field);
        assertExpectedComponentCount_borderMarginTest(formPanel);
        GridBagConstraints gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getFieldComponent());
        assertEquals(MARGIN * 2, gbc.insets.left);
        assertEquals(MARGIN * 2, gbc.insets.top);
        assertEquals(MARGIN * 2, gbc.insets.bottom);
        gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getValidationLabel());
        assertEquals(MARGIN * 2, gbc.insets.bottom);
        assertEquals(MARGIN * 2, gbc.insets.right);
        assertEquals(MARGIN * 2, gbc.insets.top);
    }

    @Test
    public void setMultiLineFieldExtraTopMargin_withExtraMargin_shouldApply() {
        // GIVEN a multi-line FormField on a FormPanel with an extra top margin set for multi-line components:
        final int FIELD_MARGIN = 5; // some non-default margin value to give to the FormField itself
        final int EXTRA_MARGIN = 7; // An extra margin to be supplied via the FormPanel
        FormPanel formPanel = new FormPanel();
        PanelField field = new PanelField();
        field.getFieldLabel().setText("This will force the field margin to be visible");
        field.getMargins().setTop(FIELD_MARGIN);
        formPanel.setMultiLineFieldExtraTopMargin(EXTRA_MARGIN);
        formPanel.add(field);

        // WHEN we look at the margins that were applied to the multi-line field's field label:
        GridBagConstraints gbc = ((GridBagLayout)formPanel.getLayout()).getConstraints(field.getFieldLabel());

        // THEN we should see it was added to the field's own margin value:
        assertEquals(FIELD_MARGIN + EXTRA_MARGIN, gbc.insets.top);
    }

    private void assertExpectedComponentCount_borderMarginTest(FormPanel formPanel) {
        // There should be a margin label on each edge: top, right, bottom, left
        // additionally, we should see our empty panel
        // also, the help label (always rendered now)
        // finally, we should see the validation label
        assertEquals(7, formPanel.getComponentCount());
    }

    @Test
    public void testDynamicHelpLabelToggling() {
        // GIVEN a FormPanel with a field that initially has no help text:
        FormPanel formPanel = new FormPanel();
        ShortTextField field = new ShortTextField("Test Field:", 12);
        formPanel.add(field);

        // THEN the help label should be rendered but not visible:
        assertNotNull(field.getHelpLabel());
        assertFalse(field.getHelpLabel().isVisible());
        assertFalse(field.hasHelpLabel());

        // WHEN we set help text on the field after the form is rendered:
        field.setHelpText("This is helpful information");

        // THEN the help label should now be visible:
        assertEquals("This is helpful information", field.getHelpText());
        assertEquals("This is helpful information", field.getHelpLabel().getToolTipText());
        assertEquals(true, field.getHelpLabel().isVisible());
        assertEquals(true, field.hasHelpLabel());

        // WHEN we clear the help text:
        field.setHelpText(null);

        // THEN the help label should be hidden again:
        assertFalse(field.getHelpLabel().isVisible());
        assertFalse(field.hasHelpLabel());

        // WHEN we set help text again with a blank string:
        field.setHelpText("");

        // THEN the help label should still be hidden:
        assertFalse(field.getHelpLabel().isVisible());
        assertFalse(field.hasHelpLabel());

        // WHEN we set help text with whitespace only:
        field.setHelpText("   ");

        // THEN the help label should still be hidden:
        assertFalse(field.getHelpLabel().isVisible());
        assertFalse(field.hasHelpLabel());

        // WHEN we set non-blank help text again:
        field.setHelpText("New help text");

        // THEN the help label should be visible again:
        assertEquals(true, field.getHelpLabel().isVisible());
        assertEquals(true, field.hasHelpLabel());
    }

    @Test
    public void testHelpLabelAlwaysRendered() {
        // GIVEN a FormPanel with fields with and without help text:
        FormPanel formPanel = new FormPanel();
        ShortTextField fieldWithHelp = new ShortTextField("Field 1:", 12);
        fieldWithHelp.setHelpText("I have help");
        ShortTextField fieldWithoutHelp = new ShortTextField("Field 2:", 12);
        formPanel.add(fieldWithHelp);
        formPanel.add(fieldWithoutHelp);

        // THEN both fields should have their help labels in the FormPanel:
        // Component layout: [top margin], [left margin], field1 label, field1 control, field1 help, field1 validation, [right margin],
        //                   [left margin], field2 label, field2 control, field2 help, field2 validation, [right margin], [bottom margin]
        // So we expect: 1 (top) + 6 (field1) + 6 (field2) + 1 (bottom) = 14 components
        assertEquals(14, formPanel.getComponentCount());

        // The help labels should be rendered at the correct grid positions:
        GridBagLayout layout = (GridBagLayout)formPanel.getLayout();
        
        // Field 1 help label should be at row 1, column HELP_COLUMN:
        GridBagConstraints gbc1 = layout.getConstraints(fieldWithHelp.getHelpLabel());
        assertEquals(FormPanel.HELP_COLUMN, gbc1.gridx);
        assertEquals(1, gbc1.gridy);
        
        // Field 2 help label should be at row 2, column HELP_COLUMN:
        GridBagConstraints gbc2 = layout.getConstraints(fieldWithoutHelp.getHelpLabel());
        assertEquals(FormPanel.HELP_COLUMN, gbc2.gridx);
        assertEquals(2, gbc2.gridy);

        // Field 1 help label should be visible, field 2 should not be:
        assertEquals(true, fieldWithHelp.getHelpLabel().isVisible());
        assertFalse(fieldWithoutHelp.getHelpLabel().isVisible());
    }
}
