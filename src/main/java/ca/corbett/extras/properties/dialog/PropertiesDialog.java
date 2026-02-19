package ca.corbett.extras.properties.dialog;

import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FormField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * TODO update Javadocs for updated class structure - all of this is changing in 2.8
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 1.8, 2024-12-30
 */
public abstract class PropertiesDialog extends JDialog {

    private static final Logger logger = Logger.getLogger(PropertiesDialog.class.getName());

    public static final String DIALOG_PROP = "propertiesDialog";
    public static final Alignment DEFAULT_ALIGNMENT = Alignment.TOP_LEFT;
    public static final int DEFAULT_BORDER_MARGIN = 8;
    public static final int INITIAL_WIDTH = 640;
    public static final int INITIAL_HEIGHT = 480;
    public static final int MINIMUM_WIDTH = 400;
    public static final int MINIMUM_HEIGHT = 360;

    protected final Window owner;
    protected final List<AbstractProperty> properties;
    protected final List<FormPanel> formPanels;
    protected final List<String> categories;
    protected final Map<String, List<String>> subcategoriesByCategory;
    protected final KeyStrokeManager keyStrokeManager;
    protected Alignment alignment = DEFAULT_ALIGNMENT;
    protected int borderMargin = DEFAULT_BORDER_MARGIN;
    private boolean wasOkayed = false;
    private boolean isInitialized = false;

    /**
     * Creates and returns a "classic"-style PropertiesDialog, where each top-level category
     * is represented as a tab in a tabbed pane, and subcategories are separated by header labels.
     * This was the only style of PropertiesDialog in releases predating 2.8, and is offered
     * here as an option for applications that don't want to switch to the new style.
     *
     * @param owner                       The Window that owns this dialog. This is used to center the dialog over the owner.
     * @param title                       The title to show in the dialog header.
     * @param properties                  The list of properties that this dialog is meant to edit. Ideally, not empty or null.
     * @param alwaysShowSubcategoryLabels Whether to show subcategory header labels even in the case where there's only one subcategory for a given category.
     * @return A new PropertiesDialog instance with the "classic" tabbed pane style.
     */
    public static PropertiesDialog createClassicDialog(Window owner,
                                                       String title,
                                                       List<AbstractProperty> properties,
                                                       boolean alwaysShowSubcategoryLabels) {
        return new TabPanePropertiesDialog(owner, title, properties, alwaysShowSubcategoryLabels);
    }

    /**
     * Returns a new PropertiesDialog instance with the "action panel" style, where properties are grouped
     * into ActionGroups and laid out in an ActionPanel rather than a tabbed pane. This style was introduced
     * in swing-extras 2.8, but is not mandatory - use createClassicDialog if you want to stick with the old style.
     * <p>
     * You can get direct access to the ActionPanel for styling purposes by invoking
     * getActionPanel() on the returned instance, with a class cast:
     * </p>
     * <pre>
     *     PropertiesDialog dialog = PropertiesDialog.createActionPanelDialog(owner, title, properties);
     *     ActionPanel actionPanel = ((ActionPanelPropertiesDialog)dialog).getActionPanel();
     * </pre>
     * <p>
     *     <b>Panel headers</b> - In the ActionPanel style, you have the option to add header labels to each FormPanel.
     *     These headers will be generated based on the subcategory name. If your layout is self-explanatory,
     *     or you've already added your own LabelProperty instances, you can suppress these auto-generated
     *     header labels by setting addPanelHeaders to false.
     * </p>
     *
     * @param owner      The Window that owns this dialog. This is used to center the dialog over the owner.
     * @param title      The title to show in the dialog header.
     * @param properties The list of properties that this dialog is meant to edit. Ideally, not empty or null.
     * @param addPanelHeaders Whether to add header labels to each FormPanel in the ActionPanel.
     * @return A new PropertiesDialog instance with the "action panel" style.
     */
    public static PropertiesDialog createActionPanelDialog(Window owner,
                                                           String title,
                                                           List<AbstractProperty> properties,
                                                           boolean addPanelHeaders) {
        return new ActionPanelPropertiesDialog(owner, title, properties, addPanelHeaders);
    }

    /**
     * Use the static factory methods to create a new PropertiesDialog instance.
     *
     * @param owner      The Window that owns this dialog. This is used to center the dialog over the owner.
     * @param title      The title to show in the dialog header.
     * @param properties The list of properties that this dialog is meant to edit. Ideally, not empty or null.
     */
    protected PropertiesDialog(Window owner, String title, List<AbstractProperty> properties) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        this.owner = owner;
        setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        keyStrokeManager = new KeyStrokeManager(this);

        // We might legitimately get no properties, if all properties are hidden or disabled,
        // of if we simply weren't given any. In that case, let's generate a placeholder
        // label field indicating that we have nothing to show.
        if (properties == null || properties.isEmpty()) {
            this.properties = new ArrayList<>(1);
            this.properties.add(new LabelProperty("defaultLabel", "No properties to show."));
        }
        else {
            this.properties = new ArrayList<>(properties);
        }

        this.formPanels = new ArrayList<>();
        this.categories = PropertiesManager.getCategories(this.properties);
        this.subcategoriesByCategory = new HashMap<>(this.categories.size());
        for (String category : categories) {
            subcategoriesByCategory.put(category, PropertiesManager.getSubcategories(category, this.properties));
        }

        add(buildButtonPanel(), BorderLayout.SOUTH);
        keyStrokeManager.registerHandler("esc", e -> dispose());
    }

    public PropertiesDialog setAlignment(Alignment alignment) {
        if (alignment == null) {
            throw new IllegalArgumentException("Alignment cannot be null");
        }
        this.alignment = alignment;
        for (FormPanel formPanel : formPanels) {
            if (formPanel != null) {
                formPanel.setAlignment(alignment);
            }
        }
        return this;
    }

    public PropertiesDialog setBorderMargin(int margin) {
        if (margin < 0) {
            throw new IllegalArgumentException("Margin cannot be negative");
        }
        this.borderMargin = margin;
        for (FormPanel formPanel : formPanels) {
            if (formPanel != null) {
                formPanel.setBorderMargin(margin);
            }
        }
        return this;
    }

    /**
     * Subclasses must implement this to populate the formPanels list with the FormPanels to be used in this dialog.
     * You have access to the following class properties:
     * <ul>
     *     <li>properties: the list of properties that this dialog is meant to edit.</li>
     *     <li>categories: the list of categories represented in the properties list.</li>
     *     <li>subcategoriesByCategory: a map of category name to list of subcategories for that category.</li>
     *     <li>formPanels: the (already created, but empty) List of formPanels to be populated.</li>
     * </ul>
     * <p>
     *     Subclasses can decide how to break up the properties list into FormPanels as they see fit.
     *     The simplest example would be to simply put everything inline in one big FormPanel.
     *     A smarter example might be to use a tabbed pane with a tab for each category, or
     *     an ActionPanel with action groups for each category.
     * </p>
     */
    protected abstract void populateFormPanels();

    /**
     * Sets up the layout of this dialog and populates formPanelList with the FormPanels to be used in this dialog.
     * Descendant classes must implement this method. The expectation is that the subclass will populate
     * the BorderLayout.CENTER region, but subclasses are also free to install something else in the NORTH, EAST,
     * or WEST regions if they wish. The SOUTH region is reserved for our button panel.
     */
    protected abstract void initLayout();

    /**
     * This will be invoked by our validation method in the case where a form validation error occurs.
     * Descendant classes must implement this method to make the given FormPanel visible to the user, so that
     * they can see the validation errors and fix them. The details of how to do this will depend
     * on how FormPanels are presented in this dialog, so it is left up to descendant classes.
     *
     * @param formPanel The FormPanel that contains validation errors that the user needs to fix.
     */
    protected abstract void makeFormPanelVisible(FormPanel formPanel);

    /**
     * Overridden to update our position if the owner window moves.
     *
     * @param visible Whether to show or hide the dialog.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            if (!isInitialized) {
                // We only want to do this the first time we show the dialog, not every time:
                isInitialized = true;
                populateFormPanels(); // subclass will handle this
                initLayout(); // subclass will handle this
                tagFormFields(this);
            }
        }
        setLocationRelativeTo(owner);
        super.setVisible(visible);
    }

    /**
     * Overridden so we can dispose our KeyStrokeManager when we're done.
     */
    @Override
    public void dispose() {
        tagFormFields(null); // untag all form fields to remove references to this dialog
        keyStrokeManager.dispose();
        super.dispose();
    }

    /**
     * Reports whether this dialog was closed via the OK button, meaning that the form
     * was validated and all values are acceptable.
     *
     * @return true if the form was validated and closed via the OK button, false otherwise.
     */
    public boolean wasOkayed() {
        return wasOkayed;
    }

    /**
     * Returns the FormField with the given identifier if it exists anywhere on this dialog.
     * Note that this method will return the first FormField that it finds with the given identifier.
     * It shouldn't be possible to have multiple FormFields with the same identifier, but if this does
     * happen, then all such fields after the first one found will be ignored by this method.
     *
     * @param identifier The string identifier of the field in question.
     * @return A FormField instance representing that field, or null if not found.
     */
    public FormField findFormField(String identifier) {
        for (FormPanel formPanel : formPanels) {
            if (formPanel != null) {
                FormField field = formPanel.getFormField(identifier);
                if (field != null) {
                    return field;
                }
            }
        }

        // Couldn't find it:
        return null;
    }

    /**
     * Invoked by the OK button. Validates all FormPanels, and if any of them fail validation,
     * makes the first one with errors visible.
     */
    protected void validateFormAndClose() {
        FormPanel firstFormPanelWithErrors = null;

        // Validate each and every FormPanel, even if we encounter errors:
        // (This allows the user to navigate FormPanels and see ALL validation errors,
        //  not just those on whichever FormPanel first failed validation.)
        for (FormPanel formPanel : formPanels) {
            if (formPanel != null && !formPanel.isFormValid()) {
                firstFormPanelWithErrors = formPanel;
            }
        }

        // If any of them failed validation, make the first one visible and we're done:
        if (firstFormPanelWithErrors != null) {
            // We have to defer to the subclass to make the form panel visible,
            // since we don't know how the subclass is presenting the form panels:
            makeFormPanelVisible(firstFormPanelWithErrors);
            return; // form stays open until valid or canceled
        }

        // If we get here, all forms are valid:
        wasOkayed = true;
        dispose();
    }

    /**
     * Builds the ok/cancel button panel at the bottom. Currently not configurable.
     */
    protected JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton btn = new JButton("OK");
        btn.addActionListener(e -> validateFormAndClose());
        btn.setPreferredSize(new Dimension(100, 24));
        panel.add(btn);

        btn = new JButton("Cancel");
        btn.addActionListener(e -> dispose());
        btn.setPreferredSize(new Dimension(100, 24));
        panel.add(btn);

        return panel;
    }

    /**
     * Sets an extra attribute on all FormFields so that they have a reference to this dialog.
     * This can be used when setting up custom actions on FormFields. Specifically, the
     * findFormField() method in this class can be very useful to look up other FormFields
     * on the same dialog, even if they are not on the same FormPanel. This allows you to
     * change the state or the contents of other FormFields in response to changes on a given FormField.
     * Example usage:
     * <pre>
     *     // Create a checkbox that can be used to enable or disable a text field:
     *     BooleanProperty enabledProp = new BooleanProperty("enabledProp", "Enable text entry", true);
     *     ShortTextProperty textProp = new ShortTextProperty("textProp", "Text entry:", "");
     *
     *     // We can use PropertyFormFieldChangeListener to automatically add a value changed
     *     // listener to the generated form field:
     *     enabledProp.addPropertyFormFieldChangeListener(event -&gt; {
     *         // Get the CheckBoxField that triggered the change event:
     *         CheckBoxField checkbox = (CheckBoxField)event.getFormField();
     *
     *         // "event" gives us access to the containing FormPanel, but that might
     *         // not be good enough, if the field we want to change is on a different FormPanel.
     *         // So, start by getting the field's reference to its PropertiesDialog:
     *         PropertiesDialog dialog = (PropertiesDialog) field.getExtraAttribute(PropertiesDialog.DIALOG_PROP);
     *         if (dialog != null) {
     *             // Now we have the dialog, we can look up any other field on the dialog by its identifier:
     *             ShortTextField textField = (ShortTextField) dialog.findFormField("textProp");
     *             if (textField != null) {
     *                 // Now we have the text field, we can enable or disable it based on the checkbox value:
     *                 textField.setEnabled(checkbox.isChecked());
     *
     *                 // This may seem complex, but it's also very powerful!
     *             }
     *         }
     *     });
     * </pre>
     * <p>
     * As a general design guideline, you should keep form fields that modify each other on the same
     * FormPanel to avoid confusion (otherwise, the update may happen off-screen and won't be obvious).
     * But, because PropertiesDialog subclasses can lay out FormPanels in any way they want,
     * it's not always obvious which fields will be on the same FormPanel. So, looking up the
     * PropertiesDialog as shown above is safer than using the event's FormPanel reference, which
     * may or may not know about the field you want to modify.
     * </p>
     *
     * @param dialog A PropertiesDialog instance, or null to clear the reference.
     */
    private void tagFormFields(PropertiesDialog dialog) {
        // Tag all form fields with a reference to this dialog, so that they can access it if needed:
        for (FormPanel formPanel : formPanels) {
            if (formPanel != null) {
                for (FormField field : formPanel.getFormFields()) {
                    field.setExtraAttribute(DIALOG_PROP, dialog);
                }
            }
        }
    }
}
