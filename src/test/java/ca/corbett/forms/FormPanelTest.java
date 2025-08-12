package ca.corbett.forms;

import ca.corbett.forms.fields.AlwaysFalseValidator;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.TextField;
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

        TextField textField = TextField.ofSingleLine("Text:", 12);
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

        TextField textField = TextField.ofSingleLine("Text:", 12);
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
        assertInstanceOf(JLabel.class, formPanel.getComponent(3));
        assertInstanceOf(JLabel.class, formPanel.getComponent(7));
        JLabel validationLabel1 = (JLabel)formPanel.getComponent(3);
        JLabel validationLabel2 = (JLabel)formPanel.getComponent(7);
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
            assertEquals(MARGIN, formPanel.getBorderMargin());
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
            assertEquals(MARGIN, formPanel.getBorderMargin());
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
            assertEquals(MARGIN, formPanel.getBorderMargin());
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
            assertEquals(MARGIN, formPanel.getBorderMargin());
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
        assertEquals(MARGIN, formPanel.getBorderMargin());
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

    private void assertExpectedComponentCount_borderMarginTest(FormPanel formPanel) {
        // There should be a margin label on each edge: top, right, bottom, left
        // additionally, we should see our empty panel
        // finally, we should see the validation label
        assertEquals(6, formPanel.getComponentCount());
    }

}