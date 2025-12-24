package ca.corbett.forms.fields;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Represents a FormField that can contain one or more buttons with configurable actions.
 * Internally, the buttons are grouped into a JPanel, which is set as this FormField's fieldComponent.
 * The layout defaults to a FlowLayout. The alignment, hgap, and vgap of the layout can be
 * modified with setAlignment, setHgap, and setVgap methods. Technically, callers can also
 * directly access or even swap out the LayoutManager by talking to the fieldComponent directly,
 * but if you need a highly customized layout, you might be better off using PanelField
 * or CollapsiblePanelField. That way, you have maximum control of the layout of your components.
 * <p>
 *     <b>Validation</b>: by default, ButtonFields do not show a validation label when
 *     the form is validated. But if you add a FieldValidator to your ButtonField,
 *     then the validation label will appear when the form is validated.
 * </p>
 * <p>
 *     <b>Field label</b>: by default, ButtonFields do not show a field label.
 *     You can manually enable a label by calling buttonField.getFieldLabel().setText("My Label").
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class ButtonField extends FormField {

    public static final int DEFAULT_ALIGNMENT = FlowLayout.LEFT;
    public static final int DEFAULT_HGAP = 4;
    public static final int DEFAULT_VGAP = 4;

    private final JPanel buttonPanel;
    private final FlowLayout layout;
    private Dimension preferredSize = null;
    private boolean shouldExpand = false;

    /**
     * Creates an empty ButtonField with no buttons and with a default layout.
     */
    public ButtonField() {
        this(List.of());
    }

    /**
     * Creates a ButtonField with the specified actions, each action corresponding to a button.
     * The label for each button is taken from the Action's name property.
     *
     * @param actions the list of actions for the buttons
     */
    public ButtonField(List<Action> actions) {
        layout = new FlowLayout(DEFAULT_ALIGNMENT, DEFAULT_HGAP, DEFAULT_VGAP);
        buttonPanel = new JPanel(layout);
        fieldComponent = buttonPanel;
        for (Action action : actions) {
            addButton(action);
        }
    }

    /**
     * Reports whether this ButtonField should expand to fill available horizontal space.
     */
    @Override
    public boolean shouldExpand() {
        return shouldExpand;
    }

    /**
     * Optionally make this field expand to fill the entire width of the parent
     * FormPanel. Defaults to false.
     */
    public ButtonField setShouldExpand(boolean expand) {
        shouldExpand = expand;
        return this;
    }

    /**
     * Returns the count of buttons currently in this ButtonField.
     */
    public int getButtonCount() {
        // Note: we can't just blindly return getComponentCount(), because our
        // buttonPanel is exposed to callers, and they may have added other components to it.
        // So, we have to iterate and look specifically for JButton instances.
        int buttonCount = 0;
        for (int i = 0; i < buttonPanel.getComponentCount(); i++) {
            Component component = buttonPanel.getComponent(i);
            if (component instanceof JButton) {
                buttonCount++;
            }
        }
        return buttonCount;
    }

    /**
     * One of FlowLayout's alignment options: LEFT, CENTER, or RIGHT from the FlowLayout class.
     */
    public int getAlignment() {
        return layout.getAlignment();
    }

    /**
     * One of FlowLayout's alignment options: LEFT, CENTER, or RIGHT from the FlowLayout class.
     */
    public ButtonField setAlignment(int alignment) {
        layout.setAlignment(alignment);
        return this;
    }

    /**
     * Gets the horizontal gap between buttons.
     */
    public int getHgap() {
        return layout.getHgap();
    }

    /**
     * Gets the vertical gap between button rows.
     */
    public int getVgap() {
        return layout.getVgap();
    }

    /**
     * Sets the horizontal gap between buttons, and between buttons and the edge of the containing panel.
     */
    public ButtonField setHgap(int hgap) {
        layout.setHgap(hgap);
        return this;
    }

    /**
     * Sets the vertical gap between buttons, and between buttons and the edge of the containing panel.
     */
    public ButtonField setVgap(int vgap) {
        layout.setVgap(vgap);
        return this;
    }

    /**
     * Gets the preferred size of the buttons in this ButtonField, or null if the buttons
     * should choose their own preferred size.
     */
    public Dimension getButtonPreferredSize() {
        return preferredSize;
    }

    /**
     * Sets the preferred size of the buttons in this ButtonField.
     * If set to null, the buttons will choose their own preferred size.
     */
    public ButtonField setButtonPreferredSize(Dimension preferredSize) {
        // Store this size for future buttons:
        this.preferredSize = preferredSize;

        // Update all existing buttons with the new size:
        boolean buttonSizeChanged = false;
        for (int i = 0; i < buttonPanel.getComponentCount(); i++) {
            if (buttonPanel.getComponent(i) instanceof JButton) {
                JButton button = (JButton) buttonPanel.getComponent(i);
                button.setPreferredSize(preferredSize);
                buttonSizeChanged = true;
            }
        }

        // May require a layout update:
        if (buttonSizeChanged) {
            buttonPanel.revalidate();
            buttonPanel.repaint();
        }

        return this;
    }

    /**
     * Adds a button with the specified Action to this ButtonField.
     * The action name should be unique!
     */
    public ButtonField addButton(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        JButton button = new JButton(action);
        if (preferredSize != null) {
            button.setPreferredSize(preferredSize);
        }
        buttonPanel.add(button);
        return this;
    }

    /**
     * Removes the button with the specified action name from this ButtonField,
     * if such a button exists.
     */
    public ButtonField removeButton(String actionName) {
        for (int i = 0; i < buttonPanel.getComponentCount(); i++) {
            if (buttonPanel.getComponent(i) instanceof JButton) {
                JButton button = (JButton) buttonPanel.getComponent(i);
                if (button.getAction() == null) {
                    continue;
                }
                if (actionName.equals(button.getAction().getValue(Action.NAME))) {
                    buttonPanel.remove(i);
                    break;
                }
            }
        }
        return this;
    }

    /**
     * Overridden here so we can enable/disable our buttons as needed.
     */
    @Override
    public FormField setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);

        for (int i = 0; i < buttonPanel.getComponentCount(); i++) {
            // The buttonPanel is exposed to callers, so it's possible they added
            // other stuff to it that isn't a button. Just in case, we enable/disable
            // all components it contains:
            Component component = buttonPanel.getComponent(i);
            component.setEnabled(isEnabled);
        }

        return this;
    }

    /**
     * Overridden here as we generally don't want to show a validation label on a button field.
     * Will return true only if one or more FieldValidators have been explicitly assigned.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }
}
