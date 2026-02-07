package ca.corbett.extras.properties;

import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.PanelField;

import java.awt.LayoutManager;

/**
 * This property wraps an empty PanelField and allows you to add
 * subfields dynamically to it when the property's FormField is created.
 * <p>
 *     <b>Example usage:</b>
 * </p>
 * <pre>
 * // Create a PanelProperty:
 * PanelProperty panelProp = new PanelProperty("propId", new BorderLayout());
 *
 * // Register a FormFieldGenerationListener to populate it with something:
 * panelProp.addFormFieldGenerationListener((prop, formField) -> {
 *   PanelField panelField = (PanelField) formField;
 *   panelField.setShouldExpand(true); // use the width of the parent form panel if you wish
 *   panelField.getPanel().setBorder(BorderFactory.createLoweredBevelBorder()); // add border if you wish
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
public class PanelProperty extends AbstractProperty {

    private LayoutManager layoutManager;

    public PanelProperty(String id) {
        this(id, null);
    }

    public PanelProperty(String id, LayoutManager layoutManager) {
        super(id, "");
        this.layoutManager = layoutManager;
    }

    /**
     * Most properties generate FormField instances that allow user input, but we do not:
     *
     * @return false, to indicate that we are effectively read-only.
     */
    @Override
    public boolean isAllowsUserInput() {
        return false;
    }

    public LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public PanelProperty setLayoutManager(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
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
        return layoutManager == null ? new PanelField() : new PanelField(layoutManager);
    }

    @Override
    public void loadFromFormField(FormField field) {
        // Nothing to do here as we don't store any data here
    }
}
