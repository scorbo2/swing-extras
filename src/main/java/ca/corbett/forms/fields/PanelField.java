package ca.corbett.forms.fields;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
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
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2020-09-27
 */
public class PanelField extends FormField {

    private final JPanel panel;
    private boolean shouldExpand;
    private boolean isEnabledStatusPropagated;

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
        shouldExpand = false; // arbitrary default
        isEnabledStatusPropagated = false; // arbitrary default
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


    /**
     * Optionally make this FormField expand to fill the entire width of the parent
     * FormPanel. Defaults to false.
     */
    public PanelField setShouldExpand(boolean expand) {
        shouldExpand = expand;
        return this;
    }

    /**
     * Determines what happens when setEnabled is invoked. By default, Swing containers
     * do not propagate the new enabled status to the components that they contain. But this
     * might be unexpected compared to the behaviour of other FormField implementations.
     * So, set this to true if you want to the setEnabled method in this FormField to propagate
     * downwards recursively to all contained components. The default value is false.
     * <p>
     * If the "all or nothing" options don't suit your particular use case, (that is,
     * if you want setEnabled to apply to <i>some</i> of the contained components here,
     * but not all of them), then you should create a derived class, override the
     * setEnabled method, and implement your custom logic.
     * </p>
     */
    public PanelField setEnabledStatusIsPropagated(boolean isPropagated) {
        isEnabledStatusPropagated = isPropagated;
        return this;
    }

    /**
     * See setEnabledStatusIsPropagated for a description of this option.
     *
     * @return true if setEnabled should act on all contained components in this panel (default false).
     */
    public boolean isEnabledStatusPropagated() {
        return isEnabledStatusPropagated;
    }

    /**
     * Overridden here so we can optionally propagate the new enabled status to all
     * contained components, depending on isEnabledStatusPropagated. See
     * setEnabledStatusIsPropagated for a description of this option.
     */
    @Override
    public FormField setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);

        if (isEnabledStatusPropagated) {
            setEnabledRecursive(panel, isEnabled);
        }

        return this;
    }

    /**
     * Recurses through the list of contained components, passing on the given isEnabled
     * status to each of them (and their own contained children, if any of our contained
     * components are containers themselves).
     */
    protected void setEnabledRecursive(Container container, boolean isEnabled) {
        for (Component c : container.getComponents()) {
            c.setEnabled(isEnabled);

            // Not just the children, but the grandchildren, great-grandchildren, etc:
            if (c instanceof Container) {
                setEnabledRecursive((Container)c, isEnabled);
            }
        }
    }

    @Override
    public boolean isMultiLine() {
        return true;
    }

    @Override
    public boolean shouldExpand() {
        return shouldExpand;
    }
}
