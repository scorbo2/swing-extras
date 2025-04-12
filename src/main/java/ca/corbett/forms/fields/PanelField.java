package ca.corbett.forms.fields;

import ca.corbett.forms.FormPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
 * </p>
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
        fieldLabel = new JLabel();
        showValidationLabel = false;
        panel = new JPanel();
        fieldComponent = panel;
    }


    /**
     * Exposes the wrapped JPanel so that callers can add custom components to it.
     *
     * @return The wrapped JPanel, which is empty by default.
     */
    public JPanel getPanel() {
        return panel;
    }


    /**
     * Renders our wrapped JPanel into the given container. The panel will span the width
     * of the form and will respect any supplied margin parameters.
     *
     * @param container   The container into which we will render the wrapped JPanel.
     * @param constraints The constraints to use for rendering.
     */
    @Override
    public void render(JPanel container, GridBagConstraints constraints) {
        constraints.insets = new Insets(topMargin, leftMargin, bottomMargin, rightMargin);
        constraints.gridx = FormPanel.LABEL_COLUMN;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = constraints.gridy + 1;
        constraints.weightx = 0.05;
        container.add(fieldComponent, constraints);
        constraints.weightx = 0.0;
    }
}
