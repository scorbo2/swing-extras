package ca.corbett.forms.fields;

import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

/**
 * A FormField that wraps and exposes a JPanel, into which callers can render
 * whatever extra custom components they want to display inline with the form.
 * The wrapped JPanel will span the width of the form.
 * Note that PanelFields have no field label, but you can certainly
 * add one yourself inside the panel, along with whatever else you need.
 * <p>
 * By default, PanelFields do not show the validation label when
 * the form is validated. But if you add a FieldValidator
 * to your PanelField, then the validation label will appear
 * when the form is validated.
 * <p>
 * The purpose of a PanelField is to allow a very easy way to create a custom
 * FormField without actually extending the FormField class yourself. You can
 * simply instantiate a PanelField and then render whatever you need. You can
 * of course also write a corresponding FieldValidator for your PanelField to
 * validate whatever value it contains, but at that point it might be easier
 * to just extend FormField and create a custom implementation.
 *
 * @author scorbo2
 * @since 2020-09-27
 */
public class PanelField extends FormField {

    private final JPanel panel;

    /**
     * Creates a new PanelField with an empty wrapped JPanel.
     * Use getPanel() to retrieve the panel and add your custom
     * components to it.
     */
    public PanelField() {
        this(new FlowLayout());
    }

    public PanelField(LayoutManager layoutManager) {
        panel = new JPanel();
        fieldComponent = panel;
        panel.setLayout(layoutManager);
    }

    /**
     * Overridden here as we generally don't want to show a validation label on a panel field.
     * Will return true only if one or more FieldValidators have been explicitly assigned.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }

    /**
     * Exposes the wrapped JPanel so that callers can add custom components to it.
     *
     * @return The wrapped JPanel, which is empty by default.
     */
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public boolean isMultiLine() {
        return true;
    }

    @Override
    public boolean shouldExpand() {
        return true;
    }
}
