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
    private final boolean isInitiallyExpanded;
    private final JPanel headerPanel;
    private final JPanel buttonWrapperPanel;
    private final JButton expandCollapseButton;
    private boolean isExpanded;
    private boolean shouldExpandHorizontally;
    private ButtonPosition buttonPosition;

    /**
     * Creates a new PanelField with an empty wrapped JPanel.
     * Use getPanel() to retrieve the panel and add your custom
     * components to it.
     */
    public CollapsiblePanelField(String fieldLabel, boolean isInitiallyExpanded) {
        this(fieldLabel, isInitiallyExpanded, new FlowLayout());
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
        this.isInitiallyExpanded = isExpanded = isInitiallyExpanded;

        JPanel wrapperPanel = new JPanel(new BorderLayout());

        JPanel labelWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        labelWrapperPanel.add(label);

        buttonWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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


    @Override
    public boolean isMultiLine() {
        return true;
    }

    @Override
    public boolean shouldExpand() {
        return shouldExpandHorizontally;
    }
}
