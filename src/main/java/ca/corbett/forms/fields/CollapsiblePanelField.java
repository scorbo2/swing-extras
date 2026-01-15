package ca.corbett.forms.fields;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.forms.Resources;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

/**
 * Similar to PanelField, except this class gives you an "expand/contract" icon that
 * allows the panel contents to be displayed or hidden. This allows you to provide
 * additional controls or instructional labels on a FormPanel, with the ability
 * to partially hide them when not needed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.5
 */
public class CollapsiblePanelField extends FormField {

    private static boolean isDefaultBorderEnabled = true;

    public enum ButtonPosition { Left, Right; }

    private final JPanel expandPanel;
    private final JPanel headerPanel;
    private final JPanel buttonWrapperPanel;
    private final JButton expandCollapseButton;
    private boolean isExpanded;
    private boolean shouldExpandHorizontally;
    private ButtonPosition buttonPosition;
    private boolean isEnabledStatusPropagated;

    /**
     * Creates a new PanelField with an empty wrapped JPanel.
     * Use getPanel() to retrieve the panel and add your custom
     * components to it.
     */
    public CollapsiblePanelField(String labelText, boolean isInitiallyExpanded) {
        this(labelText, isInitiallyExpanded, new FlowLayout());
    }

    /**
     * Creates a new PanelField with an empty wrapped JPanel which has
     * the given LayoutManager. Use getPanel() to retrieve the panel and add
     * your custom components to it.
     */
    public CollapsiblePanelField(String labelText, boolean isInitiallyExpanded, LayoutManager layoutManager) {
        expandPanel = new JPanel();
        expandPanel.setLayout(layoutManager);
        shouldExpandHorizontally = false; // arbitrary default
        isExpanded = isInitiallyExpanded;
        isEnabledStatusPropagated = false; // arbitrary default

        JPanel wrapperPanel = new JPanel(new BorderLayout());

        JPanel labelWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        fieldLabel.setText(labelText); // this is a bit of a psych-out... see hasFieldLabel() override below
        labelWrapperPanel.add(label);

        buttonWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        expandCollapseButton = new JButton(isInitiallyExpanded ? Resources.getMinusIcon() : Resources.getPlusIcon());
        expandCollapseButton.addActionListener(e -> setIsExpanded(! isExpanded));
        expandCollapseButton.setBorder(null);
        expandCollapseButton.setOpaque(false);
        buttonWrapperPanel.add(expandCollapseButton);

        headerPanel = new JPanel(new BorderLayout());
        buttonPosition = ButtonPosition.Left; // arbitrary default
        headerPanel.add(labelWrapperPanel, BorderLayout.CENTER);
        headerPanel.add(buttonWrapperPanel, BorderLayout.WEST);

        wrapperPanel.add(headerPanel, BorderLayout.NORTH);
        wrapperPanel.add(expandPanel, BorderLayout.SOUTH);

        expandPanel.setVisible(isInitiallyExpanded);

        fieldComponent = wrapperPanel;
        if (isDefaultBorderEnabled) {
            setDefaultBorder();
        }
    }

    /**
     * Expands or collapses the wrapped JPanel.
     */
    public CollapsiblePanelField setIsExpanded(boolean expand) {
        if (isExpanded == expand) {
            return this; // ignore no-op requests
        }
        isExpanded = expand;
        expandCollapseButton.setIcon(isExpanded ? Resources.getMinusIcon() : Resources.getPlusIcon());
        expandPanel.setVisible(isExpanded);
        if (expandPanel.getParent() != null) {
            expandPanel.getParent().invalidate();
            expandPanel.getParent().validate();
            expandPanel.getParent().repaint();
        }
        return this;
    }

    /**
     * Reports whether the panel is currently expanded or collapsed.
     */
    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * You can set the expand/collapse button on the left side (default) or on the right side of the form field.
     */
    public CollapsiblePanelField setButtonPosition(ButtonPosition position) {
        if (position == null || position == buttonPosition) {
            return this; // no
        }
        buttonPosition = position;
        headerPanel.remove(buttonWrapperPanel);
        headerPanel.add(buttonWrapperPanel, buttonPosition == ButtonPosition.Left ? BorderLayout.WEST : BorderLayout.EAST);
        return this;
    }

    /**
     * Sets a default line border around the slider in this FormField. You can also invoke
     * setBorder to set some custom border.
     */
    public void setDefaultBorder() {
        setBorder(BorderFactory.createLineBorder(
                LookAndFeelManager.getLafColor("ColorPalette.separatorColor", Color.GRAY)));
    }

    /**
     * Sets the given border around the JSlider in this FormField. You can also invoke
     * setDefaultBorder() to easily set a simple line border.
     */
    public CollapsiblePanelField setBorder(Border border) {
        getFieldComponent().setBorder(border); // the wrapper panel, not the wrapped panel
        return this;
    }

    /**
     * We need to override and return false unconditionally here, otherwise the rendering of this
     * component within FormPanel will get wonky. The CollapsiblePanelField maintains its own
     * header with label and expand/collapse button, so we don't want FormPanel to add another label
     * beside it. So, we pretend we have no field label even though we sort of do.
     * Callers can still invoke getFieldLabel().getText() to retrieve our field label text,
     * but this method will pretend that we have no field label so we can handle our own rendering of it.
     */
    @Override
    public boolean hasFieldLabel() {
        return false;
    }

    /**
     * Reports whether a default border will be added to all new SliderField instances automatically.
     */
    public static boolean isIsDefaultBorderEnabled() {
        return isDefaultBorderEnabled;
    }

    /**
     * By default, all new instances of this class will give themselves a default LineBorder.
     * You can disable that behavior with this method. Note that this only affects new instance
     * creation from this point on - it will not change the border of any already-created instances.
     */
    public static void setIsDefaultBorderEnabled(boolean enable) {
        isDefaultBorderEnabled = enable;
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
        return expandPanel;
    }


    /**
     * Optionally make this FormField expand to fill the entire width of the parent
     * FormPanel. Defaults to false.
     */
    public CollapsiblePanelField setShouldExpandHorizontally(boolean expand) {
        shouldExpandHorizontally = expand;
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
    public CollapsiblePanelField setEnabledStatusIsPropagated(boolean isPropagated) {
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
            setEnabledRecursive(expandPanel, isEnabled);
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
        return shouldExpandHorizontally;
    }
}
