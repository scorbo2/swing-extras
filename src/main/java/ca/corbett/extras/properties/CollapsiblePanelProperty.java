package ca.corbett.extras.properties;

import ca.corbett.forms.fields.CollapsiblePanelField;
import ca.corbett.forms.fields.FormField;

import java.awt.LayoutManager;

/**
 * This property wraps an empty CollapsiblePanelField and allows you to add
 * subfields dynamically to it when the property's FormField is created.
 * <p>
 *     <b>Example usage:</b>
 * </p>
 * <pre>
 * // Create a CollapsiblePanelProperty:
 * CollapsiblePanelProperty panelProp = new CollapsiblePanelProperty("propId", "Panel label", new BorderLayout());
 * panelProp.setInitiallyExpanded(false); // if you want it to start collapsed (default is expanded)
 *
 * // Register a FormFieldGenerationListener to populate it with something:
 * panelProp.addFormFieldGenerationListener((prop, formField) -> {
 *   CollapsiblePanelField panelField = (CollapsiblePanelField) formField;
 *   panelField.setShouldExpandHorizontally(true); // use the width of the parent form panel if you wish
 *   panelField.getPanel().setBorder(BorderFactory.createLoweredBevelBorder()); // override default border if you wish
 *   FormPanel subForm = new FormPanel();
 *   subForm.add(new LabelField("Additional help text goes here."));
 *   subForm.add(new LabelField("You can add whatever static fields you like."));
 *   subForm.add(new LabelField("Images, help text, whatever."));
 *   subForm.add(new LabelField("Just be aware that nothing here gets saved to properties."));
 *   panelField.getPanel().add(subForm, BorderLayout.CENTER);
 * });
 *
 * // Now add panelProp to your application properties with the rest of your properties.
 * // It will get rendered on the application properties dialog automatically!
 * </pre>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.6
 */
public class CollapsiblePanelProperty extends AbstractProperty {

    private LayoutManager layoutManager;
    private boolean isInitiallyExpanded = true;

    public CollapsiblePanelProperty(String id, String fieldLabel) {
        this(id, fieldLabel, null);
    }

    public CollapsiblePanelProperty(String id, String fieldLabel, LayoutManager layoutManager) {
        super(id, fieldLabel);
        this.layoutManager = layoutManager;

        // Most properties generate FormField instances that allow user input, but we do not:
        allowsUserInput = false;
    }

    public LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public CollapsiblePanelProperty setLayoutManager(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        return this;
    }

    public boolean isInitiallyExpanded() {
        return isInitiallyExpanded;
    }

    public CollapsiblePanelProperty setInitiallyExpanded(boolean initiallyExpanded) {
        isInitiallyExpanded = initiallyExpanded;
        return this;
    }

    @Override
    public void saveToProps(Properties props) {
        // Nothing to do here as we don't store any data here
    }

    @Override
    public void loadFromProps(Properties props) {
        // Nothing to do here as we don't store any data here
    }

    @Override
    protected FormField generateFormFieldImpl() {
        return layoutManager == null
            ? new CollapsiblePanelField(propertyLabel, isInitiallyExpanded)
            : new CollapsiblePanelField(propertyLabel, isInitiallyExpanded, layoutManager);
    }

    @Override
    public void loadFromFormField(FormField field) {
        // Nothing to do here as we don't store any data here
    }
}
